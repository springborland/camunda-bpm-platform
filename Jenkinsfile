import groovy.json.JsonOutput

// https://github.com/camunda/jenkins-global-shared-library
// https://github.com/camunda/cambpm-jenkins-shared-library
@Library(['camunda-ci', 'cambpm-jenkins-shared-library']) _

String getAgent(String dockerImage = 'gcr.io/ci-30-162810/centos:v0.4.6', Integer cpuLimit = 4){
  String mavenForkCount = cpuLimit;
  String mavenMemoryLimit = cpuLimit * 2;
  """
metadata:
  labels:
    agent: ci-cambpm-camunda-cloud-build
spec:
  nodeSelector:
    cloud.google.com/gke-nodepool: agents-n1-standard-32-netssd-preempt
  tolerations:
  - key: "agents-n1-standard-32-netssd-preempt"
    operator: "Exists"
    effect: "NoSchedule"
  containers:
  - name: "jnlp"
    image: "${dockerImage}"
    args: ['\$(JENKINS_SECRET)', '\$(JENKINS_NAME)']
    tty: true
    env:
    - name: LIMITS_CPU
      value: ${mavenForkCount}
    - name: TZ
      value: Europe/Berlin
    resources:
      limits:
        cpu: ${cpuLimit}
        memory: ${mavenMemoryLimit}Gi
      requests:
        cpu: ${cpuLimit}
        memory: ${mavenMemoryLimit}Gi
    workingDir: "/home/work"
    volumeMounts:
      - mountPath: /home/work
        name: workspace-volume
  """
}

def failedStageTypes = []

pipeline {
  agent none
  options {
    buildDiscarder(logRotator(numToKeepStr: '5')) //, artifactNumToKeepStr: '30'
    copyArtifactPermission('*');
  }
  parameters {
      string defaultValue: cambpmDefaultBranch(), description: 'The name of the EE branch to run the EE pipeline on', name: 'EE_BRANCH_NAME'
  }
  stages {
    stage('h2 tests') {
      parallel {
        stage('engine-UNIT-h2') {
          when {
            expression {
              withLabels('h2', 'rolling-update', 'migration')
            }
            beforeAgent true
          }
          agent {
            label 'h2'
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, false, false, getMavenProfileDir('engine-unit'), getMavenProfileCmd('engine-unit') + cambpmGetDbProfiles('h2'))
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
            failure {
              cambpmAddFailedStageType(failedStageTypes, 'engine-unit')
            }
          }
        }
      }
    }
  }
  post {
    changed {
      script {
        if (!agentDisconnected()){ 
          emailextrecipients([brokenBuildSuspects()])
        }
      }
    }
    always {
      script {
        if (agentDisconnected()) {// Retrigger the build if the slave disconnected
          //currentBuild.result = 'ABORTED'
          //currentBuild.description = "Aborted due to connection error"
          build job: currentBuild.projectName, propagate: false, quietPeriod: 60, wait: false
        }
      }
    }
  }
}

void runMaven(boolean runtimeStash, boolean archivesStash, boolean qaStash, String directory, String cmd, boolean singleThreaded = false) {
//  if (runtimeStash) unstash "platform-stash-runtime"
//  if (archivesStash) unstash "platform-stash-archives"
//  if (qaStash) unstash "platform-stash-qa"
  String forkCount = singleThreaded? "-DforkCount=1" : '';
  configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
    sh("mvn -s \$MAVEN_SETTINGS_XML ${forkCount} ${cmd} -nsu -Dmaven.repo.local=\${WORKSPACE}/.m2  -f ${directory}/pom.xml -B")
  }
}

boolean withLabels(List labels) { // TODO
  if (env.BRANCH_NAME != cambpmDefaultBranch() && !pullRequest.labels.contains('no-build')) {
    return false;
  }

  if (env.BRANCH_NAME == cambpmDefaultBranch()) {
    return !labels.contains('daily');
  } else if (changeRequest()) {
    for (l in labels) {
      if (pullRequest.labels.contains(l)) {
        return true;
      }
    }
  }

  return false;
}

boolean withLabels(String... labels) {
  return withLabels(Arrays.asList(labels));
}

boolean withDbLabels(String dbLabel) {
  return withLabels(cambpmGetDbType(dbLabel),'all-db')
}

String getDbAgent(String dbLabel, Integer cpuLimit = 4, Integer mavenForkCount = 1){
  Map dbInfo = cambpmGetDbInfo(dbLabel)
  String mavenMemoryLimit = cpuLimit * 4;
  """
metadata:
  labels:
    name: "${dbLabel}"
    jenkins: "slave"
    jenkins/label: "jenkins-slave-${dbInfo.type}"
spec:
  containers:
  - name: "jnlp"
    image: "gcr.io/ci-30-162810/${dbInfo.type}:${dbInfo.version}"
    args: ['\$(JENKINS_SECRET)', '\$(JENKINS_NAME)']
    tty: true
    env:
    - name: LIMITS_CPU
      value: ${mavenForkCount}
    - name: TZ
      value: Europe/Berlin
    resources:
      limits:
        memory: ${mavenMemoryLimit}Gi
      requests:
        cpu: ${cpuLimit}
        memory: ${mavenMemoryLimit}Gi
    volumeMounts:
    - mountPath: "/home/work"
      name: "workspace-volume"
    workingDir: "/home/work"
    nodeSelector:
      cloud.google.com/gke-nodepool: "agents-n1-standard-4-netssd-preempt"
    restartPolicy: "Never"
    tolerations:
    - effect: "NoSchedule"
      key: "agents-n1-standard-4-netssd-preempt"
      operator: "Exists"
    volumes:
    - emptyDir:
        medium: ""
      name: "workspace-volume"
  """
}

String resolveMavenProfileInfo(String profile) {
  Map PROFILE_PATHS = [
      'engine-unit': [
          directory: 'engine',
          command: 'clean test -P',
          labels: ['authorizations']],
      'engine-unit-authorizations': [
          directory: 'engine',
          command: 'clean test -PcfgAuthorizationCheckRevokesAlways,',
          labels: ['authorizations']],
      'webapps-unit': [
          directory: 'webapps',
          command: 'clean test -Dskip.frontend.build=true -P',
          labels: ['default-build']],
      'webapps-unit-authorizations': [
          directory: 'webapps',
          command: 'clean test -Dskip.frontend.build=true -PcfgAuthorizationCheckRevokesAlways,',
          labels: ['default-build']]
  ]

  return PROFILE_PATHS[profile]
}

String getMavenProfileCmd(String profile) {
  return resolveMavenProfileInfo(profile).command
}

String getMavenProfileDir(String profile) {
  return resolveMavenProfileInfo(profile).directory
}

String[] getLabels(String profile) {
  return resolveMavenProfileInfo(profile).labels
}