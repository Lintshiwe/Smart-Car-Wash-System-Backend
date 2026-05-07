pipeline {
  agent any

  parameters {
    choice(name: 'ACTION', choices: ['deploy', 'rollback', 'setup', 'status'], description: 'Select action')
    booleanParam(name: 'RUN_TESTS', defaultValue: true, description: 'Run tests on deploy')
    booleanParam(name: 'BUILD_DOCKER', defaultValue: false, description: 'Build Docker images (optional)')
    booleanParam(name: 'DEPLOY_PRODUCTION', defaultValue: false, description: 'Run explicit production deploy command')
    string(name: 'PROD_DEPLOY_CMD', defaultValue: '', description: 'Shell command for production deploy')
  }

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  environment {
    PROJECT = 'carwash'
    MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
    FRAMEWORK_DIR = '/home/lintshiwe/devops-framework'
    PROJECT_DIR = '/home/lintshiwe/devops-framework/projects/carwash'
    ANSIBLE_HOST_KEY_CHECKING = 'False'
  }

  stages {
    stage('Checkout') {
      steps {
        echo "[${PROJECT}] Checking out source..."
        checkout scm
      }
    }

    stage('Build') {
      when {
        expression { params.ACTION == 'deploy' }
      }
      steps {
        echo "[${PROJECT}] Building..."
        sh 'mvn -B -ntp clean compile'
      }
    }

    stage('Test') {
      when {
        allOf {
          expression { params.ACTION == 'deploy' }
          expression { params.RUN_TESTS }
        }
      }
      steps {
        echo "[${PROJECT}] Testing..."
        sh 'mvn -B -ntp test'
      }
    }

    stage('Package') {
      when {
        expression { params.ACTION == 'deploy' }
      }
      steps {
        echo "[${PROJECT}] Packaging..."
        sh 'mvn -B -ntp -DskipTests package'
      }
    }

    stage('Setup Infrastructure') {
      when {
        expression { params.ACTION == 'setup' }
      }
      steps {
        sh '''#!/usr/bin/env bash
set -euo pipefail
ansible-playbook "$FRAMEWORK_DIR/ansible/playbooks/setup.yml" \
  -i "$PROJECT_DIR/inventory.ini" \
  -e @"$PROJECT_DIR/config.yml" -v
'''
      }
    }

    stage('Deploy') {
      when {
        expression { params.ACTION == 'deploy' }
      }
      steps {
        sh '''#!/usr/bin/env bash
set -euo pipefail
ansible-playbook "$FRAMEWORK_DIR/ansible/playbooks/deploy.yml" \
  -i "$PROJECT_DIR/inventory.ini" \
  -e @"$PROJECT_DIR/config.yml" -v
'''
      }
    }

    stage('Rollback') {
      when {
        expression { params.ACTION == 'rollback' }
      }
      steps {
        sh '''#!/usr/bin/env bash
set -euo pipefail
ansible-playbook "$FRAMEWORK_DIR/ansible/playbooks/rollback.yml" \
  -i "$PROJECT_DIR/inventory.ini" \
  -e @"$PROJECT_DIR/config.yml" -v
'''
      }
    }

    stage('Status') {
      when {
        expression { params.ACTION == 'status' }
      }
      steps {
        sh '''#!/usr/bin/env bash
set -euo pipefail
ansible-playbook "$FRAMEWORK_DIR/ansible/playbooks/status.yml" \
  -i "$PROJECT_DIR/inventory.ini" \
  -e @"$PROJECT_DIR/config.yml" -v
'''
      }
    }

    stage('Docker (Optional)') {
      when {
        expression { params.BUILD_DOCKER }
      }
      steps {
        sh '''#!/usr/bin/env bash
set -euo pipefail
"$FRAMEWORK_DIR/scripts/docker-build.sh" carwash
'''
      }
    }

    stage('Approve Production') {
      when {
        expression {
          return params.DEPLOY_PRODUCTION && (env.BRANCH_NAME == null || env.BRANCH_NAME == 'main')
        }
      }
      steps {
        input message: 'Deploy backend build to PRODUCTION?', ok: 'Deploy'
      }
    }

    stage('Deploy Production') {
      when {
        expression {
          return params.DEPLOY_PRODUCTION && (env.BRANCH_NAME == null || env.BRANCH_NAME == 'main')
        }
      }
      steps {
        script {
          if (!params.PROD_DEPLOY_CMD?.trim()) {
            error 'DEPLOY_PRODUCTION=true but PROD_DEPLOY_CMD is empty. Provide deployment command.'
          }
        }
        sh '''#!/usr/bin/env bash
set -euo pipefail
eval "$PROD_DEPLOY_CMD"
'''
      }
    }
  }

  post {
    success {
      echo "[${PROJECT}] Pipeline completed."
    }
    failure {
      echo "[${PROJECT}] Pipeline failed."
    }
    always {
      cleanWs()
    }
  }
}
