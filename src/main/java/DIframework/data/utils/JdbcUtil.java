package DIframework.data.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;


public class JdbcUtil {
    private JdbcUtil() {
    }

    private static Connection connection;

    public static Connection getConnection() {
        return connection;
    }


    public static void connect(String url, String username, String password, String[] otherConfigs) {
        try {
            connection = DriverManager.getConnection(url, username, password);
            if (otherConfigs != null) {
                PreparedStatement ps;
                for (String config : otherConfigs) {
                    ps = connection.prepareStatement(config);
                    ps.executeUpdate();
                }
            }
            connection.setAutoCommit(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}



