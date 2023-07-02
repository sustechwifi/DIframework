package DIframework.web.impl;

import java.lang.reflect.Method;

public class HandlerMethod {

    private Method method;

    private Object bean;

    public Method getMethod() {
        return method;
    }

    public Object getBean() {
        return bean;
    }

    public HandlerMethod(Method method, Object bean) {
        this.method = method;
        this.bean = bean;
    }
}
