package DIframework.core.impl;

import DIframework.core.interfaces.ApplicationEvent;
import DIframework.core.interfaces.BeanFactory;

public class DefaultApplicationEvent implements ApplicationEvent {
    private String message;
    private BeanFactory beanFactory;


    public DefaultApplicationEvent(String message) {
        this.message = message;
    }

    public DefaultApplicationEvent(String message, BeanFactory beanFactory) {
        this.message = message;
        this.beanFactory = beanFactory;
    }

    @Override
    public String getMessage() {
        return message;
    }


    public BeanFactory getBeanFactory() {
        return beanFactory;
    }
}
