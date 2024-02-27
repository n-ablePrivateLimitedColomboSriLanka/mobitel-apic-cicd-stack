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
            UNIT_TEST_YAML_FILE = 'unit-test.yaml'
            UNIT_TEST_RESULT_FILE = 'unit-test-result.json'
        }
        parameters {
            string(name: 'gitRepositoryUrl', defaultValue: pipelineParams.gitRepositoryUrlDefaultValue)
            string(name: 'trunkBranch', defaultValue: pipelineParams.trunkBranchDefaultValue)
            string(name: 'commitId', defaultValue: pipelineParams.commitIdDefaultValue)
            string(name: 'bitBucketCredentialsId', defaultValue: pipelineParams.bitBucketCredentialsIdDefaultValue)
            string(name: 'testCredentialsId', defaultValue: pipelineParams.testCredentialsIdDefaultValue)
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
            stage('Unit Test') {
                steps {
                    script {
                        unitTestYaml = readYaml file: env.UNIT_TEST_YAML_FILE
                        unitTestSuiteHookUrl = unitTestYaml.hookUrl
                    }
                    withCredentials([
                        usernamePassword(
                            credentialsId: params.testCredentialsId,
                            usernameVariable: 'API_KEY',
                            passwordVariable: 'API_SECRET'
                        )
                    ]) {
                        sh 'curl -X POST ' +
                           "-H 'Content-Type: application/json' " +
                           "-H 'x-api-key: ${API_KEY}' " +
                           "-H 'x-api-secret: ${API_SECRET}' " +
                           "-d '{\"options\": {\"JUnitFormat\": true}}' " +
                           "${unitTestSuiteHookUrl}/tests/run " +
                           ">| ${env.UNIT_TEST_RESULT_FILE}"
                    }
                }
            }
            stage('Publish Unit Test Results') {
                steps {
                    junit env.UNIT_TEST_RESULT_FILE
                }
            }
        }
    }
}
