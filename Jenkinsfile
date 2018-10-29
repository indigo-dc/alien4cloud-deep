#!/usr/bin/groovy

@Library(['github.com/indigo-dc/jenkins-pipeline-library']) _

pipeline {
    agent {
        label 'java'
    }
    
    environment {
        dockerhub_repo = "indigodatacloud/alien4cloud"
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
                    CheckstyleReport('checkstyle-result.xml')
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
    }
}
