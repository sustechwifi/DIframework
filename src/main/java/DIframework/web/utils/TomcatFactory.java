package DIframework.web.utils;

import DIframework.web.impl.DispatcherServlet;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.filters.CorsFilter;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.LinkedHashMap;

public class TomcatFactory {
    private int port;

    private Tomcat container;

    private DispatcherServlet dispatcherServlet;

    private final String dispatcherName = "dispatcherServlet";

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Tomcat getContainer() {
        return container;
    }

    public TomcatFactory(TomcatConfig config, DispatcherServlet dispatcherServlet) {
        this.port = config.getPort();
        this.dispatcherServlet = dispatcherServlet;
    }

    public void init() throws LifecycleException, IOException {
        // 创建临时目录作为tomcat的基础目录
        Path tempBaseDir = Files.createTempDirectory("tomcat-temp-base-dir");
        // 创建临时目录作为应用文档资源的目录
        Path tempDocDir = Files.createTempDirectory("tomcat-temp-doc-dir");

        Tomcat tomcat = new Tomcat();
        Connector connector = new Connector();
        // 设置绑定端口
        connector.setPort(port);
        tomcat.getService().addConnector(connector);
        tomcat.setConnector(connector);
        tomcat.getHost().setAutoDeploy(false);
        tomcat.setBaseDir(tempBaseDir.toFile().getAbsolutePath());


        // 创建应用上下文
        StandardContext context = (StandardContext) tomcat.addWebapp("/", tempDocDir.toFile().getAbsolutePath());
        context.setParentClassLoader(TomcatFactory.class.getClassLoader());
        context.setUseRelativeRedirects(false);

        container = tomcat;

        // 添加servlet
        Tomcat.addServlet(context, dispatcherName, dispatcherServlet);
        context.addServletMappingDecoded("/*", dispatcherName);

        Runnable r = () -> {
            try {
                tomcat.start();
                tomcat.getServer().await();
            } catch (LifecycleException e) {
                throw new RuntimeException(e);
            }
        };
        new Thread(r).start();
    }
}
