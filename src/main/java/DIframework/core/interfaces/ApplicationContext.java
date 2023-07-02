package DIframework.core.interfaces;

import java.util.Set;

public interface ApplicationContext extends BeanFactory{
    Set<String> listAllBeans();
}
