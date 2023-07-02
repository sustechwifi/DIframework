package DIframework.data.utils;

import DIframework.core.interfaces.ApplicationContext;

import java.security.InvalidParameterException;

public class DataSourceConfig {

    public static DataSource readConfig(ApplicationContext context) throws Throwable {
        if (context == null) {
            throw new NullPointerException("context 不能为 null");
        }
        if (context.containsBean("Datasource.url")
                && context.containsBean("Datasource.username")
                && context.containsBean("Datasource.password")) {
            return new DataSource(
                    (String) context.getBean("Datasource.url"),
                    (String) context.getBean("Datasource.username"),
                    (String) context.getBean("Datasource.password")
            );
        }else {
            throw new InvalidParameterException(
                    "数据库配置不完整，缺少以下的一个或多个配置：Datasource.url, Datasource.username, Datasource.password"
            );
        }
    }

    public static void connect(String[] otherConfigs, DataSource datasource) {
        if (datasource == null) return;
        JdbcUtil.connect(
                datasource.getUrl(),
                datasource.getUsername(),
                datasource.getPassword(),
                otherConfigs);
    }
}
