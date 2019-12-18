def call(Map params) {

    sshKeyFile = "rsa.key"
    sshKeyDir = "/var/jenkins_home/tmp"

    sh "mkdir " + sshKeyDir
    sh "cp " + params.sshGitKey + " " + sshKeyDir + "/" + sshKeyFile
    sh "echo '" + libraryResource('Dockerfile')  + "' > " sshKeyDir + "/Dockerfile"

    sh 'docker build --rm --build-arg SSH_PRIVATE_KEY_FILE=' + sshKeyFile +
       '--no-cache -f ' + sshKeyDir + '/Dockerfile -t ansible-docker:latest ' + sshKeyDir

    sh 'rm -rf ' + sshKeyDir

    sh 'docker run --rm ansible-docker:latest ansible-playbook ' + 
       '/ansible/' + params.playbook + '--extra-vars "{'
       'deploy: '  + params.deploy + ',' +
       'ec2_access_key: ' + params.accessKey + ',' +
       'ec2_secret_key: ' + params.secretKey + ',' +
       'key_name: ' + params.keyname  + '}"'
}
