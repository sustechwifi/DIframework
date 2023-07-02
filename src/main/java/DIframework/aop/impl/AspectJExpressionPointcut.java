package DIframework.aop.impl;

import DIframework.aop.interfaces.Pointcut;
import DIframework.utils.Log;
import DIframework.utils.SpelExpressionParser;

import java.lang.reflect.Method;

public class AspectJExpressionPointcut implements Pointcut {

    private String name;
    private String expression;

    public AspectJExpressionPointcut(String expression) {
        this.expression = expression;
    }

    public AspectJExpressionPointcut() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        try {
            boolean match = SpelExpressionParser.match(method, targetClass, expression);
            return match;
        } catch (ClassNotFoundException e) {
            Log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public String getExpression() {
        return expression;
    }

    @Override
    public String name() {
        return name;
    }

}
