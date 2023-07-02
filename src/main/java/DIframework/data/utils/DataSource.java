package DIframework.data.utils;

public class DataSource {

    private String url;
    private String username;
    private String password;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }


    public DataSource(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }
}
