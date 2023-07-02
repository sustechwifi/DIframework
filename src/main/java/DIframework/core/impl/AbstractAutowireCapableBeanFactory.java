package DIframework.core.impl;

import DIframework.core.interfaces.BeanDefinition;
import DIframework.core.interfaces.BeanFactory;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractAutowireCapableBeanFactory implements BeanFactory {

    /**
     *  三级缓存，分别保存 BeanDefinition 、未初始化Bean、 和已注入完成的单例Bean。
     */
    protected Map<String, BeanDefinition> beanFactories = new HashMap<>();
    protected Map<String,Object> earlySingletonObjects = new HashMap<>();
    protected Map<String,Object> singletonObjects = new HashMap<>();

    protected void addBean(DefaultBeanDefinition beanDefinition){
        beanFactories.put(FACTORY_BEAN_PREFIX+beanDefinition.getBeanName(),beanDefinition);
    }
    public abstract Object doInitializeBean(String beanName, BeanDefinition mbd, Object[] args) throws Throwable;
    public abstract Object doCreateBean(BeanDefinition definition);

    public abstract Object createPrototypeBean(BeanDefinition definition) throws Throwable;

    public abstract BeanDefinition getDefinition(Class<?> clazz) throws Throwable;
    public abstract BeanDefinition getDefinition(String clazz) throws Throwable;
}
