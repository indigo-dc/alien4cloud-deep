#!/usr/bin/groovy

@Library(['github.com/indigo-dc/jenkins-pipeline-library@1.3.5']) _

pipeline {
    agent {
       label 'java-a4c'
    }

    environment {
        dockerhub_repo = "indigodatacloud/alien4cloud-deep"
        dockerhub_image_id = ''
        docker_image_name = "automated_testing_alien4cloud-deep"
    }


    stages {


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
                    sh "ls ${env.WORKSPACE}/indigodc-orchestrator-plugin/src"
                    sh "cat ${env.WORKSPACE}/indigodc-orchestrator-plugin/src/dependency-check-report.xml"
                    OWASPDependencyCheckPublish(report="/jenkins/workspace/ode_alien4cloud-deep_jenkins-dev/indigodc-orchestrator-plugin/src/dependency-check-junit.xml")
                    HTMLReport(
                        "${env.WORKSPACE}/indigodc-orchestrator-plugin/src",
                        'dependency-check-report.html',
                        'OWASP Dependency Report')
                    deleteDir()
                }
            }
        }

        stage('Docker build') {
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
                    dockerhub_image_id = DockerBuild(dockerhub_repo,
                                                     tag: env.BRANCH_NAME)
                }
            }
            post {
                failure {
                    DockerClean()
                }
                always {
                    cleanWs()
                }
            }
        }
        /*
        stage('Functional testing') {
            steps {
                dir("integration_testing") {
                  sh 'npm install puppeteer commander'
                  sh 'docker run -d --name ${docker_image_name} -p 8088:8088 dockerhub_image_id'
                  sh 'while : ; do ; $c=`docker logs  --tail 2 alien4cloud-deep | grep -E "Started.*Bootstrap.*in"` ; if [[ ! -z ${c} ]] ; then ; break ; fi ; done ;'
                  sh 'nodejs ui_a4c.js -h \'http://localhost:8088\' -u admin -p admin -t ./AutomatedApp.yml'
                }
            }
            post {
                always {
                  sh 'docker kill ${docker_image_name}'
                  sh 'docker rm ${docker_image_name}'
                }

            }

        }*/

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
                DockerPush(dockerhub_image_id)
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
                    'mariojmdavid',
                    ['wgcastell',
                     'vkozlov',
                     'dlugo',
                     'keiichiito',
                     'laralloret',
                     'ignacioheredia']
                )
            }
        }
    }
}
