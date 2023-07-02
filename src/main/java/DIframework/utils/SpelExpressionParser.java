package DIframework.utils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpelExpressionParser {

    // 匹配 execution(public user.controller.HelloController.register(*))

    private static final Pattern SPEL_PATTERN = Pattern.compile("execution\\(public (.*)\\)");

    private static final Pattern pattern = Pattern.compile("^([^.]+(\\.[^.]+)*)\\.(.*)\\((.*)\\)$");

    public static Class<?> target = null;

    public static boolean match(Method method,Class<?> clazz,String expression) throws ClassNotFoundException {
        Matcher matcher = SPEL_PATTERN.matcher(expression);
        if (matcher.matches()) {
            String methodSignature = matcher.group(1);
            Matcher classMatcher = pattern.matcher(methodSignature.trim());
            if (!classMatcher.matches()){
                throw new IllegalArgumentException("类名或方法不匹配");
            }
            String className = classMatcher.group(1);
            Class<?> targetClass = Class.forName(className);
            String methodName = classMatcher.group(3);
            var res = Arrays.stream(targetClass.getDeclaredMethods()).filter(
                    i -> i.getName().equals(methodName)
            ).findFirst();
            if (res.isEmpty()){
                Log.error("找不到方法："+methodName);
            }
            return method.getName().equals(methodName) && clazz.getName().contains(className);
        } else {
            throw new IllegalArgumentException("Invalid spel expression: " + expression);
        }
    }
}
