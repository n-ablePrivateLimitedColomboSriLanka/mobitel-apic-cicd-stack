def call(body) {
    // evaluate the body block, and collect configuration into the object
    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()
    pipeline {
        agent any
        options {
            skipDefaultCheckout(true)
        }
        parameters {
            string(name: 'gitRepositoryUrl', defaultValue: pipelineParams.gitRepositoryUrlDefaultValue)
            string(name: 'trunkBranch', defaultValue: pipelineParams.trunkBranchDefaultValue)
            string(name: 'commitId', defaultValue: pipelineParams.commitIdDefaultValue)
            string(name: 'bitBucketCredentialsId', defaultValue: pipelineParams.bitBucketCredentialsIdDefaultValue)
        }
        stages {
            stage('Git Checkout') {
                steps {
                    cleanWs()
                    script {
                        scmvars = checkout scmGit(
                                        branches: [[name: "${params.commitId}"]],
                                        extensions: [
                                            cloneOption(depth: 1, noTags: true, reference: '', shallow: true),
                                            [$class: 'IgnoreNotifyCommit'],
                                        ],
                                        userRemoteConfigs: [
                                            [credentialsId: params.bitBucketCredentialsId, url: params.gitRepositoryUrl]
                                        ]
                                 )
                    }
                }
            }
            stage('Create Release') {
                steps {
                    script {
                        yamlFile = findFiles(glob: '**/*api*.yaml', excludes: '**/*product*.yaml')[0]
                        yamlFileObject = readYaml file: yamlFile
                        versionInSource = yamlFileObject.info.version
                    }
                    sh "git tag ${versionInSource}"
                }
            }

            stage('Publish Release') {
                steps {
                    withCredentials([string(credentialsId: params.bitBucketCredentialsId, variable: 'GIT_TOKEN')]) {
                        sh "git -c http.extraheader='Authorization: Bearer $GIT_TOKEN' push origin ${versionInSource}"
                    }
                }
            }
        }
    }
}
