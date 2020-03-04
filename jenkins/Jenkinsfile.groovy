pipeline {
    agent {
        docker {
            registryUrl 'https://devnexus.engineering.clearswift.org'
            image 'devnexus.engineering.clearswift.org:8082/ig-server-2.0.0-rhel-7-build-env:latest'
            label 'igs20'
            args '--tmpfs /tmp -v $HOME/tools:/home/jenkins/tools:ro -v /var/lib/jenkins/tools:/var/lib/jenkins/tools:ro'
        }
    }

    parameters {
        booleanParam(name: 'DEPLOY_TO_NEXUS', defaultValue: false, description: 'Deploy Artifacts to Nexus')
    }

    environment {
        String COMMON_JAVA_SOURCEDIR = './'
        String MAVEN_SETTINGS_FILE_UUID = '7efd6a2b-ee91-443a-8065-06e241cd37f0'
    }

    stages {
        stage('Setup') {
            steps {
                echo "Building Branch: ${env.BRANCH}"
            }
        }

        stage('Common - Build') {
            steps {
                script {
                    timeout(time: 60, unit: 'MINUTES') {
                        buildPOM("./", 'pom.xml', 'clean install', "")
                    }
                }
            }
        }

        // stage('Deploy')
        // {
        //     when {
        //         expression { params.DEPLOY_TO_NEXUS }
        //     }
        //     steps
        //     {
        //         script
        //         {
        //             timeout(time:10, unit: 'MINUTES') {
        //                 buildPOM(env.COMMON_JAVA_SOURCEDIR, 'clearswift-common-pom.xml', 'deploy', '-DskipTests')
        //             }
        //         }
        //     }
        // }
    }
    post {
        always {
            script {
                junit '**/surefire-reports/*.xml'

            }
        }
    }
}

def buildPOM(localRepoPath, buildPOMPath, executionGoals, mavenParams)
{
    echo 'Building: ' + localRepoPath + '/' + buildPOMPath

    withMaven(mavenSettingsConfig: env.MAVEN_SETTINGS_FILE_UUID, mavenLocalRepo: '~/.m2') {
        sh 'cd ' + localRepoPath + ' && $MVN_CMD -f ' + buildPOMPath + ' ' + executionGoals + ' ' +  mavenParams
    }
}

