def call(Map params) {
	createAWSAnsibleImage(params.sshRepoKey, params.sshGitKey, params.keyname);
	deployApp(params);
	clearEnv();
}

// Cria a imagem
def createAWSAnsibleImage(String repositoryKeypath,
		       	  String ansibleKeypath,
		          String repositoryKeyname) {

	sshKeyFile = "rsa.key"
    	buildContext = "/var/jenkins_home/tmp"

	// Cria diretório para armazenar as chaves
	sh "mkdir " + buildContext + " || rm -f " + buildContext + "/*"

	// Cria arquivos com chaves de acesso aos repositórios aws-ansible e do projeto em questão
	sh "cp " + ansibleKeypath + " " + buildContext + "/" + sshKeyFile
	sh "cp " + repositoryKeypath + " " + buildContext + "/" + repositoryKeyname  + " && chmod 600 " + buildContext + "/" + repositoryKeyname
	sh "echo -n '" + libraryResource('Dockerfile') + "' > " + buildContext + "/Dockerfile"
 
	// Cria imagem do aws-ansible	
	sh 'docker build --rm --no-cache ' +
	   '--build-arg ANSIBLE_SSH_PRIVATE_KEY_FILE=' + sshKeyFile + ' ' +
	   '--build-arg REPO_SSH_PRIVATE_KEY_FILE=' + repositoryKeyname + ' ' +
	   '-f ' + buildContext + '/Dockerfile -t ansible-docker:latest ' + buildContext
}

def deployApp(Map params) {

	String volumes = ' '
	String reactVolume = ''
	if (params.containsKey('reactBuild')) {
		String reactVolume = '-v ' + params.reactBuild + ':/ansible/roles/react-server/files/build'
		sh 'echo "Montando volume com build de produção do react server"'
		sh 'echo "Parâmetro adicional a ser passado no docker-run: ' + reactVolume + '"'
		volumes = volumes + reactVolume + ' '
	}

	if (params.containsKey('yarnBuild')) {
		sh 'echo "Montando volume com build de produção do yarn-server.."'
		yarnVolume = '-v ' + params.yarnBuild.build + ':/ansible/roles/yarn-server/files/.next ' +
			     '-v ' + params.yarnBuild.jsonPackage  +  ':/ansible/roles/yarn-server/files/package.json ' +
			     '-v ' + params.yarnBuild.nextConfig +  ':/ansible/roles/yarn-server/files/next.config.js '
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

	if (params.containsKey('useBase') 
		extraVar = extraVars + ', use_base: true, new_db: ' + params.newBase
	
	if (params.containsKey('useReact') 
		extraVar = extraVars + ', use_react: true'

	if (params.containsKey('useYarn') 
		extraVar = extraVars + ', use_yarn: true'

	if (params.containsKey('usePyapi') 
		extraVar = extraVars + ', use_pyapi: true'

	if (params.containsKey('useDockerapp') 
		extraVar = extraVars + ', use_dockerapp: true'

	if (params.containsKey('directories') 
		extraVar = extraVars + ', sync_dirs: ' + params.directories

	if (params.containsKey('useAwscli') 
		extraVar = extraVars + ', useAwscli: true'
	
	// Executa o ansible para deploy na AWS
	sh 'docker run ' + reactVolume + ' ' + yarnVolume + ' ' +
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

