/* groovylint-disable CompileStatic, DuplicateStringLiteral, VariableName, VariableTypeRequired */
/* groovylint-disable-next-line UnusedVariable */
def api_job_creator_repo_url = APIC_JOB_DSL_REPO_URL

def views = [
    ['name': 'Meta', 'description': 'All Meta Jobs', 'label_regex':'.*labels:.*meta.*'],
    ['name': 'API', 'description': 'All API Projects', 'label_regex':'.*labels:.*api.*'],
    ['name': 'Product', 'description': 'All Product Projects', 'label_regex':'.*labels:.*product.*'],
]

for (view in views) {
    listView(view.name) {
        description(view.description)
        jobFilters {
            regex {
                matchType(MatchType.INCLUDE_MATCHED)
                matchValue(RegexMatchValue.DESCRIPTION)
                regex(view.label_regex)
            }
        }
        columns {
            status()
            weather()
            name()
            lastSuccess()
            lastFailure()
            lastDuration()
            buildButton()
        }
    }
}

pipelineJob('APIJobCreator') {
    description('labels:meta')
    parameters {
        stringParam('repository_clone_url', '', 'Clone URL of a specific repository to be processed')
        stringParam('trunk_branch', APIC_TRUNK_BRANCH_NAME, 'The trunk branch to which feature branches are merged to')
    }
    definition {
        // cpsScm {
        //     lightweight(true)
        //     scm {
        //         git {
        //             remote {
        //                 url(api_job_creator_repo_url)
        //             }
        //             branch('*/master')
        //         }
        //     }
        //     scriptPath('Jenkinsfile')
        //     // sandbox(true)
        // }
        cps {
            sandbox()
        }
    }
}
