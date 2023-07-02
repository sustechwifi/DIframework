package DIframework.web.utils;

import DIframework.core.interfaces.ApplicationContext;

public class TomcatConfig {
    private int port;
    private String info;


    public TomcatConfig(int port, String info) {
        this.port = port;
        this.info = info;
    }

    public static TomcatConfig readConfig(ApplicationContext context) throws Throwable {
        var webConfig = new TomcatConfig(8080, "web app started");
        if (context.containsBean("port")) {
            webConfig.setPort(Integer.parseInt((String) context.getBean("port")));
        }
        return webConfig;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
