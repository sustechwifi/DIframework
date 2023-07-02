package DIframework.aop.interfaces;

import java.lang.reflect.Method;
import java.util.List;

public interface AdviceRegisterFactory {
    void register(Advice advice,Pointcut pointcut);
    List<Advice> getAdvices();
    List<Advice> getAdvices(Method method, Class<?> targetClass);

    AspectMetadata getAspectMetadata();
}
