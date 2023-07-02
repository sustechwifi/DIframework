package DIframework.core.impl;

import DIframework.aop.annotation.After;
import DIframework.aop.annotation.Aspect;
import DIframework.aop.annotation.Before;
import DIframework.aop.impl.AnnotatedAspectMetadata;
import DIframework.aop.impl.AspectJAutoProxyFactory;
import DIframework.aop.impl.AspectJExpressionPointcut;
import DIframework.aop.impl.DefaultAdviceRegisterFactory;
import DIframework.core.annotation.*;
import DIframework.core.interfaces.ApplicationContext;
import DIframework.core.interfaces.ApplicationEvent;
import DIframework.data.annotation.Repository;
import DIframework.data.utils.DataSourceConfig;
import DIframework.data.utils.DatabaseManipulationProxy;
import DIframework.utils.Log;
import DIframework.utils.MyClassScanner;
import DIframework.web.annotation.RestController;
import DIframework.web.annotation.Service;
import DIframework.web.impl.DispatcherServlet;
import DIframework.web.utils.TomcatConfig;
import DIframework.web.utils.TomcatFactory;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultApplicationContext extends AbstractApplicationContext {

    private static AbstractAutowireCapableBeanFactory defaultBeanFactory;
    private static AspectJAutoProxyFactory aspectJAutoProxyFactory;

    private static DatabaseManipulationProxy mapperFactory;

    private static ApplicationContext context;
    private static DefaultEventMulticaster multicaster;

    private static DispatcherServlet dispatcherServlet;

    private static String applicationPath;

    private static boolean webAppEnable;
    private static boolean jdbcEnable;

    private static final Class<?>[] accessedComponent = {
            Component.class,
            RestController.class,
            Service.class,
    };

    private static final Class<?>[] singletonComponent = {
            RestController.class,
            Service.class,
    };

    private static AnnotatedAspectMetadata loadAspect(Class<?> cls) {
        var metadata = new AnnotatedAspectMetadata();
        if (cls.isAnnotationPresent(Aspect.class)) {
            var anno = cls.getAnnotation(Aspect.class);
            var name = anno.value();
            if ("".equals(name.trim())) {
                name = cls.getName();
            }
            metadata.setName(name);
            metadata.setAspectClass(cls);
            Arrays.stream(cls.getDeclaredMethods()).forEach(i -> {
                if (i.isAnnotationPresent(Before.class) || i.isAnnotationPresent(After.class))
                    metadata.addAspectMethod(i);
            });
        } else {
            Log.error("缺少 @Aspect 注解：" + cls.getName());
            throw new RuntimeException("缺少 @Aspect 注解：" + cls.getName());
        }
        return metadata;
    }

    private static void loadConfiguration(List<Class<?>> classes) throws Throwable {
        for (Class<?> c : classes) {
            if (c.isAnnotationPresent(Configuration.class)) {
                var obj = c.getDeclaredConstructor().newInstance();
                var methods = c.getDeclaredMethods();
                try {
                    for (var m : methods) {
                        if (m.isAnnotationPresent(Bean.class)) {
                            var tmp = m.getAnnotation(Bean.class).value();
                            var name = tmp.equals("") ? m.getName() : tmp;
                            // 得到实际返回值的类型
                            var res = m.invoke(obj);
                            var resClass = res.getClass();
                            // 切点和切面不保存在容器中，无法通过 getBean 获取
                            if (resClass.isAnnotationPresent(Aspect.class)) {
                                var adviceRegisterFactory = new DefaultAdviceRegisterFactory(loadAspect(resClass));
                                aspectJAutoProxyFactory.addAdviceRegisterFactory(adviceRegisterFactory);
                            } else if (res instanceof AspectJExpressionPointcut pointcut) {
                                pointcut.setName(name);
                                aspectJAutoProxyFactory.addPointCut(pointcut);
                            } else {
                                //添加其他配置到容器中
                                defaultBeanFactory.singletonObjects.put(name, res);
                            }
                        }
                    }
                    multicaster.onLoadConfiguration(() -> "配置类扫描完成");
                } catch (Exception e) {
                    Log.error("解析配置时发生错误:\n" + e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }
    }


    private static List<Class<?>> clsPreprocess(List<Class<?>> classes) {
        // 先将单例 Mapper 添加到容器中
        for (var c : classes) {
            if (c.isAnnotationPresent(Repository.class)) {
                String name = c.getAnnotation(Repository.class).value();
                name = "".equals(name) ? c.getName() : name;
                defaultBeanFactory.singletonObjects.put(name,
                        mapperFactory.getMapper(context.getClass(), c));
            }
        }
        return classes.stream()
                .filter(i -> {
                    for (var anno : i.getDeclaredAnnotations())
                        if (Arrays.stream(accessedComponent).toList().contains(anno.annotationType()))
                            return true;
                    return false;
                }).collect(Collectors.toList());
    }

    private static void loadBeanConfig(DefaultBeanDefinition definition, Class<?> c) {
        if (c.isAnnotationPresent(Component.class)) {
            var bean = c.getAnnotation(Component.class);
            definition.setSingleton(bean.isSingleton());
            definition.setBeanName(c.getName());
            var beanId = bean.value();
            if (!"".equals(beanId)) {
                definition.setBeanName(beanId);
            } else {
                definition.setBeanName(c.getName());
            }
        } else {
            String name;
            definition.setSingleton(true);
            if (c.isAnnotationPresent(RestController.class)) {
                var bean = (RestController) c.getAnnotation(RestController.class);
                name = bean.value();
            } else if (c.isAnnotationPresent(Service.class)) {
                var bean = (Service) c.getAnnotation(Service.class);
                name = bean.value();
            } else {
                throw new RuntimeException("未知的组件类型");
            }
            if (!"".equals(name)) {
                definition.setBeanName(name);
            } else {
                definition.setBeanName(c.getName());
            }
        }
    }

    private static void processAllClass(List<Class<?>> classes) {
        classes = clsPreprocess(classes);
        for (Class<?> c : classes) {
            // 装配 BeanDefinition
            var definition = new DefaultBeanDefinition();
            // 处理成员字段
            var privateFields = new ArrayList<Field>();
            var autoWiredFiled = new ArrayList<Field>();
            for (var field : c.getDeclaredFields()) {
                if (Modifier.isPrivate(field.getModifiers())) {
                    privateFields.add(field);
                } else if (field.isAnnotationPresent(AutoWired.class)) {
                    autoWiredFiled.add(field);
                }
            }

            // 处理成员方法
            var autoWiredMethods = new ArrayList<Method>();
            for (var method : c.getDeclaredMethods()) {
                if (method.isAnnotationPresent(AutoWired.class)) {
                    autoWiredMethods.add(method);
                }
            }

            // 处理构造器, 选择参数最多的构造器作为默认构造器
            var autoWiredConstructors = new ArrayList<Constructor<?>>();
            for (var constructor : c.getDeclaredConstructors()) {
                if (constructor.isAnnotationPresent(AutoWired.class)) {
                    autoWiredConstructors.add(constructor);
                }
            }
            var con = autoWiredConstructors.stream()
                    .max(Comparator.comparingInt(Constructor::getParameterCount));
            Constructor<?> chosenCon;
            if (con.isEmpty()) {
                try {
                    chosenCon = c.getDeclaredConstructor();
                } catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException(String.format("%s 类中缺少注入构造器或无参构造器", c));
                }
            } else {
                chosenCon = con.get();
            }
            loadBeanConfig(definition, c);
            definition.setBeanLoader(c.getClassLoader());
            definition.setAutoWiredConstructors(autoWiredConstructors.toArray(Constructor[]::new));
            definition.setAutoWiredMethods(autoWiredMethods.toArray(Method[]::new));
            definition.setAutoWiredFields(autoWiredFiled.toArray(Field[]::new));
            definition.setPrivateFields(privateFields.toArray(Field[]::new));
            definition.setDefaultConstructor(chosenCon);
            definition.setBeanClass(c);
            defaultBeanFactory.addBean(definition);
        }
        multicaster.onApplicationLifeCircle(() -> "bean definition 装配完成");
    }

    @Override
    public Set<String> listAllBeans() {
        return defaultBeanFactory.singletonObjects.keySet();
    }


    private static void loadConstructArgs(DefaultBeanDefinition definition) {
        if (definition == null || definition.getDefaultConstructor().getParameterCount() == 0)
            return;
        // 从构造器中注入
        var params = definition.getDefaultConstructor().getParameters();
        Object[] args = new Object[params.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = getAutoWireFieldFromConstructor(params[i]);
        }
        definition.setDefaultConstructArgs(args);
    }

    private static Object getAutoWireFieldFromConstructor(Parameter param) {
        Object autoWired = null;
        try {
            if (param.isAnnotationPresent(Qualified.class)){
                autoWired = context.getBean(param.getAnnotation(Qualified.class).value());
            }else {
                autoWired = context.getBean(param.getType());
                if (autoWired == null) {
                    throw new IllegalArgumentException(
                            String.format("非法的构造参数类型: %s", param.getType().getName()));
                }
            }
        } catch (Throwable e) {
            // 处理其他异常
            e.printStackTrace();
            throw new IllegalArgumentException("构造器注入发生错误");
        }
        return autoWired;
    }

    /**
     * 代理对象实例化
     */
    public static void initializeBean() {
        var definitions = defaultBeanFactory.beanFactories.values();
        for (var definition : definitions) {
            // 从 definition 初始化所有 bean
            var dd = (DefaultBeanDefinition) definition;
            loadConstructArgs(dd);
            defaultBeanFactory.earlySingletonObjects.put(
                    dd.getBeanName(),
                    defaultBeanFactory.doCreateBean(dd)
            );
        }
        multicaster.onApplicationLifeCircle(() -> "bean 代理对象实例化完成");
    }

    /**
     * 代理对象初始化
     * 进行字段注入
     */
    public static void populateBean() throws Throwable {
        var earlyBeans = defaultBeanFactory.earlySingletonObjects.entrySet();
        for (var bean : earlyBeans) {
            // 初始化所有单例的 bean
            if (!defaultBeanFactory.containsBean(bean.getKey())) {
                var d = defaultBeanFactory.getDefinition(bean.getKey());
                defaultBeanFactory.singletonObjects.put(
                        bean.getKey(),
                        defaultBeanFactory.doInitializeBean(d.getBeanName(), d, d.args())
                );
            }
        }
        multicaster.onApplicationLifeCircle(() -> "bean 代理对象字段注入完成，所有单例bean 的初始化完成");
    }

    // 只判断单例池中是否有该bean
    @Override
    public boolean containsBean(String var1) {
        return defaultBeanFactory.singletonObjects.containsKey(var1);
    }

    /**
     * 容器初始化
     */
    public static void createBeanFactory() {
        // 内部组件初始化
        aspectJAutoProxyFactory = new AspectJAutoProxyFactory();
        defaultBeanFactory = new ListableFactory(aspectJAutoProxyFactory);
        mapperFactory = new DatabaseManipulationProxy();
        context = new DefaultApplicationContext();
        dispatcherServlet = new DispatcherServlet(applicationPath, context);
        multicaster = new DefaultEventMulticaster();


        multicaster.onApplicationLifeCircle(() -> "上下文容器组件初始化完成");
        multicaster.setOnCreatedListener(e -> {
            var event = (DefaultApplicationEvent) e;
            var context = (DefaultApplicationContext) event.getBeanFactory();
            Log.callback(e.getMessage() + "\n当前应用程序上下文中的所有单例 bean为：" + context.listAllBeans());
            dispatcherServlet.init();
        });
        multicaster.setOnConfigListener(e -> {
            aspectJAutoProxyFactory.registerAll();
            Log.callback(e.getMessage());
        });
    }


    public static void prepareBeanFactory(List<Class<?>> classes) throws Throwable {
        loadConfiguration(classes);
        multicaster.onApplicationLifeCircle(() -> "解析配置类完成，已加载所有配置");
    }

    public static void runWebApplication() throws Throwable {
        if (!webAppEnable) return;
        var webConfig = TomcatConfig.readConfig(context);
        TomcatFactory webServer = new TomcatFactory(webConfig, dispatcherServlet);
        Log.log(String.format("Tomcat 服务已启动，运行端口:%d, 启动信息:%s",
                webConfig.getPort(), webConfig.getInfo()));
        webServer.init();
    }

    public static void jpaConfig() throws Throwable {
        if (!jdbcEnable) return;
        DataSourceConfig.connect(null, DataSourceConfig.readConfig(context));
    }

    public static void refresh() throws Throwable {
        createBeanFactory();
        synchronized (context){
            var classes = MyClassScanner.scan(applicationPath);
            prepareBeanFactory(classes);
            processAllClass(classes);
            initializeBean();
            populateBean();
            ApplicationEvent event = new DefaultApplicationEvent("上下文对象完成初始化，应用程序启动", context);
            multicaster.onCreated(event);
            multicaster.multicastEvent(event);
            jpaConfig();
            runWebApplication();
        }
    }

    public static <T> ApplicationContext run(Class<T> clazz, String[] args) throws Throwable {
        if (!clazz.isAnnotationPresent(Application.class)) {
            throw new IllegalArgumentException("不是启动类!");
        }
        if (args.length != 0) {
            Log.log("获取启动参数:" + Arrays.toString(args));
        }
        Application app = clazz.getAnnotation(Application.class);
        String packagePath = app.value();
        if ("".equals(packagePath)) {
            packagePath = clazz.getPackageName();
        }
        applicationPath = packagePath;
        webAppEnable = app.webApp();
        jdbcEnable = app.jdbcEnable();
        refresh();
        return context;
    }

    @Override
    public <T> T getBean(Class<T> var1) throws Throwable {
        return defaultBeanFactory.getBean(var1);
    }

    @Override
    public Object getBean(String var1) throws Throwable {
        return defaultBeanFactory.getBean(var1);
    }
}
