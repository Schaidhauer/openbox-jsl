def call(Map params) {
    sh 'docker build ../resources/ -t ansible-docker:latest'
    sh 'docker run --rm ansible-docker:latest ansible-playbook' + '
       '/ansible/' + params.playbook + '--extra-vars "{'
       'deploy: ' + params.deploy + ',' +
       'ec2_access_key: ' + params.accessKey + ',' +
       'ec2_secret_key: ' + params.secretKey '}"'
}
