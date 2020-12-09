import groovy.json.JsonOutput

// https://github.com/camunda/jenkins-global-shared-library
// https://github.com/camunda/cambpm-jenkins-shared-library
@Library(['camunda-ci', 'cambpm-jenkins-shared-library@pipeline-ahmed']) _

def failedStageTypes = []

pipeline {
  agent none
  options {
    buildDiscarder(logRotator(numToKeepStr: '5')) //, artifactNumToKeepStr: '30'
    copyArtifactPermission('*');
  }
  parameters {
      string defaultValue: cambpmDefaultBranch(), description: 'The name of the EE branch to run the EE pipeline on',
      name: 'EE_BRANCH_NAME'
  }

  stages {

    // For more details, please read the note in dbTasksMatrix method.
    stage("DB Matrix") {
      // Should be called in declarative "parallel" to make the visualization works in Jenkins Blue Ocean UI.
      parallel {
        stage('Run DB Matrix') {
          steps {
            script {
              parallel(dbTasksMatrix())
            }
          }
        }
      }
    }

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
            node {
              label 'centos-stable'
            }
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
              addFailedStageType(failedStageTypes, 'engine-unit')
            }
          }
        }
        stage('engine-UNIT-authorizations-h2') {
          when {
            expression {
              withLabels('h2','authorizations')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'centos-stable'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, false, false, getMavenProfileDir('engine-unit-authorizations'), getMavenProfileCmd('engine-unit-authorizations') + cambpmGetDbProfiles('h2'))
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
            failure {
              addFailedStageType(failedStageTypes, 'engine-unit-authorizations')
            }
          }
        }
        stage('engine-rest-UNIT-jersey-2') {
          when {
            expression {
              withLabels('rest-api')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'centos-stable'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, false, false, 'engine-rest/engine-rest/', 'clean install -Pjersey2')
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('engine-rest-UNIT-resteasy3') {
          when {
            expression {
              withLabels('rest-api')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'centos-stable'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, false, false, 'engine-rest/engine-rest/', 'clean install -Presteasy3')
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('webapp-UNIT-h2') {
          when {
            expression {
              withLabels('default-build')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'centos-stable'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, false, false, getMavenProfileDir('webapps-unit'), getMavenProfileCmd('webapps-unit') + cambpmGetDbProfiles('h2'))
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
            failure {
              addFailedStageType(failedStageTypes, 'webapps-unit')
            }
          }
        }
        stage('webapp-UNIT-authorizations-h2') {
          when {
            expression {
              withLabels('default-build')
            }
          }
          agent {
            node {
              label 'centos-stable'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, false, false, getMavenProfileDir('webapps-unit-authorizations'), getMavenProfileCmd('webapps-unit-authorizations') + cambpmGetDbProfiles('h2'))
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
            failure {
              addFailedStageType(failedStageTypes, 'webapps-unit-authorizations')
            }
          }
        }
        stage('engine-IT-tomcat-9-postgresql-96') {
          when {
            expression {
              withLabels('all-as','tomcat')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'postgresql_96'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, true, false, 'qa/', 'clean install -Ptomcat,postgresql,engine-integration')
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('engine-IT-wildfly-postgresql-96') {
          when {
            expression {
              withLabels('all-as','wildfly')
            }
          }
          agent {
            node {
              label 'postgresql_96'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, true, false, 'qa/', 'clean install -Pwildfly,postgresql,engine-integration')
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
            failure {
              addFailedStageType(failedStageTypes, 'engine-IT-wildfly')
            }
          }
        }
        stage('webapp-IT-tomcat-9-h2') {
          when {
            expression {
              withLabels('webapp-integration', 'h2')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'chrome_78'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, true, false, 'qa/', 'clean install -Ptomcat,h2,webapps-integration')
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('webapp-IT-standalone-wildfly') {
          when {
            branch cambpmDefaultBranch();
            beforeAgent true
          }
          agent {
            node {
              label 'chrome_78'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, true, false, 'qa/', 'clean install -Pwildfly-vanilla,webapps-integration-sa')
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('camunda-run-IT') {
          when {
            expression {
              withLabels('run')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'chrome_78'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, true, true, 'distro/run/', 'clean install -Pintegration-test-camunda-run')
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('spring-boot-starter-IT') {
          when {
            expression {
              withLabels('spring-boot')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'chrome_78'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, true, true, 'spring-boot-starter/', 'clean install -Pintegration-test-spring-boot-starter')
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
      }
    }

    stage('db tests + CE webapps IT') {
      parallel {
        stage('engine-api-compatibility') {
          when {
            allOf {
              expression {
                skipStageType(failedStageTypes, 'engine-unit')
              }
              branch cambpmDefaultBranch();
            }
            beforeAgent true
          }
          agent {
            node {
              label 'centos-stable'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, false, false, 'engine/', 'clean verify -Pcheck-api-compatibility')
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('engine-UNIT-plugins') {
          when {
            allOf {
              expression {
                skipStageType(failedStageTypes, 'engine-unit')
              }
              branch cambpmDefaultBranch();
            }
            beforeAgent true
          }
          agent {
            node {
              label 'centos-stable'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, false, false, 'engine/', 'clean test -Pcheck-plugins')
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('engine-UNIT-database-table-prefix') {
          when {
            expression {
              skipStageType(failedStageTypes, 'engine-unit') && withLabels('all-db','h2','db2','mysql','oracle','mariadb','sqlserver','postgresql','cockroachdb') // TODO store as param
            }
            beforeAgent true
          }
          agent {
            node {
              label 'centos-stable'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, false, false, 'engine/', 'clean test -Pdb-table-prefix')
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('webapp-UNIT-database-table-prefix') {
          when {
            allOf {
              expression {
                skipStageType(failedStageTypes, 'webapps-unit')
              }
              branch cambpmDefaultBranch();
            }
            beforeAgent true
          }
          agent {
            node {
              label 'centos-stable'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                nodejs('nodejs-14.6.0') {
                  runMaven(true, false, false, 'webapps/', 'clean test -Pdb-table-prefix')
                }
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('engine-UNIT-wls-compatibility') {
          when {
            allOf {
              expression {
                skipStageType(failedStageTypes, 'engine-unit')
              }
              branch cambpmDefaultBranch();
            }
            beforeAgent true
          }
          agent {
            node {
              label 'centos-stable'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, false, false, '.', 'clean verify -Pcheck-engine,wls-compatibility,jersey')
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('IT-wildfly-domain') {
          when {
            expression {
              skipStageType(failedStageTypes, 'engine-IT-wildfly') && withLabels('wildfly')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'centos-stable'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, true, false, 'qa/', 'clean install -Pwildfly-domain,h2,engine-integration')
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('IT-wildfly-servlet') {
          when {
            expression {
              skipStageType(failedStageTypes, 'engine-IT-wildfly') && withLabels('wildfly')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'centos-stable'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, true, true, 'qa/', 'clean install -Pwildfly,wildfly-servlet,h2,engine-integration')
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
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
          // send email if the slave disconnected
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
  if (runtimeStash) unstash "platform-stash-runtime"
  if (archivesStash) unstash "platform-stash-archives"
  if (qaStash) unstash "platform-stash-qa"
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
          directory: 'engine/',
          command: 'clean test -P',
          labels: ['authorizations']],
      'engine-unit-authorizations': [
          directory: 'engine/',
          command: 'clean test -PcfgAuthorizationCheckRevokesAlways,',
          labels: ['authorizations']],
      'webapps-unit': [
          directory: 'webapps/',
          command: 'clean test -Dskip.frontend.build=true -P',
          labels: ['default-build']],
      'webapps-unit-authorizations': [
          directory: 'webapps/',
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

void addFailedStageType(List failedStageTypesList, String stageType) {
  if (!failedStageTypesList.contains(stageType)) failedStageTypesList << stageType
}

boolean skipStageType(List failedStageTypesList, String stageType) {
  !failedStageTypesList.contains(stageType)
}

def dbTasksMatrix() {
  // This is a workaround to build a matrix programatically and run it in scripted pipeline style
  // till the Jenkins bug "Method code too large!" is fixed.
  // https://issues.jenkins.io/browse/JENKINS-37984
  // https://www.jenkins.io/blog/2019/12/02/matrix-building-with-scripted-pipeline

  Map matrix_axes = [
      DB: [
        'postgresql_96', 'postgresql_94', 'postgresql_107', 'postgresql_112', 'postgresql_122',
        'cockroachdb_201', 'mariadb_100', 'mariadb_102', 'mariadb_103', 'mariadb_galera', 'mysql_57',
        'oracle_11', 'oracle_12', 'oracle_18', 'oracle_19', 'db2_105', 'db2_111', 'sqlserver_2017', 'sqlserver_2019'
      ],
      PROFILE: [
        'engine-unit', 'engine-unit-authorizations', 'webapps-unit', 'webapps-unit-authorizations'
      ]
  ]

  List axes = cambpmGetMatrixAxes(matrix_axes)

  // Parallel task map.
  Map tasks = [failFast: false]

  for(int i = 0; i < axes.size(); i++) {

    // Convert the Axis into valid values for withEnv step.
    Map axis = axes[i]
    List axisEnv = axis.collect { k, v ->
        "${k}=${v}"
    }

    String agentNodeLabel = axis['DB']

    // This If statement works like 'when' in the declarative style.
    // It only adds the database to tasks list according to the PR label.
    if (
      true
      // skipStageType(failedStageTypes, env.PROFILE) &&
      // (withLabels(getLabels(env.PROFILE)) || withDbLabels(env.DB))
    ) {
      tasks[axisEnv.join(', ')] = { ->
        node(agentNodeLabel) {
          withEnv(axisEnv) {
            stage("QA test") {
              // The 'stage' here works like a 'step' in the declarative style.
              stage("Run Maven DB") {
                echo "QA DB Test Stage: ${PROFILE}-${DB}"
                // catchError(stageResult: 'FAILURE') {
                  withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest',
                            mavenSettingsConfig: 'camunda-maven-settings',
                            options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]
                  ) {
                    runMaven(true, false, isQaStashEnabled(env.PROFILE), getMavenProfileDir(env.PROFILE), getMavenProfileCmd(env.PROFILE) + cambpmGetDbProfiles(env.DB) + " " + cambpmGetDbExtras(env.DB), true)
                  }
                //}
              }
              stage("PublishTestResult for DB") {
                cambpmPublishTestResult();
              }
            }
          }
        }
      }
    }
  }

  return tasks
}
