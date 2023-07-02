package DIframework.core.interfaces;


import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface BeanDefinition extends BeanFactory {
    ClassLoader getClassLoader();

    Field[] autoWiredFields();

    Method[] autoWiredMethods();

    Class<?> getBeanClass();

    String getBeanName();

    Object[] args();

}
