package DIframework.utils;

public class DefaultClassCastHelper implements ClassCastHelper {
    private Object stringToInt(String original) {
        return Integer.parseInt(original);
    }

    private Object stringToDouble(String original) {
        return Double.parseDouble(original);
    }

    private Object stringToLong(String original) {
        return Long.parseLong(original);
    }

    private Object stringToFloat(String original) {
        return Float.parseFloat(original);
    }

    private Object stringToBoolean(String original) {
        return Boolean.parseBoolean(original);
    }

    @Override
    public <T> T parse(String value, Class<T> clazz) {
        if (clazz == Integer.class || clazz == int.class) {
            return (T) stringToInt(value);
        } else if (clazz == Double.class || clazz == double.class) {
            return (T) stringToDouble(value);
        } else if (clazz == String.class) {
            return (T) value;
        } else if (clazz == long.class || clazz == Long.class) {
            return (T) stringToLong(value);
        } else if (clazz == boolean.class || clazz == Boolean.class) {
            return (T) stringToBoolean(value);
        } else if (clazz == float.class || clazz == Float.class) {
            return (T) stringToFloat(value);
        } else {
            throw new IllegalArgumentException("不支持的字符串转换");
        }
    }

}
