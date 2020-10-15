package openbox.ansible;

def start(Map params)
{
    // Configura ambiente para realizar deploy
    configure(params.accessKey, params.secretKey, params.sshGitKey, params.sshRepoKey, params.keyname);
    appDeploy = new ApplicationDeployConfig();
    sh appDeploy.getBuildCmd(params.sshRepoKey, params.keyname);
    sh appDeploy.getRunCmd(
        params.accessKey,
        params.secretKey, 
        params.ecrPassword,
        params.service,
        params.image,
        params.keyname,
        params.repository,
        params.branch
    );

}

def configure(String ansibleKeypath, String repositoryKeypath, String repositoryKeyname)
{
	sshKeyFile = ApplicationDeployConfig.ANSIBLE_KEY_FILE;
    buildContext = ApplicationDeployConfig.DOCKER_BUILD_CTX;

	// Cria diretório para armazenar a chave
	sh "mkdir " + buildContext + " || rm -f " + buildContext + "/*"

	// Copia chave e Dockerfile para contexto de build
	sh "cp " + ansibleKeypath + " " + buildContext + "/" + sshKeyFile
	sh "echo -n '" + libraryResource('Dockerfile_ecs') + "' > " + buildContext + "/Dockerfile"
    sh "cp " + repositoryKeypath + " " + buildContext + "/" + repositoryKeyname  + " && chmod 600 " + buildContext + "/" + repositoryKeyname
}

return this;