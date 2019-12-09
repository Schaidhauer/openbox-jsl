def call(Map params) {
    sh 'echo "' + script.libraryResource('Dockerfile')  +'" | docker build --no-cache -t ansible-docker:latest -'
    sh 'docker run --rm ansible-docker:latest ansible-playbook ' + 
       '/ansible/' + params.playbook + '--extra-vars "{'
       'deploy: ' + params.deploy + ',' +
       'ec2_access_key: ' + params.accessKey + ',' +
       'ec2_secret_key: ' + params.secretKey + '}"'
}

/**
 * Generates a path to a temporary file location, ending with {@code path} parameter.
 *
 * @param path path suffix
 * @return path to file inside a temp directory
 */
@NonCPS
String createTempLocation(String path) {
    String tmpDir = script.pwd tmp: true
    return tmpDir + File.separator + new File(path).getName()
}

/**
 * Returns the path to a temp location of a script from the global library (resources/ subdirectory)
 *
 * @param srcPath path within the resources/ subdirectory of this repo
 * @param destPath destination path (optional)
 * @return path to local file
 */
String copyGlobalLibraryScript(String srcPath, String destPath = null) {
    destPath = destPath ?: createTempLocation(srcPath)
    script.writeFile file: destPath, text: script.libraryResource(srcPath)
    script.echo "copyGlobalLibraryScript: copied ${srcPath} to ${destPath}"
    return destPath
}
