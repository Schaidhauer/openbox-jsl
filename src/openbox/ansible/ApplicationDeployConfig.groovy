package openbox.ansible;

public class ApplicationDeployConfig
{
    static String ANSIBLE_KEY_FILE = "rsa.key";
    static String DOCKER_BUILD_CTX = "/var/jenkins_home/tmp";
    static String DOCKER_BUILD_IMG = "ansible-docker:latest";

    static String DOCKER_RUN_CMD = "ansible-playbook ecs.deploy.playbook.yml";

    private String cmd_build;
    private String cmd_run;

    ApplicationDeployConfig() {}

    public String getBuildCmd(String repositoryKeypath, String repositoryKeyname)
    {
        this.cmd_build = DockerStepAssembler.assembleDockerBuild(
            DOCKER_BUILD_IMG,
            [
                "ANSIBLE_SSH_PRIVATE_KEY_FILE": ApplicationDeployConfig.ANSIBLE_KEY_FILE,
                "REPO_SSH_PRIVATE_KEY_FILE": repositoryKeyname
            ],
            DOCKER_BUILD_CTX
        );
        return this.cmd_build;
    }

    public String getRunCmd(String awsAccessKey,
                            String awsSecretKey,
                            String ecrPassword,
                            String service,
                            String image,
                            String repositoryKeyname,
                            String repository,
                            String branch
    ) {
        this.cmd_run = DockerStepAssembler.assembleDockerRun(
            DOCKER_BUILD_IMG,
            DOCKER_RUN_CMD + ' --extra-vars "{' + 
            "ec2_access_key: " + awsAccessKey + "," +
            "ec2_secret_key: " + awsSecretKey + "," +
            "ecr_password: " +  ecrPassword + "," +
            "app_service: " + service + "," +
            "app_image: " + image + "," +
            "jenkins_key_name: " + repositoryKeyname + "," +
            "repository: " + repository + "," +
            "branch: " + branch + '}" -vvv'
        );
        return this.cmd_run;
    }
}