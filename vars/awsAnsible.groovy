def call(Map params) {

    sshKeyFile = "rsa.key"
    sshKeyDir = "/var/jenkins_home/tmp"

    // Cria diretório para armazenar as chaves
    sh "mkdir " + sshKeyDir + " || rm -f " + sshKeyDir + "/*"

    // Cria arquivos com chaves de acesso aos repositórios do aws-ansible e do projeto em questão
    sh "cp " + params.sshGitKey + " " + sshKeyDir + "/" + sshKeyFile
    sh "cp " + params.sshRepoKey + " " + sshKeyDir + "/" + params.keyname  + " && chmod 600 " + sshKeyDir + "/" + params.keyname
    sh "echo -n '" + libraryResource('Dockerfile') + "' > /var/jenkins_home/tmp/Dockerfile"

    // Cria imagem do aws-ansible
    sh 'docker build --rm --no-cache ' +
       '--build-arg ANSIBLE_SSH_PRIVATE_KEY_FILE=' + sshKeyFile + ' ' +
       '--build-arg REPO_SSH_PRIVATE_KEY_FILE=' + params.keyname + ' ' +
       '-f ' + sshKeyDir + '/Dockerfile -t ansible-docker:latest ' + sshKeyDir

    String reactBuild = ''
    if (params.containsKey('reactBuild')) {
       sh 'echo "Montando volume com build de produção do react server.."'
       reactVolume = '-v ' + params.reactBuild + ':/ansible/roles/react-server/files/build'
       sh 'echo "Parâmetro adicional a ser passado no docker-run: ' + reactVolume + '"'
    }

    // Executa o ansible para deploy na AWS
    sh 'docker run ' + reactVolume + ' ' +
       'ansible-docker:latest ansible-playbook deploy.playbook.yml --extra-vars "{' +
       'app_id: ' + params.app + ', ' +
       'deploy: '  + params.deploy + ', ' +
       'use_base: ' + params.useBase  + ', ' +
       'use_react: ' + params.useReact  + ', ' +	
       'ec2_access_key: ' + params.accessKey + ', ' +
       'ec2_secret_key: ' + params.secretKey + ', ' +
       'jenkins_key_name: ' + params.keyname  + ', ' +
       'admin_username: ' + params.username +  ', ' + 
       'admin_public_key: ' + params.public_key + ', ' +
       'sync_dirs: ' + params.directories + ', ' + 
       'new_db: ' + params.newBase + ', ' + 
       'use_awscli: ' + params.useAwscli + '}"'

    // Remove imagem após uso
    sh 'docker rmi -f ansible-docker:latest'
    
    // Apaga artefatos após deploy
    if (params.containsKey('reactBuild')) {
       sh 'echo "Apagando artefatos"'
       sh 'rm -rf ' + params.reactBuild
    }

    // Chaves já foram utilizadas, então deleta
    sh 'rm -rf ' + sshKeyDir
}
