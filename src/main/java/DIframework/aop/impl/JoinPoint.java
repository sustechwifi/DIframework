package DIframework.aop.impl;

import java.lang.reflect.Method;
import java.util.Arrays;


public class JoinPoint {

    private Method method;

    private Object target;

    private Object[] args;

    public JoinPoint(Method method, Object target, Object... args) {
        this.method = method;
        this.args = args;
        this.target = target;
    }

    public Object[] getArgs(){
        return args;
    }

    public Method getSignature(){
        return method;
    }

    public Object getTarget() {
        return target;
    }


    @Override
    public String toString() {
        return "JoinPoint{" +
                "method=" + method +
                ", target=" + target +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
