package DIframework.core.impl;

import DIframework.core.interfaces.ApplicationEvent;
import DIframework.core.interfaces.ApplicationListener;
import DIframework.utils.Log;
import DIframework.utils.OperationConstants;

import java.util.ArrayList;
import java.util.List;

public class DefaultEventMulticaster {
    private List<ApplicationListener> listeners = new ArrayList<>();

    private ApplicationListener configListener;

    private ApplicationListener createdListener;

    public void addListener(ApplicationListener listener) {
        listeners.add(listener);
    }

    public void setOnConfigListener(ApplicationListener configListener) {
        this.configListener = configListener;
    }

    public void setOnCreatedListener(ApplicationListener createdListener) {
        this.createdListener = createdListener;
    }

    public void removeListener(ApplicationListener listener) {
        listeners.remove(listener);
    }

    public void onApplicationLifeCircle(ApplicationEvent event){
        Log.lifeCircle(event.getMessage());
    }

    public void multicastEvent(ApplicationEvent event) {
        for (ApplicationListener listener : listeners) {
            listener.onApplicationEvent(event);
        }
    }

    public void onLoadConfiguration(ApplicationEvent event){
        configListener.onApplicationEvent(event);
    }

    public void onCreated(ApplicationEvent event){
        createdListener.onApplicationEvent(event);
        OperationConstants.welcome();
    }
}
