package DIframework.core.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultBeanDefinition extends AbstractBeanDefinition {
    private ClassLoader beanLoader;
    private Field[] autoWiredFields;
    private Field[] privateFields;
    private Method[] autoWiredMethods;
    private Constructor<?>[] autoWiredConstructors;
    private Constructor<?> defaultConstructor;

    private Object[] defaultConstructArgs;
    private String beanName;
    private Class<?> beanClass;
    private List<Object> argsBuffer = new ArrayList<>();

    public void setDefaultConstructor(Constructor<?> defaultConstructor) {
        this.defaultConstructor = defaultConstructor;
    }

    public Object[] getDefaultConstructArgs() {
        return defaultConstructArgs;
    }

    public void setDefaultConstructArgs(Object[] defaultConstructArgs) {
        this.defaultConstructArgs = defaultConstructArgs;
    }

    @Override
    public Class<?> getBeanClass() {
        return beanClass;
    }

    @Override
    public String getBeanName() {
        return beanName;
    }

    public void addProperties (Object ...args){
        argsBuffer.addAll(Arrays.stream(args).toList());
    }

    public void pushProperty(Object arg){
        argsBuffer.add(arg);
    }

    public void popProperty(){
        if (!argsBuffer.isEmpty()){
            argsBuffer.remove(argsBuffer.size() - 1);
        }
    }

    @Override
    public Object[] args() {
        return argsBuffer.toArray(Object[]::new);
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public ClassLoader getBeanLoader() {
        return beanLoader;
    }

    public Field[] getAutoWiredFields() {
        return autoWiredFields;
    }

    public Method[] getAutoWiredMethods() {
        return autoWiredMethods;
    }

    public Constructor[] getAutoWiredConstructors() {
        return autoWiredConstructors;
    }

    public boolean isSingleton() {
        return singleton;
    }

    private boolean singleton;

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    public void setBeanName(String name) {
        this.beanName = name;
    }

    public Field[] getPrivateFields() {
        return privateFields;
    }

    public void setPrivateFields(Field[] privateFields) {
        this.privateFields = privateFields;
    }

    public void setBeanLoader(ClassLoader beanLoader) {
        this.beanLoader = beanLoader;
    }

    public void setAutoWiredFields(Field[] autoWiredFields) {
        this.autoWiredFields = autoWiredFields;
    }

    public void setAutoWiredMethods(Method[] autoWiredMethods) {
        this.autoWiredMethods = autoWiredMethods;
    }

    public void setAutoWiredConstructors(Constructor[] autoWiredConstructors) {
        this.autoWiredConstructors = autoWiredConstructors;
    }

    public Constructor getDefaultConstructor() {
        return defaultConstructor;
    }

    @Override
    public ClassLoader getClassLoader() {
        return beanLoader;
    }

    @Override
    public Field[] autoWiredFields() {
        return autoWiredFields;
    }

    @Override
    public Method[] autoWiredMethods() {
        return autoWiredMethods;
    }


    @Override
    public boolean isSingleton(String var1) throws Throwable {
        if (var1.equals(beanName) && singleton){
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "DefaultBeanDefinition{" +
                "beanName='" + beanName + '\'' +
                ", beanClass=" + beanClass +
                ", singleton=" + singleton +
                '}';
    }
}
