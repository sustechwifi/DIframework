package DIframework.data.utils;

import DIframework.utils.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Function;

public class DataAdapter {

    String adaptSql(String sql) {
        String s = sql.replaceAll("#\\{\\w+\\d*\\w*}", "?");
        return s;
    }


    /*
     * 检查是否为支持的返回值类型
     */
    boolean supportReturnType(Class<?> cls) {
        if (cls.isPrimitive()
                || cls == String.class
                || cls == Boolean.class
                || Number.class.isAssignableFrom(cls)
                || cls.isRecord()
                || Collection.class.isAssignableFrom(cls)) return true;
        Field[] declaredFields = cls.getDeclaredFields();
        // 获取参数最多的构造器
        var c = Arrays.stream(cls.getDeclaredConstructors())
                .max(Comparator.comparingInt(Constructor::getParameterCount));
        if (c.isEmpty()){
            String msg = String.format("返回值缺少构造器 %s",cls);
            Log.error(msg);
            return false;
        }
        return true;
    }

    private Function<SqlResult, ?> getMappingFunctionFromConstructor(Constructor<?> con) {
        return sqlResult -> {
            var args = new Object[con.getParameterCount()];
            for (int i = 0; i < con.getParameterCount(); i++) {
                args[i] = read(sqlResult, con.getParameters()[i].getType(), i + 1);
            }
            try {
                return con.newInstance(args);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };
    }

    Function<SqlResult, ?> getMappingFunction(Class<?> cls) {
        if (cls.isPrimitive() || cls == String.class) {
            return sqlResult -> read(sqlResult, cls);
        } else if (cls.isRecord()) {
            return getMappingFunctionFromConstructor(cls.getDeclaredConstructors()[0]);
        } else {
            // 获取参数最多的构造器
            var c = Arrays.stream(cls.getDeclaredConstructors())
                    .max(Comparator.comparingInt(Constructor::getParameterCount));
            if (c.isEmpty()){
                var msg = cls + " 缺少构造器";
                throw new IllegalArgumentException(msg);
            }
            return getMappingFunctionFromConstructor(c.get());
        }
    }

    void write(PreparedStatement p, Object w, int index) throws SQLException {
        if (w == null){
            p.setNull(index,Types.NULL);
        }
        else if (w instanceof Integer) {
            p.setInt(index, (Integer) w);
        } else if (w instanceof String) {
            p.setString(index, (String) w);
        } else if (w instanceof Long) {
            p.setLong(index, (long) w);
        } else if (w instanceof Double) {
            p.setDouble(index, (double) w);
        } else if (w instanceof Boolean) {
            p.setBoolean(index, (boolean) w);
        }
    }

    <O> Object read(SqlResult resultSet, Class<O> returnType) {
        return read(resultSet, returnType, 1);
    }


    <O> Object read(SqlResult resultSet, Class<O> returnType, int index) {
        if (returnType.equals(String.class)) {
            return resultSet.getString(index);
        } else if (returnType.equals(int.class) || returnType.equals(Integer.class)
                || returnType.equals(short.class) || returnType.equals(Short.class)
                || returnType.equals(byte.class) || returnType.equals(Byte.class)
        ) {
            return resultSet.getInt(index);
        } else if (returnType.equals(double.class) || returnType.equals(Double.class)
                || returnType.equals(float.class) || returnType.equals(Float.class)
        ) {
            return resultSet.getDouble(index);
        } else if (returnType.equals(boolean.class) || returnType.equals(Boolean.class)) {
            return resultSet.getBoolean(index);
        } else if (returnType.equals(long.class) || returnType.equals(Long.class)) {
            return resultSet.getLong(index);
        } else {
            return null;
        }
    }

}
