package DIframework.core.interfaces;

public interface BeanFactory {
    char FACTORY_BEAN_PREFIX = '&';

    Object getBean(String var1) throws Throwable;

    <T> T getBean(Class<T> var1) throws Throwable;

    boolean containsBean(String var1);

    boolean isSingleton(String var1) throws Throwable;

    Class<?> getType(String var1) throws Throwable;
}
