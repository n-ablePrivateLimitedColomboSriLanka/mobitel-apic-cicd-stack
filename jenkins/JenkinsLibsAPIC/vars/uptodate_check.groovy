def call(body) {
    // evaluate the body block, and collect configuration into the object
    def pipelineParams = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()

    pipeline {
        agent any
        options {
            skipDefaultCheckout(true)
        }
        environment {
            BIBUCKET_BUILD_STATUS_FILE='bitbucket-build-status.json'
        }
        parameters {
            string(name: 'gitRepositoryUrl', defaultValue: pipelineParams.gitRepositoryUrlDefaultValue)
            string(name: 'baseRef', defaultValue: pipelineParams.trunkBranchDefaultValue)
            string(name: 'headRef', defaultValue: pipelineParams.commitIdDefaultValue)
            string(name: 'bitBucketCredentialsId', defaultValue: pipelineParams.bitBucketCredentialsIdDefaultValue)
            string(name: 'bitBucketRestApiBaseUrl', defaultValue: pipelineParams.bitBucketRestApiBaseUrlDefaultValue)
        }
        stages {
            stage('Git Checkout') {
                steps {
                    cleanWs()
                    script {
                        scmvars = checkout scmGit(
                                        branches: [[name: "${params.headRef}"]],
                                        extensions: [
                                            [$class: 'IgnoreNotifyCommit'],
                                        ],
                                        userRemoteConfigs: [
                                            [credentialsId: params.bitBucketCredentialsId, url: params.gitRepositoryUrl]
                                        ]
                                 )
                    }
                }
            }
            stage('Check if up to date') {
                steps {
                    script {
                        isUpToDate = sh(script: "git merge-base --is-ancestor ${params.baseRef}" +
                                              " ${params.headRef}", returnStatus: true)
                        error message: 'The head branch is not up to date with the base branch'
                    }
                    script {
                        bitBucketBuildStatus = [
                            state: 'SUCCESSFUL',
                            key: 'UpToDateCheck',
                            /* groovylint-disable-next-line DuplicateStringLiteral */
                            name: 'UpToDateCheck',
                            url: env.BUILD_URL,
                            description: 'Success marks the head branch as up to date with the base branch'
                        ]
                    }
                }
            }
        }
        post {
            success {
                script {
                    writeJSON json: bitBucketBuildStatus, file: env.BIBUCKET_BUILD_STATUS_FILE
                }
            }
            failure {
                script {
                    bitBucketBuildStatus.state = 'FAILED'
                    writeJSON json: bitBucketBuildStatus, file: env.BIBUCKET_BUILD_STATUS_FILE
                }
            }
            always {
                withCredentials([string(credentialsId: params.bitBucketCredentialsId, variable: 'GIT_TOKEN')]) {
                    sh "curl -X POST -H 'Content-Type: application/json' " +
                        "-H 'Authorization: Bearer $GIT_TOKEN' " +
                        '-d @bitbucket-build-status.json ' +
                        "${params.bitBucketRestApiBaseUrl}/rest/build-status/1.0/commits/${params.headRef}"
                }
            }
        }
    }
}