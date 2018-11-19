#!/usr/bin/groovy

@Library(['github.com/indigo-dc/jenkins-pipeline-library']) _

pipeline {
    agent {
        label 'java-a4c'
    }
    
    environment {
        dockerhub_repo = "indigodatacloud/alien4cloud-deep"
        dockerhub_image_id = ''
    }

    stages {
        stage('Code fetching') {
            steps {
                checkout scm 
            }
        }

        stage('Style analysis') {
            steps {
                dir("indigodc-orchestrator-plugin") {
                    sh 'wget https://raw.githubusercontent.com/checkstyle/checkstyle/master/src/main/resources/google_checks.xml'
                    sh 'wget https://github.com/checkstyle/checkstyle/releases/download/checkstyle-8.13/checkstyle-8.13-all.jar'
                    sh 'java -jar checkstyle-8.13-all.jar -c google_checks.xml src/ -e src/test/ -e src/main/assembly/ -f xml -o checkstyle-result.xml'
                }
            }
            post {
                always {
                    CheckstyleReport('**/checkstyle-result.xml')
                }
            }
        }

        stage('Unit testing coverage') {
            steps {
                dir("$WORKSPACE/indigodc-orchestrator-plugin") {
                    MavenRun('clean test')
                }
            }
            post {
                always {
                    jacoco()
                }
            }
        }

        stage('Metrics gathering') {
            agent {
                label 'sloc'
            }
            steps {
                checkout scm
                dir("indigodc-orchestrator-plugin") {
                    SLOCRun()
                }
            }
            post {
                success {
                    dir("indigodc-orchestrator-plugin") {
                        SLOCPublish()
                    }
                }
            }
        }

        stage('Dependency check') {
            agent {
                label 'docker-build'
            }
            steps {
                checkout scm
                OWASPDependencyCheckRun("${env.WORKSPACE}/indigodc-orchestrator-plugin/src", project="alien4cloud-deep")
            }
            post {
                always {
                    OWASPDependencyCheckPublish()
                    HTMLReport('indigodc-orchestrator-plugin/src', 'dependency-check-report.html', 'OWASP Dependency Report')
                    deleteDir()
                }
            }
        }

        stage('DockerHub delivery') {
            when {
                anyOf {
                    branch 'master'
                    buildingTag()
                }
            }
            agent {
                label 'docker-build'
            }
            steps {
                checkout scm
                script {
                    dockerhub_image_id = DockerBuild(dockerhub_repo, env.BRANCH_NAME)
                }
            }
            post {
                success {
                    DockerPush(dockerhub_image_id)
                }
                failure {
                    DockerClean()
                }
                always {
                    cleanWs()
                }
            }
        }
        
        stage('Notifications') {
            when {
                buildingTag()
            }
            steps {
                JiraIssueNotification(
                    'DEEP',
                    'DPM',
                    '10204',
                    "[preview-testbed] New alien4cloud version ${env.BRANCH_NAME} available",
                    "Check new artifacts at:\n\t- Docker image: [${dockerhub_image_id}:${env.BRANCH_NAME}|https://hub.docker.com/r/${dockerhub_image_id}/tags/]\n",
                    ['wp3', 'preview-testbed', "alien4cloud-${env.BRANCH_NAME}"],
                    'Task',
                    'mariojmdavid'
                )
            }
        }
    }
}
