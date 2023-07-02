package DIframework.core.impl;

import DIframework.aop.impl.AspectJAutoProxyFactory;
import DIframework.core.annotation.AutoWired;
import DIframework.core.annotation.ProxyTargetClass;
import DIframework.core.annotation.Qualified;
import DIframework.core.interfaces.BeanDefinition;
import DIframework.core.interfaces.BeanFactory;
import DIframework.core.proxy.AbstractBeanProxyHandler;
import DIframework.core.proxy.DefaultCglibProxyBeanHandler;
import DIframework.core.proxy.DefaultJDKBeanProxyHandler;

import java.lang.reflect.Field;
import java.security.InvalidParameterException;
import java.util.NoSuchElementException;

public class ListableFactory extends AbstractAutowireCapableBeanFactory {

    private final AspectJAutoProxyFactory aspectJAutoProxyFactory;

    public ListableFactory(AspectJAutoProxyFactory aspectJAutoProxyFactory) {
        this.aspectJAutoProxyFactory = aspectJAutoProxyFactory;
    }

    @Override
    public Object getBean(String var1) throws Throwable {
        if (var1.charAt(0) == BeanFactory.FACTORY_BEAN_PREFIX) {
            return beanFactories.get(var1);
        } else {
            if (singletonObjects.containsKey(var1)) {
                return singletonObjects.get(var1);
            }
            if (!beanFactories.containsKey(FACTORY_BEAN_PREFIX + var1)) {
                throw new NoSuchElementException("未知的bean: "+var1);
            }
            var definition = (DefaultBeanDefinition) beanFactories.get(FACTORY_BEAN_PREFIX + var1);
            if (earlySingletonObjects.containsKey(var1)) {
                var doneBean = doInitializeBean(var1, definition, definition.args());
                if (definition.isSingleton(definition.getBeanName())) {
                    singletonObjects.put(definition.getBeanName(), doneBean);
                }
                return doneBean;
            } else {
                Object res;
                if (definition.isSingleton(definition.getBeanName())) {
                    var earlyBean = doCreateBean(definition);
                    earlySingletonObjects.put(definition.getBeanName(), earlyBean);
                    res = doInitializeBean(definition.getBeanName(), definition, definition.args());
                    singletonObjects.put(definition.getBeanName(), res);
                } else {
                    res = createPrototypeBean(definition);
                }
                return res;
            }
        }
    }


    @Override
    public <T> T getBean(Class<T> var1) throws Throwable {
        var tmp = singletonObjects
                .entrySet()
                .stream()
                .filter(i -> i.getValue().getClass() == var1)
                .findFirst();
        if (tmp.isEmpty()) {
            // 尝试通过类名寻找
            var tmp2 = getBean(var1.getName());
            if (tmp2 != null){
                return (T) tmp2;
            }
            var definition = (DefaultBeanDefinition) getDefinition(var1);
            var beanName = definition.getBeanName();
            Object res;
            if (definition.isSingleton(beanName)) {
                Object bean = earlySingletonObjects.get(beanName);
                if (bean == null) {
                    bean = doCreateBean(definition);
                    earlySingletonObjects.put(beanName, bean);
                }
                res = doInitializeBean(beanName, definition, definition.args());
                singletonObjects.put(beanName, res);
            } else {
                res = createPrototypeBean(definition);
            }
            return (T) res;
        } else {
            return (T) tmp.get().getValue();
        }
    }

    /**
     * 是否存在已完成注入的单例 bean
     */

    @Override
    public boolean containsBean(String var1) {
        if (var1.charAt(0) == BeanFactory.FACTORY_BEAN_PREFIX) {
            return beanFactories.containsKey(var1);
        } else {
            return singletonObjects.containsKey(var1);
        }
    }

    @Override
    public boolean isSingleton(String var1) throws Throwable {
        if (var1.charAt(0) != BeanFactory.FACTORY_BEAN_PREFIX) {
            var1 = "&" + var1;
        }
        return beanFactories.get(var1).isSingleton(var1);
    }

    @Override
    public Class<?> getType(String var1) throws Throwable {
        return getBean(var1).getClass();
    }

    @Override
    public Object doCreateBean(BeanDefinition def) {
        // 通过代理设置方法参数注入
        AbstractBeanProxyHandler handler;
        Object bean;
        var definition  = (DefaultBeanDefinition) def;
        try {
            bean = definition.getDefaultConstructor().newInstance(definition.getDefaultConstructArgs());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (bean.getClass().getInterfaces().length == 0 || definition.getBeanClass().isAnnotationPresent(ProxyTargetClass.class)) {
            // 没有实现接口使用cglib代理
            handler = new DefaultCglibProxyBeanHandler(bean, this, aspectJAutoProxyFactory,definition);
        } else {
            // 否则使用 jdk 代理
            handler = new DefaultJDKBeanProxyHandler(bean, this, aspectJAutoProxyFactory,definition);
        }
        return handler.doProxy();
    }

    @Override
    public Object createPrototypeBean(BeanDefinition definition) throws Throwable {
        synchronized (aspectJAutoProxyFactory){
            var d = (DefaultBeanDefinition) definition;
            var early = doCreateBean(d);
            d.popProperty();
            d.pushProperty(early);
            return doInitializeBean(d.getBeanName(), d, d.args());
        }
    }

    @Override
    public BeanDefinition getDefinition(Class<?> clazz) {
        var entry = beanFactories
                .entrySet()
                .stream()
                .filter(i -> clazz == i.getValue().getBeanClass())
                .findFirst();
        if (entry.isEmpty()) {
            throw new InvalidParameterException("未注册的bean类型: " + clazz);
        } else {
            return entry.get().getValue();
        }
    }

    @Override
    public BeanDefinition getDefinition(String name) {
        var entry = beanFactories
                .entrySet()
                .stream()
                .filter(i -> name.equals(i.getValue().getBeanName()))
                .findFirst();
        if (entry.isEmpty()) {
            throw new InvalidParameterException("未注册的bean id: " + name);
        } else {
            return entry.get().getValue();
        }
    }

    @Override
    public Object doInitializeBean(String beanName, BeanDefinition mbd, Object[] args) throws Throwable {
        var definition = (DefaultBeanDefinition) mbd;
        if (containsBean(beanName)) {
            return getBean(beanName);
        }
        if (!beanName.equals(definition.getBeanName())) {
            throw new InvalidParameterException("参数错误！！");
        }
        Object bean = earlySingletonObjects.get(beanName);
        if (bean == null) {
            if (args.length == 0) {
                throw new InvalidParameterException("缺少待初始化的 bean");
            }
            // 多例 bean 的 earlyBean
            bean = args[0];
        }
        // 注入字段
        for (var field : definition.getAutoWiredFields()) {
            autoWireField(bean, field);
        }
        return bean;
    }


    private void autoWireField(Object bean, Field field) throws Throwable {
        field.setAccessible(true);
        var anno = (AutoWired) field.getAnnotation(AutoWired.class);
        //从容器中寻找单例对象注入,若不存在则尝试创建
        Object autoWired;
        try {
            //先根据注解值注入
            if (field.isAnnotationPresent(Qualified.class)){
                var name = field.getAnnotation(Qualified.class).value();
                autoWired = getBean(name);
                if (autoWired != null){
                    field.set(bean, autoWired);
                }else {
                    throw new NoSuchElementException("找不到指定的Bean Name: "+name);
                }
            }else {
                //再根据字段名注入
                autoWired = getBean(field.getName());
                if (autoWired != null){
                    field.set(bean, autoWired);
                }else {
                    throw new NoSuchElementException("无法根据字段名注入");
                }
            }
        } catch (NoSuchElementException e) {
            // 再根据类型注入
            autoWired = getBean(field.getType());
            field.set(bean, autoWired);
        } catch (Throwable e) {
            // 处理其他异常
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
