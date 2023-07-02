package DIframework.aop.impl;

import DIframework.aop.interfaces.Advice;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Consumer;


public class MethodAdvice implements Advice{

    private int type;
    private int priority;

    private String name;

    private Consumer<JoinPoint> beforeInvoke;
    private Consumer<JoinPoint> afterInvoke;

    public int getPriority() {
        return priority;
    }

    public MethodAdvice(int type, String name) {
        this.type = type;
        this.name = name;
    }


    @Override
    public int type() {
        return type;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setBeforeInvoke(Consumer<JoinPoint> beforeInvoke) {
        this.beforeInvoke = beforeInvoke;
    }

    public void setAfterInvoke(Consumer<JoinPoint> afterInvoke) {
        this.afterInvoke = afterInvoke;
    }

    public void before(Method method, Object[] args, Object target) {
        JoinPoint join = new JoinPoint(method, target, args);
        if (beforeInvoke != null)
            beforeInvoke.accept(join);
    }

    public void after(Method method, Object[] args, Object target) {
        JoinPoint join = new JoinPoint(method, target, args);
        if (afterInvoke != null)
            afterInvoke.accept(join);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodAdvice that = (MethodAdvice) o;
        return type == that.type && priority == that.priority && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, priority, name);
    }

    @Override
    public int compareTo(Advice o) {
        return this.getPriority() - o.getPriority();
    }
}