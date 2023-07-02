package DIframework.aop.interfaces;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

public interface AspectMetadata {
    Class<?> getAspectClass();
    String getAspectName();

    List<Method> getAspectMethods();
    List<Method> getAspectMethods(Class<? extends Annotation> annotationType);

    int getPriority();
}
