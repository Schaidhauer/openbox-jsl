def call(Map params) {
	createAWSAnsibleImage(params.sshRepoKey, params.sshGitKey, params.keyname, params.sslAccountKey);
	deployApp(params);
	clearEnv();
}

// Cria a imagem
def createAWSAnsibleImage(String repositoryKeypath,
		       	  String ansibleKeypath,
		          String repositoryKeyname,
			  String sslKeypath) {

	sslKeyFile = "account.key"
	sshKeyFile = "rsa.key"
    	buildContext = "/var/jenkins_home/tmp"

	// Cria diretório para armazenar as chaves
	sh "mkdir " + buildContext + " || rm -f " + buildContext + "/*"

	// Cria arquivos com chaves de acesso aos repositórios aws-ansible e do projeto em questão
	// Agora também cria arquivo com chave privada da conta associada ao certificado de domínio
	sh "cp " + ansibleKeypath + " " + buildContext + "/" + sshKeyFile
	sh "cp " + sslKeypath + " " + buildContext + "/" + sslKeyFile
	sh "cp " + repositoryKeypath + " " + buildContext + "/" + repositoryKeyname  + " && chmod 600 " + buildContext + "/" + repositoryKeyname
	sh "echo -n '" + libraryResource('Dockerfile') + "' > " + buildContext + "/Dockerfile"

	sh "ls -lasht " + buildContext
 
	// Cria imagem do aws-ansible	
	sh 'docker build --rm --no-cache ' +
	   '--build-arg ANSIBLE_SSH_PRIVATE_KEY_FILE=' + sshKeyFile + ' ' +
	   '--build-arg REPO_SSH_PRIVATE_KEY_FILE=' + repositoryKeyname + ' ' +
	   '--build-arg SSL_PRIVATE_KEY_FILE=' + sslKeyFile + ' ' +
	   '-f ' + buildContext + '/Dockerfile -t ansible-docker:latest ' + buildContext
}

def deployApp(Map params) {

	String volumes = ' '
	if (params.containsKey('reactBuild')) {
		String reactVolume = '-v ' + params.reactBuild + ':/ansible/roles/react-server/files/build'
		sh 'echo "Montando volume com build de produção do react server"'
		sh 'echo "Parâmetro adicional a ser passado no docker-run: ' + reactVolume + '"'
		volumes = volumes + reactVolume + ' '
	}

	if (params.containsKey('yarnBuild')) {
		sh 'echo "Montando volume com build de produção do yarn-server.."'
		String yarnVolume = '-v ' + params.yarnBuild.build + ':/ansible/roles/yarn-server/files/.next ' +
			     '-v ' + params.yarnBuild.jsonPackage  +  ':/ansible/roles/yarn-server/files/package.json ' +
			     '-v ' + params.yarnBuild.nextConfig +  ':/ansible/roles/yarn-server/files/next.config.js'
		sh 'echo "Parâmetro adicional a ser passado no docker-run: ' + yarnVolume + '"'
		volumes = volumes + yarnVolume + ' '
	}
	
	String extraVars = 'app_id: ' + params.app + ', ' +
			   'deploy: '  + params.deploy + ', ' +
	   		   'repository: ' + params.repository + ', ' +
	   		   'branch: ' + params.branch + ', ' +
			   'ec2_access_key: ' + params.accessKey + ', ' +
			   'ec2_secret_key: ' + params.secretKey + ', ' +
			   'jenkins_key_name: ' + params.keyname  + ', ' +
			   'admin_username: ' + params.username +  ', ' + 
			   'admin_public_key: ' + params.public_key

	if (params.containsKey('useBase'))
		extraVars = extraVars + ', use_base: true, new_db: ' + params.newBase
	
	if (params.containsKey('useReact')) 
		extraVars = extraVars + ', use_react: true'

	if (params.containsKey('useYarn'))
		extraVars = extraVars + ', use_yarn: true'
	
	if (params.containsKey('domain'))
		extraVars = extraVars + ', use_ssl: true, app_domain: \'' + params.domain + '\''

	if (params.containsKey('usePyapi')) 
		extraVars = extraVars + ', use_pyapi: true'

	if (params.containsKey('useDockerapp'))
		extraVars = extraVars + ', use_dockerapp: true'

	if (params.containsKey('directories'))
		extraVars = extraVars + ', sync_dirs: ' + params.directories

	if (params.containsKey('useAwscli'))
		extraVars = extraVars + ', useAwscli: true'
	
	// Executa o ansible para deploy na AWS
	sh 'docker run' + volumes + ' ' +
	   'ansible-docker:latest ansible-playbook deploy.playbook.yml --extra-vars "{' + extraVars + '}"'
 
	// Apaga artefatos após deploy
	if (params.containsKey('reactBuild')) {
		sh 'echo "Apagando artefatos"'
		sh 'rm -rf ' + params.reactBuild
	}

	// Apaga artefatos após deploy
	if (params.containsKey('yarnBuild')) {
		sh 'echo "Apagando artefatos"'
		sh 'rm -rf ' + params.yarnBuild.build
		sh 'rm -rf ' + params.yarnBuild.jsonPackage
	}
}

// Remove imagem após uso
def clearEnv() {
	sh 'rm -rf /var/jenkins_home/tmp'
	sh 'docker rmi -f ansible-docker:latest'
}

