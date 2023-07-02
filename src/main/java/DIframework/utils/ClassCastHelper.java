package DIframework.utils;

@FunctionalInterface
public interface ClassCastHelper {
    <T> T parse(String value,Class<T> clazz);
}
