package DIframework.aop.impl;

import DIframework.aop.interfaces.Advice;
import DIframework.aop.interfaces.AdviceRegisterFactory;
import DIframework.aop.interfaces.AspectMetadata;
import DIframework.aop.interfaces.Pointcut;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultAdviceRegisterFactory implements AdviceRegisterFactory {
    private final Map<Advice, Pointcut> adviceMap = new HashMap<>();
    private final AspectMetadata aspect;

    public DefaultAdviceRegisterFactory(AspectMetadata aspectMetadata) {
        this.aspect = aspectMetadata;
    }

    @Override
    public void register(Advice advice, Pointcut pointcut) {
        if (advice instanceof MethodAdvice) {
            adviceMap.put(advice, pointcut);
        }
    }

    @Override
    public List<Advice> getAdvices() {
        return adviceMap.keySet().stream().sorted().toList();
    }

    @Override
    public List<Advice> getAdvices(Method method, Class<?> targetClass) {
        List<Advice> res = new ArrayList<>();
        adviceMap.keySet().forEach(i -> {
            if (adviceMap.get(i).matches(method, targetClass))
                res.add(i);
        });
        res.sort(Advice::compareTo);
        return res;
    }

    @Override
    public AspectMetadata getAspectMetadata() {
        return aspect;
    }
}
