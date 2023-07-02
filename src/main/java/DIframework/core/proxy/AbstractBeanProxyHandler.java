package DIframework.core.proxy;

import DIframework.aop.impl.AspectJAutoProxyFactory;
import DIframework.aop.impl.MethodAdvice;
import DIframework.aop.interfaces.Advice;
import DIframework.core.annotation.Qualified;
import DIframework.core.impl.AbstractAutowireCapableBeanFactory;
import DIframework.core.interfaces.BeanDefinition;
import DIframework.utils.Log;
import DIframework.utils.OperationConstants;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractBeanProxyHandler {

    protected Object target;
    protected AbstractAutowireCapableBeanFactory factory;
    protected AspectJAutoProxyFactory aspectProxyFactory;

    protected BeanDefinition beanDefinition;

    protected ExecutorService executorService = Executors.newFixedThreadPool(10);

    protected void performAsyncTask(Runnable task) {
        executorService.submit(task);
    }

    protected void shutdown() {
        executorService.shutdown();
    }


    public AbstractBeanProxyHandler(Object target,
                                    AbstractAutowireCapableBeanFactory beanFactory,
                                    AspectJAutoProxyFactory aspectProxyFactory,
                                    BeanDefinition beanDefinition) {
        this.target = target;
        this.factory = beanFactory;
        this.aspectProxyFactory = aspectProxyFactory;
        this.beanDefinition = beanDefinition;
    }

    public abstract Object doProxy();

    protected List<Advice> getMatchedAdvices(Method method,Class<?> clazz,int type){
        return aspectProxyFactory.getAdvice(method, clazz,type);
    }

    protected void injectBeforeAdvices(Method method,Object target,Object[] args) {
        var beforeAdvices = getMatchedAdvices(method,target.getClass(), OperationConstants.AOP_BEFORE);
        for (var before : beforeAdvices){
            ((MethodAdvice)before).before(method,args,target);
        }
    }

    protected void injectAfterAdvices(Method method,Object target,Object[] args) {
        var afterAdvices = getMatchedAdvices(method,target.getClass(), OperationConstants.AOP_AFTER);
        for (var after : afterAdvices){
            ((MethodAdvice)after).after(method,args,target);
        }
    }


    public Object[] adaptAuWiredMethodArgs(Method method, Object[] objects) {
        Object[] newArgs = new Object[objects.length];
        int cnt = 0;
        for(var arg : method.getParameters()){
            Object bean;
            Object tmp;
            try {
                if (arg.isAnnotationPresent(Qualified.class)){
                    bean = factory.getBean(arg.getAnnotation(Qualified.class).value());
                }else {
                    bean = factory.getBean(arg.getType());
                }
                tmp = bean;
                newArgs[cnt++] = tmp;
            } catch (Throwable e) {
                var msg = String.format("方法参数注入时发生错误 %s",arg.getName());
                Log.error(msg);
                throw new RuntimeException(msg);
            }
        }
        return newArgs;
    }

}
