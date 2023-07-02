package DIframework.web.utils;

public enum HttpMethod {
    GET,POST,DELETE,PUT;


    @Override
    public String toString() {
        switch (this) {
            case GET -> {
                return "GET";
            }
            case POST -> {
                return "POST";
            }
            case DELETE -> {
                return "DELETE";
            }
            case PUT -> {
                return "PUT";
            }
            default -> {
                return "NONE";
            }
        }
    }
}
