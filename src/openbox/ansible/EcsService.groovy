package openbox.ansible;

class EcsService
{
    static String DEFAULT_LAUNCH_TYPE = 'FARGATE';

    static String[] VALID_LAUNCH_TYPES = ['FARGATE', 'EC2'];

    static String[] VALID_SERVICES = ["site-plataforma"];

    private String name;
    private String cluster;
    private String task;
    private String launch_type;
    private int desired_count;
    
    private Boolean useLoadBalancer;
    private String lb_arn_tg;
    private String lb_http_container;
    private String lb_http_port;

    private String vpc; 
    private String subnet;
    private String security_group;

    public static EcsService get(String params)
    {

    }

    private EcsService(String name, String cluster, String task, String vpc, String subnet, String security_group)
    {
        this.name = name;
        this.cluster = cluster;
        this.task = task;

        this.vpc = vpc;
        this.subnet = subnet;
        this.security_group = security_group;

        this.desired_count = 1;
        this.launch_type = EcsService.DEFAULT_LAUNCH_TYPE;
        this.useLoadBalancer = false;
    }

    public void configureLoadBalancer(String lb_arn_tg, String lb_http_container, String lb_http_port)
    {
        this.lb_arn_tg = lb_arn_tg;
        this.lb_http_container = lb_http_container;
        this.lb_http_port = lb_http_port;
        this.useLoadBalancer = true;    
    }

    public String getName()
    {
        return this.name;
    }

    public String getCluster()
    {
        return this.cluster;
    }

    public String getTask()
    {
        return this.task;
    }

    public String getLaunchType()
    {
        return this.launch_type;
    }

    public String getDesiredCount()
    {
        return this.desired_count;
    }

    public String getVpc()
    {
        return this.vpc;
    }

    public String getSubnet()
    {
        return this.subnet;
    }

    public String getSecurityGroup()
    {
        return this.security_group;
    }

    public void setTask(String task)
    {
        this.task = task;
    }

    public void setLaunchType(String launch_type)
    {
        if (!EcsService.VALID_LAUNCH_TYPES.contains(launch_type))
            throw new InvalidArgumentException('launch_type inválido');

        this.launch_type = launch_type;
    }

    public void setVpc(String vpc)
    {
        this.vpc = vpc;
    }

    public void setSubnet(String subnet)
    {
        this.subnet = subnet;
    }

    public void setSecurityGroup(String security_group)
    {
        this.security_group = security_group;
    }
}