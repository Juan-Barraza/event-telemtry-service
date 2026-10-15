pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    environment {
        DOCKERHUB_USER = 'juanb28'
        IMAGE_NAME     = 'event-telemetry-service'
        IMAGE_TAG      = "${env.BUILD_NUMBER}"
        FULL_IMAGE     = "${DOCKERHUB_USER}/${IMAGE_NAME}"
        DOCKER_CREDS   = credentials('docker-hub') 
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw -B clean verify'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Code Quality') {
            when { expression { return params.RUN_SONAR } }
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh './mvnw -B sonar:sonar -Dsonar.projectKey=${IMAGE_NAME}'
                }
            }
        }

        stage('Image Build') {
            steps {
                sh """
                    docker build -t ${FULL_IMAGE}:${IMAGE_TAG} -t ${FULL_IMAGE}:latest .
                """
            }
        }

        stage('Push to Docker Hub') {
            steps {
                sh """
                    echo "${DOCKER_CREDS_PSW}" | docker login -u "${DOCKER_CREDS_USR}" --password-stdin
                    docker push ${FULL_IMAGE}:${IMAGE_TAG}
                    docker push ${FULL_IMAGE}:latest
                """
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    CMD_ID=$(aws ssm send-command \
                    --instance-ids $TELEMETRY_INSTANCE_ID \
                    --document-name AWS-RunShellScript \
                    --parameters "commands=[\\"/opt/app/deploy.sh ${FULL_IMAGE}:${IMAGE_TAG} telemetry ${TELEMETRY_PORTS}\\"]" \
                    --query Command.CommandId --output text)

                    aws ssm wait command-executed \
                    --command-id $CMD_ID --instance-id $TELEMETRY_INSTANCE_ID

                    aws ssm get-command-invocation \
                    --command-id $CMD_ID --instance-id $TELEMETRY_INSTANCE_ID \
                    --query StandardOutputContent --output text
                '''
            }
        }
     }


    parameters {
        booleanParam(name: 'RUN_SONAR', defaultValue: false, description: 'Ejecutar análisis SonarQube')
    }

    post {
        always {
            sh 'docker logout || true'
            sh "docker image prune -f || true"
        }
        success { echo "${IMAGE_NAME}:${IMAGE_TAG} listo" }
        failure { echo " Falló el pipeline" }
    }
}