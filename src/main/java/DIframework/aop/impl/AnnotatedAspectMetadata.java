package DIframework.aop.impl;

import DIframework.aop.interfaces.AspectMetadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AnnotatedAspectMetadata implements AspectMetadata {

    private int priority;

    private String name;
    private Class<?> aspectClass;
    private List<Method> aspectMethods = new ArrayList<>();

    public void setName(String name) {
        this.name = name;
    }

    public void setAspectClass(Class<?> aspectClass) {
        this.aspectClass = aspectClass;
    }

    @Override
    public Class<?> getAspectClass() {
        return aspectClass;
    }

    @Override
    public String getAspectName() {
        return name;
    }

    @Override
    public List<Method> getAspectMethods() {
        return aspectMethods;
    }

    @Override
    public List<Method> getAspectMethods(Class<? extends Annotation> annotationType) {
        return aspectMethods.stream()
                .filter(i -> i.isAnnotationPresent(annotationType))
                .collect(Collectors.toList());
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void addAspectMethod(Method method){
        aspectMethods.add(method);
    }
}
