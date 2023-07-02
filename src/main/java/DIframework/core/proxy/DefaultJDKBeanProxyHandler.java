package DIframework.core.proxy;

import DIframework.aop.impl.AspectJAutoProxyFactory;
import DIframework.core.annotation.Asynchronous;
import DIframework.core.annotation.AutoWired;
import DIframework.core.impl.AbstractAutowireCapableBeanFactory;
import DIframework.core.impl.DefaultBeanDefinition;
import DIframework.utils.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class DefaultJDKBeanProxyHandler extends AbstractBeanProxyHandler implements InvocationHandler {

    DefaultBeanDefinition definition;

    public DefaultJDKBeanProxyHandler(Object target,
                                      AbstractAutowireCapableBeanFactory factory,
                                      AspectJAutoProxyFactory aspectProxyFactory,
                                      DefaultBeanDefinition definition) {
        super(target, factory, aspectProxyFactory, definition);
        this.definition = definition;
    }


    @Override
    public Object doProxy() {
        return Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), this);
    }


    private Object handleJDKAuWiredMethod(Method method, Object[] args) throws Throwable {
        if (method.isAnnotationPresent(AutoWired.class)) {
            return method.invoke(target, adaptAuWiredMethodArgs(method, args));
        } else {
            return method.invoke(target, args);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        if (method.isAnnotationPresent(Asynchronous.class)) {
            if (method.getReturnType() != void.class){
                Log.error("异步方法的返回值应该为 void 或 配置切面进行回调!");
            }
            super.performAsyncTask(() -> {
                try {
                    injectBeforeAdvices(method, target, args);
                    handleJDKAuWiredMethod(method, args);
                    injectAfterAdvices(method, target, args);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
            return null;
        } else {
            injectBeforeAdvices(method, target, args);
            result = handleJDKAuWiredMethod(method, args);
        }
        injectAfterAdvices(method, target, args);
        return result;
    }
}
