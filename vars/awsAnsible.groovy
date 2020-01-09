def call(Map params) {

    sshKeyFile = "rsa.key"
    sshKeyDir = "/var/jenkins_home/tmp"

    // Cria diretório para armazenar as chaves
    sh "mkdir " + sshKeyDir + " || rm -f " + sshKeyDir + "/*"

    // Cria arquivos com chaves de acesso aos repositórios do aws-ansible e do projeto em questão
    sh "cp " + params.sshGitKey + " " + sshKeyDir + "/" + sshKeyFile
    sh "cp " + params.sshRepoKey + " " + sshKeyDir + "/" + params.keyname  + " && chmod 644 " + sshKeyDir + "/" + params.keyname
    sh "echo -n '" + libraryResource('Dockerfile') + "' > /var/jenkins_home/tmp/Dockerfile"

    // Cria imagem do aws-ansible
    sh 'docker build --rm ' + ' ' +
       '--build-arg ANSIBLE_SSH_PRIVATE_KEY_FILE=' + sshKeyFile + ' ' +
       '--build-arg REPO_SSH_PRIVATE_KEY_FILE=' + params.keyname + ' ' +
       '--no-cache -f ' + sshKeyDir + '/Dockerfile -t ansible-docker:latest ' + sshKeyDir

    // Executa o ansible para deploy na AWS
    sh 'docker run --rm -v ' + sshKeyDir + ':/ansible/ssh/tmp ' +
       'ansible-docker:latest ansible-playbook ' + 
       '/ansible/' + params.playbook + ' --extra-vars "{' +
       'deploy: '  + params.deploy + ',' +
       'ec2_access_key: ' + params.accessKey + ',' +
       'ec2_secret_key: ' + params.secretKey + ',' +
       'jenkins_key_name: ' + params.keyname  + ',' +
       'admin_username: ' + params.username +  ',' + 
       'admin_public_key: ' + params.public_key + '}"'

    // Remove imagem após uso
    sh 'docker rmi -f ansible-docker:latest'

    // Chaves já foi utilizadas, então deleta
    sh 'rm -rf ' + sshKeyDir
}
