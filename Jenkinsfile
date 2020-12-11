import groovy.json.JsonOutput

// https://github.com/camunda/jenkins-global-shared-library
// https://github.com/camunda/cambpm-jenkins-shared-library
@Library(['camunda-ci', 'cambpm-jenkins-shared-library']) _

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
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, false, false, getMavenProfileDir('engine-unit'), getMavenProfileCmd('engine-unit') + cambpmGetDbProfiles('h2'), true)
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
          //emailextrecipients([brokenBuildSuspects()])
          emailext body: 'Please ignore and do not reply', recipientProviders: [brokenBuildSuspects()], subject: 'Jenkins failure', to: 'yana.vasileva@camunda.com'
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

String resolveMavenProfileInfo(String profile) {
  Map PROFILE_PATHS = [
      'engine-unit': [
          directory: 'engine',
          command: 'clean test -Dtest.includes=bpmn -P',
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