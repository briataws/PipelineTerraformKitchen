def call(Map pipelineParams) {

  if(!pipelineParams) {
    pipelineParams = [:]
  }

  node {
    stage('Initialize') {
      TERRAFORM_CONTAINER_INPUTS = env.TERRAFORM_CONTAINER_INPUTS?:''
    }
  }

  pipeline {
    agent {
      label 'ecs'
      }

    environment {
      AWS_DEFAULT_REGION = "us-west-2"
      TF_LOG = "${pipelineParams.terraformDebugOutput?:''}"
      DESTROY_TIMEOUT = "${pipelineParams.terraformDestroyTimeout ? pipelineParams.terraformDestroyTimeout : '30'}"
    }

    options {
      buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {

      stage('kitchen: converge') {
        steps {
          sshagent ([sshKey()]) {
            sh 'kitchen converge'
          }
        }
      }

      stage('kitchen: verify') {
        steps {
          sshagent ([sshKey()]) {
            sh 'terraform output --json > test/integration/default/files/terraform.json'
            sh 'kitchen verify'
          }
        }
      }

      stage('kitchen: prompt destroy') {
        steps {
          script{
            def userInput = true
            def didTimeout = false
            try {
              timeout(time: DESTROY_TIMEOUT, unit: 'MINUTES'){
              userInput = input(
              id: 'Proceed1', message: 'Do you want to perform Kitchen Destroy?', parameters: [
              [$class: 'BooleanParameterDefinition', defaultValue: true, description: '', name: 'Please confirm you agree with this']
              ])
              }
            }
            catch(err) {
              def user = err.getCauses()[0].getUser()
              if('SYSTEM' == user.toString()) {
                  didTimeout = true
              } else {
                  userInput = false
                  echo "Aborted by: [${user}]"
              }
            }
            if (didTimeout) {
                sshagent ([sshKey()]) {
                  sh 'kitchen destroy'
                }
            } else if (userInput == true) {
                sshagent ([sshKey()]) {
                  sh 'kitchen destroy'
                }
            } else {
                currentBuild.result = 'FAILURE'
                sshagent ([sshKey()]) {
                  sh 'kitchen destroy'
                }
            }
          }
        }
      }
    }

    post {
      always {
        junit 'test/integration/default/junit/junit.xml'
        sh 'kitchen destroy'
      }
    }
  }
}
