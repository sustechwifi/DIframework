package DIframework.core.impl;

import DIframework.core.interfaces.ApplicationContext;

public abstract class AbstractApplicationContext implements ApplicationContext {
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
