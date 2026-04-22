pipeline {
    agent { label 'Bento' }

    parameters {
        choice(
            name: 'BRANCH_NAME',
            choices: ['main', 'dev', 'qa', 'feature'],
            description: 'Select Git branch'
        )

        booleanParam(
            name: 'RUN_RC02',
            defaultValue: false,
            description: 'Trigger RCS-02 job after success?'
        )
    }

    tools {
        jdk 'Jdk17'
        maven 'maven'
    }

    stages {

        stage('Pipeline Started') {
            steps {
                echo "Executing Pipeline for branch: ${params.BRANCH_NAME}"
            }
        }

        stage('Git Checkout') {
            steps {
                git branch: "${params.BRANCH_NAME}", url: 'https://github.com/xdvraj/RockPaperScissorsJava.git'
            }
        }

        stage('Compile') {
            steps {
                sh 'mvn compile'
            }
        }

        stage('Testing') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package'
            }
        }
    }

    post {
        success {
            echo 'Pipeline SUCCESS'

            script {
                if (params.RUN_RC02) {
                    echo 'Triggering RCS-02 job...'
                    build job: 'RCS-02'
                } else {
                    echo 'Skipping RCS-02 job'
                }
            }
        }

        failure {
            echo 'Pipeline FAILED'
        }
    }
}