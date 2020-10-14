package openbox.ansible;

class DockerStepAssembler
{
    public static String assembleDockerBuild(String imageName, Map buildArgs, String context)
    {
        String buildArg = "docker build ";
        for (arg in buildArgs)
            buildArg += ("--build-arg " + arg.key + "=" + arg.value) + " ";

        buildArg += "-f " + context + "/Dockerfile " +
                    "-t " + imageName + " " + context;

        return buildArg;
    }

    public static String assembleDockerRun(String imageName, Map volumes, String command)
    {
        String runArg = "docker run ";
        for (vol in volumes)
            runArg += ("--volume " + vol.key + ":" + vol.value) + " ";
        
        runArg += imageName + " " command;
        return runArg;
    }
}