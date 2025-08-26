#!/usr/bin/env groovy

pipeline {
    agent {
        kubernetes {
            yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: gradle
    image: gradle:8-jdk21-alpine
    command:
    - sleep
    args:
    - 99d
    volumeMounts:
    - name: docker-sock
      mountPath: /var/run/docker.sock
  - name: docker
    image: docker:latest
    command:
    - sleep
    args:
    - 99d
    volumeMounts:
    - name: docker-sock
      mountPath: /var/run/docker.sock
  volumes:
  - name: docker-sock
    hostPath:
      path: /var/run/docker.sock
"""
        }
    }

    environment {
        GRADLE_OPTS = '-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true'
        SONAR_TOKEN = credentials('sonar-token')
        DOCKER_REGISTRY = 'your-registry.com'
        K8S_NAMESPACE = 'production'
        SLACK_CHANNEL = '#deployments'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        skipStagesAfterUnstable()
        parallelsAlwaysFailFast()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMIT_SHORT = sh(
                        script: "git rev-parse --short HEAD",
                        returnStdout: true
                    ).trim()
                    env.BUILD_VERSION = "${env.BRANCH_NAME}-${env.BUILD_NUMBER}-${env.GIT_COMMIT_SHORT}"
                }
            }
        }

        stage('Prepare') {
            parallel {
                stage('Cache Dependencies') {
                    steps {
                        container('gradle') {
                            sh './gradlew dependencies --no-daemon'
                        }
                    }
                }

                stage('Code Format Check') {
                    steps {
                        container('gradle') {
                            sh './gradlew spotlessCheck --no-daemon'
                        }
                    }
                }
            }
        }

        stage('Build & Test') {
            parallel {
                stage('Unit Tests') {
                    steps {
                        container('gradle') {
                            sh './gradlew test --no-daemon --continue'
                        }
                    }
                    post {
                        always {
                            publishTestResults testResultsPattern: 'build/test-results/test/*.xml'
                            publishCoverage adapters: [
                                jacocoAdapter('build/reports/jacoco/test/jacocoTestReport.xml')
                            ], sourceFileResolver: sourceFiles('STORE_LAST_BUILD')
                        }
                    }
                }

                stage('Integration Tests') {
                    steps {
                        container('gradle') {
                            sh './gradlew integrationTest --no-daemon --continue'
                        }
                    }
                    post {
                        always {
                            publishTestResults testResultsPattern: 'build/test-results/integrationTest/*.xml'
                        }
                    }
                }

                stage('Static Analysis') {
                    parallel {
                        stage('Checkstyle') {
                            steps {
                                container('gradle') {
                                    sh './gradlew checkstyleMain checkstyleTest --no-daemon'
                                }
                            }
                            post {
                                always {
                                    recordIssues enabledForFailure: true,
                                               tools: [checkStyle(pattern: 'build/reports/checkstyle/*.xml')]
                                }
                            }
                        }

                        stage('SpotBugs') {
                            steps {
                                container('gradle') {
                                    sh './gradlew spotbugsMain --no-daemon'
                                }
                            }
                            post {
                                always {
                                    recordIssues enabledForFailure: true,
                                               tools: [spotBugs(pattern: 'build/reports/spotbugs/*.xml')]
                                }
                            }
                        }

                        stage('PMD') {
                            steps {
                                container('gradle') {
                                    sh './gradlew pmdMain --no-daemon'
                                }
                            }
                            post {
                                always {
                                    recordIssues enabledForFailure: true,
                                               tools: [pmdParser(pattern: 'build/reports/pmd/*.xml')]
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('Security Scan') {
            parallel {
                stage('Dependency Check') {
                    steps {
                        container('gradle') {
                            sh './gradlew dependencyCheckAnalyze --no-daemon'
                        }
                    }
                    post {
                        always {
                            publishHTML([
                                allowMissing: false,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'build/reports/dependency-check',
                                reportFiles: 'dependency-check-report.html',
                                reportName: 'Dependency Check Report'
                            ])
                        }
                    }
                }

                stage('Container Security') {
                    when {
                        anyOf {
                            branch 'main'
                            branch 'develop'
                        }
                    }
                    steps {
                        container('docker') {
                            script {
                                sh './gradlew jib --no-daemon'
                                sh 'trivy image ${DOCKER_REGISTRY}/${JOB_NAME}:${BUILD_VERSION}'
                            }
                        }
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                container('gradle') {
                    withSonarQubeEnv('SonarQube') {
                        sh """
                            ./gradlew sonarqube --no-daemon \
                                -Dsonar.projectKey=${JOB_NAME} \
                                -Dsonar.projectVersion=${BUILD_VERSION} \
                                -Dsonar.branch.name=${BRANCH_NAME}
                        """
                    }
                }

                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build Artifacts') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    branch 'release/*'
                }
            }
            steps {
                container('gradle') {
                    sh './gradlew bootJar --no-daemon'
                }

                container('docker') {
                    script {
                        sh """
                            ./gradlew jib --no-daemon \
                                --image=${DOCKER_REGISTRY}/${JOB_NAME}:${BUILD_VERSION} \
                                --image=${DOCKER_REGISTRY}/${JOB_NAME}:latest
                        """
                    }
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'build/libs/*.jar', allowEmptyArchive: false
                }
            }
        }

        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    deployToEnvironment('staging', env.BUILD_VERSION)
                }
            }
        }

        stage('Smoke Tests') {
            when {
                branch 'develop'
            }
            steps {
                container('gradle') {
                    sh './gradlew contractTest --no-daemon'
                }
            }
        }

        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                script {
                    input message: 'Deploy to Production?', ok: 'Deploy',
                          submitterParameter: 'APPROVED_BY'

                    deployToEnvironment('production', env.BUILD_VERSION)
                }
            }
        }

        stage('Performance Tests') {
            when {
                branch 'main'
            }
            steps {
                container('gradle') {
                    sh './gradlew jmh --no-daemon'
                }

                script {
                    // K6 Load Testing
                    sh '''
                        docker run --rm -i grafana/k6 run --vus 50 --duration 5m - <<EOF
                        import http from 'k6/http';
                        import { check, sleep } from 'k6';

                        export default function () {
                            let response = http.get('http://staging-service:8080/actuator/health');
                            check(response, {
                                'status is 200': (r) => r.status === 200,
                                'response time < 500ms': (r) => r.timings.duration < 500,
                            });
                            sleep(1);
                        }
EOF
                    '''
                }
            }
            post {
                always {
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'build/reports/jmh',
                        reportFiles: '*.html',
                        reportName: 'JMH Performance Report'
                    ])
                }
            }
        }
    }

    post {
        always {
            script {
                // Cleanup
                sh 'docker system prune -f'

                // Collect all reports
                publishHTML([
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'build/reports',
                    reportFiles: '**/*.html',
                    reportName: 'All Reports'
                ])
            }
        }

        success {
            script {
                if (env.BRANCH_NAME == 'main') {
                    slackSend(
                        channel: env.SLACK_CHANNEL,
                        color: 'good',
                        message: """
✅ *Production Deployment Successful*
*Project:* ${JOB_NAME}
*Version:* ${BUILD_VERSION}
*Deployed by:* ${env.APPROVED_BY}
*Build:* ${BUILD_URL}
                        """.trim()
                    )
                }
            }
        }

        failure {
            slackSend(
                channel: env.SLACK_CHANNEL,
                color: 'danger',
                message: """
❌ *Build Failed*
*Project:* ${JOB_NAME}
*Branch:* ${BRANCH_NAME}
*Build:* ${BUILD_URL}
*Stage:* ${env.STAGE_NAME}
                """.trim()
            )
        }

        unstable {
            slackSend(
                channel: env.SLACK_CHANNEL,
                color: 'warning',
                message: """
⚠️ *Build Unstable*
*Project:* ${JOB_NAME}
*Branch:* ${BRANCH_NAME}
*Build:* ${BUILD_URL}
                """.trim()
            )
        }
    }
}

// Helper Functions
def deployToEnvironment(environment, version) {
    sh """
        helm upgrade --install ${JOB_NAME}-${environment} ./helm-charts/${JOB_NAME} \
            --namespace ${environment} \
            --set image.repository=${DOCKER_REGISTRY}/${JOB_NAME} \
            --set image.tag=${version} \
            --set environment=${environment} \
            --wait --timeout=10m
    """

    // Health check después del deployment
    sh """
        kubectl wait --for=condition=available --timeout=300s \
            deployment/${JOB_NAME} -n ${environment}
    """

    // Verificar que el servicio responde
    sh """
        kubectl run curl-test --image=curlimages/curl --rm -i --restart=Never -n ${environment} \
            -- curl -f http://${JOB_NAME}:8080/actuator/health
    """
}