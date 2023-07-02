package DIframework.aop.interfaces;

import java.lang.reflect.Method;

public interface Pointcut {
    boolean matches(Method method, Class<?> targetClass);

    void setExpression(String expression);

    String getExpression();

    String name();
}
