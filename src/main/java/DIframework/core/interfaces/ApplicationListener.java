package DIframework.core.interfaces;

@FunctionalInterface
public interface ApplicationListener {
    void onApplicationEvent(ApplicationEvent e);
}
