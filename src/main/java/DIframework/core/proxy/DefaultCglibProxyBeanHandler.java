package DIframework.core.proxy;

import DIframework.aop.impl.AspectJAutoProxyFactory;
import DIframework.core.annotation.Asynchronous;
import DIframework.core.annotation.AutoWired;
import DIframework.core.impl.AbstractAutowireCapableBeanFactory;
import DIframework.core.impl.DefaultBeanDefinition;
import DIframework.utils.Log;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class DefaultCglibProxyBeanHandler extends AbstractBeanProxyHandler implements MethodInterceptor {

    DefaultBeanDefinition definition;

    public DefaultCglibProxyBeanHandler(Object target,
                                        AbstractAutowireCapableBeanFactory factory,
                                        AspectJAutoProxyFactory aspectProxyFactory,
                                        DefaultBeanDefinition definition) {
        super(target, factory, aspectProxyFactory, definition);
        this.definition = definition;
    }

    private Object handleCglibAuWiredMethod(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        if (method.isAnnotationPresent(AutoWired.class)) {
            return methodProxy.invokeSuper(o, adaptAuWiredMethodArgs(method, objects));
        } else {
            return methodProxy.invokeSuper(o, objects);
        }
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Object result;
        if (method.isAnnotationPresent(Asynchronous.class)) {
            if (method.getReturnType() != void.class){
                Log.error("异步方法的返回值应该为 void 或 配置切面进行回调!");
            }
            super.performAsyncTask(() -> {
                try {
                    injectBeforeAdvices(method, o, objects);
                    handleCglibAuWiredMethod(o, method, objects, methodProxy);
                    injectAfterAdvices(method, o, objects);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
            return null;
        } else {
            injectBeforeAdvices(method, o, objects);
            result = handleCglibAuWiredMethod(o, method, objects, methodProxy);
        }
        injectAfterAdvices(method, o, objects);
        return result;
    }

    @Override
    public Object doProxy() {
        Enhancer enhancer = new Enhancer();
        enhancer.setCallback(this);
        enhancer.setSuperclass(target.getClass());
        if (definition.getDefaultConstructArgs() == null) {
            return enhancer.create();
        } else {
            // 使用父类的构造器 构造代理子类实例
            return enhancer.create(
                    definition.getDefaultConstructor().getParameterTypes(),
                    definition.getDefaultConstructArgs()
            );
        }
    }
}
