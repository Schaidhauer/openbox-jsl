def call(Map params) {
    sh "echo '" + libraryResource('Dockerfile') + "' | " +
       "docker build --rm " +
       "--build-arg SSH_PRIVATE_KEY='" + params.sshGitKey + "' " +
       "--no-cache -t ansible-docker:latest -"

    sh "docker run --rm ansible-docker:latest ansible-playbook " + 
       "/ansible/" + params.playbook + "--extra-vars '{"
       "deploy: "  + params.deploy + "," +
       "ec2_access_key: " + params.accessKey + "," +
       "ec2_secret_key: " + params.secretKey + "}'"
}
