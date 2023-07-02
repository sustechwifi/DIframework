package DIframework.aop.impl;


import DIframework.aop.annotation.After;
import DIframework.aop.annotation.Before;
import DIframework.aop.interfaces.Advice;
import DIframework.aop.interfaces.AdviceRegisterFactory;
import DIframework.aop.interfaces.Pointcut;
import DIframework.utils.Log;
import DIframework.utils.OperationConstants;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 存放 pointcut adviceRegisterFactor的容器
 */
public class AspectJAutoProxyFactory {

    private final List<Pointcut> pointcuts = new ArrayList<>();

    private final List<AdviceRegisterFactory> registerFactories = new ArrayList<>();

    public void addPointCut(Pointcut pointcut) {
        pointcuts.add(pointcut);
    }

    public void addAdviceRegisterFactory(AdviceRegisterFactory registerFactory) {
        registerFactories.add(registerFactory);
    }

    private int calPriority(AdviceRegisterFactory factory,int t){
        return factory.getAspectMetadata().getPriority() * OperationConstants.AOP_ASPECT_BASE + t;
    }

    private void doRegister(AdviceRegisterFactory registerFactory) {
        for (var m : registerFactory.getAspectMetadata().getAspectMethods(Before.class)) {
            var anno = m.getAnnotation(Before.class);
            var name = anno.value();
            var pointcut = pointcuts.stream().filter(i -> i.name().equals(name)).findFirst();
            if (pointcut.isEmpty()) {
                Log.error("找不到切点：" + name);
                throw new RuntimeException("找不到切点：" + name);
            }
            var advice = new MethodAdvice(OperationConstants.AOP_BEFORE,name);
            advice.setPriority(calPriority(registerFactory,anno.priority()));
            advice.setBeforeInvoke(i -> {
                try {
                    var aspect = registerFactory.getAspectMetadata()
                            .getAspectClass()
                            .getDeclaredConstructor()
                            .newInstance();
                    m.invoke(aspect,i);
                } catch (Exception e) {
                    throw new RuntimeException("前置增强报错：" + e.getMessage());
                }
            });
            registerFactory.register(advice,pointcut.get());
        }
        for (var m : registerFactory.getAspectMetadata().getAspectMethods(After.class)) {
            var anno = m.getAnnotation(After.class);
            var name = anno.value();
            var pointcut = pointcuts.stream().filter(i -> i.name().equals(name)).findFirst();
            if (pointcut.isEmpty()) {
                Log.error("找不到切点：" + name);
                throw new RuntimeException("找不到切点：" + name);
            }
            var advice = new MethodAdvice(OperationConstants.AOP_AFTER,name);
            advice.setPriority(calPriority(registerFactory,anno.priority()));
            advice.setAfterInvoke(i -> {
                try {
                    var aspect = registerFactory.getAspectMetadata()
                            .getAspectClass()
                            .getDeclaredConstructor()
                            .newInstance();
                    m.invoke(aspect,i);
                } catch (Exception e) {
                    throw new RuntimeException("后置增强报错：" + e.getMessage());
                }
            });
            registerFactory.register(advice,pointcut.get());
        }
    }

    public void registerAll() {
        for (var i : registerFactories) {
            doRegister(i);
        }
    }


    public List<Pointcut> getPointcuts(){
        return pointcuts;
    }


    public List<Advice> getAdvice(Method method,Class<?> clazz,int type){
        List<Advice> res = new ArrayList<>();
        for (var i:registerFactories){
            List<Advice> advices = i.getAdvices(method, clazz);
            if (advices != null && !advices.isEmpty()){
                res.addAll(advices);
            }
        }
        return res.stream().filter(i -> i.type() == type).collect(Collectors.toList());
    }
}
