import openbox.ansible.ApplicationDeploy;

def call(Map params)
{
    // Configura ambiente para realizar deploy
    configure(params.sshGitKey);
    appDeploy = new ApplicationDeploy(
        params.accessKey,
        params.secretKey, 
        params.ecrPassword,
        params.service,
        params.image,
        []);
    sh appDeploy.getBuildCmd();
    sh appDeploy.getRunCmd();

}

def configure(String ansibleKeypath)
{
	sshKeyFile = ApplicationDeploy.ANSIBLE_KEY_FILE;
    buildContext = ApplicationDeploy.DOCKER_BUILD_CTX;

	// Cria diretório para armazenar a chave
	sh "mkdir " + buildContext + " || rm -f " + buildContext + "/*"

	// Copia chave e Dockerfile para contexto de build
	sh "cp " + ansibleKeypath + " " + buildContext + "/" + sshKeyFile
	sh "echo -n '" + libraryResource('Dockerfile_ecs') + "' > " + buildContext + "/Dockerfile"
}