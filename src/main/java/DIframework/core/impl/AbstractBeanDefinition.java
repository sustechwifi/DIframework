package DIframework.core.impl;

import DIframework.core.interfaces.BeanDefinition;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class AbstractBeanDefinition implements BeanDefinition {
    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public Field[] autoWiredFields() {
        return new Field[0];
    }

    @Override
    public Method[] autoWiredMethods() {
        return new Method[0];
    }

    @Override
    public Object getBean(String var1) throws Throwable {
        return null;
    }

    @Override
    public <T> T getBean(Class<T> var1) throws Throwable {
        return null;
    }

    @Override
    public boolean containsBean(String var1) {
        return false;
    }

    @Override
    public boolean isSingleton(String var1) throws Throwable {
        return false;
    }

    @Override
    public Class<?> getType(String var1) throws Throwable {
        return null;
    }
}
