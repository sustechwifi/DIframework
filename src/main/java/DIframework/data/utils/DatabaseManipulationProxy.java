package DIframework.data.utils;


import DIframework.data.annotation.CustomMapper;
import DIframework.data.annotation.Para;
import DIframework.data.annotation.Select;
import DIframework.data.annotation.Update;

import java.lang.reflect.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DatabaseManipulationProxy implements InvocationHandler {

    private final DataAdapter adapter = new DataAdapter();

    public <T, I> I getMapper(Class<T> clazz, Class<I> $interface) {
        return (I) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{$interface}, this);
    }

    private static Object[] sortArgs(String sql, Parameter[] params, Object[] args) {
        String regex = "#\\{(\\w+)}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sql);
        List<Object> res = new ArrayList<>();
        P:
        while (matcher.find()) {
            String match = matcher.group(1);
            for (int i = 0; i < params.length; i++) {
                if (params[i].isAnnotationPresent(Para.class)) {
                    if (params[i].getAnnotation(Para.class).value().equals(match)) {
                        res.add(args[i]);
                        continue P;
                    }
                }
            }
            throw new IllegalArgumentException("方法参数中找不到语句中的变量:" + match);
        }
        return res.toArray();
    }


    private static Function<SqlResult, ?> findCustomMapperFunction(Parameter[] params, Object[] args) {
        for (int i = 0; i < params.length; i++) {
            if (params[i].isAnnotationPresent(CustomMapper.class)) {
                return (Function<SqlResult, ?>) args[i];
            }
        }
        throw new IllegalArgumentException("格式错误");
    }

    private static Class<?> getGenericReturnTypeClass(Method method) {
        Type returnType = method.getGenericReturnType();
        if (returnType instanceof ParameterizedType parameterizedType) {
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length > 0) {
                Type actualTypeArgument = actualTypeArguments[0];
                if (actualTypeArgument instanceof Class) {
                    return (Class<?>) actualTypeArgument;
                }
            }
        }
        return null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        try {
            var returnType = method.getReturnType();
            if (method.isAnnotationPresent(Update.class)) {
                Update update = method.getAnnotation(Update.class);
                String sql = update.value();
                return SqlFactory.handleUpdate(sql, sortArgs(sql, method.getParameters(), args));
            } else if (method.isAnnotationPresent(Select.class)) {
                Select annotation = method.getAnnotation(Select.class);
                String sql = annotation.value();
                boolean aggregated = annotation.aggregated();
                boolean customMap = annotation.customMap();
                if (customMap) {
                    var func = Arrays.stream(method.getParameters()).filter(i -> i.isAnnotationPresent(CustomMapper.class)).findFirst();
                    if (func.isEmpty()) {
                        throw new IllegalArgumentException("缺少 @CustomMap注解");
                    } else if (func.get().getType() != Function.class) {
                        throw new IllegalArgumentException("@CustomMap注解 只能用于 Function 接口");
                    } else {
                        var function = findCustomMapperFunction(method.getParameters(), args);
                        var sortedArgs = sortArgs(sql, method.getParameters(), args);
                        if (aggregated) {
                            return SqlFactory.handleSingleResult(SqlFactory.handleQuery(sql, sortedArgs), function);
                        } else {
                            return SqlFactory.handleMultipleResult(SqlFactory.handleQuery(sql, sortedArgs), function);
                        }
                    }
                } else {
                    if (!adapter.supportReturnType(returnType)) {
                        var msg = String.format("方法 %s 返回了不支持的返回类型: %s", method.getName(), returnType.getName());
                        throw new IllegalArgumentException(msg);
                    }
                    var sortedArgs = sortArgs(sql, method.getParameters(), args);
                    if (aggregated) {
                        return SqlFactory.handleSingleResult(SqlFactory.handleQuery(sql, sortedArgs), adapter.getMappingFunction(returnType));
                    } else {
                        returnType = getGenericReturnTypeClass(method);
                        if (returnType == null) {
                            var msg = String.format("无法解析方法 %s 返回值中的泛型", method.getName());
                            throw new IllegalArgumentException(msg);
                        }
                        return SqlFactory.handleMultipleResult(SqlFactory.handleQuery(sql, sortedArgs), adapter.getMappingFunction(returnType));
                    }
                }
            }
        } catch (SQLException e) {
            var msg = String.format("在调用方法:%s 时发生错误\n", method.getName());
            throw new RuntimeException(msg + e.getMessage());
        }
        try {
            return method.invoke(proxy, args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
