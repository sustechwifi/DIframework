package DIframework.web.impl;

import DIframework.core.interfaces.ApplicationContext;
import DIframework.utils.DefaultClassCastHelper;
import DIframework.utils.Log;
import DIframework.utils.MyClassScanner;
import DIframework.web.annotation.*;
import DIframework.web.interfaces.HttpMessageConverter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

public class RequestMappingHandlerMapping {

    private final ApplicationContext context;
    private final Map<String, List<Class<?>>> controllers;
    private final Map<String, HandlerMethod> handlerMethods;

    private final HttpMessageConverter converter;

    private final static Object lock = new Object();

    public RequestMappingHandlerMapping(String path, ApplicationContext context) {
        this.context = context;
        this.controllers = new HashMap<>();
        this.handlerMethods = new HashMap<>();
        this.converter = new DefaultHttpMessageConverter();
        afterPropertiesSet(path);
    }


    public void addRestController(String path, Class<?> controller) {
        if (controllers.containsKey(path)){
            controllers.get(path).add(controller);
        }else {
            var tmp = new ArrayList<Class<?>>();
            tmp.add(controller);
            controllers.put(path, tmp);
        }

    }

    private Map<String,String> matchPathVar(String pathPatten,String requestUri){
        String[] pathPatternParts = pathPatten.split("/");
        String[] requestUriParts = requestUri.split("/");
        Map<String, String> pathVariables = new HashMap<>();
        int diff = requestUriParts.length - pathPatternParts.length;
        for (int i = diff; i < requestUriParts.length; i++) {
            String pathPatternPart = pathPatternParts[i-diff];
            String requestUriPart = requestUriParts[i];
            if (pathPatternPart.startsWith("{") && pathPatternPart.endsWith("}")) {
                String variableName = pathPatternPart.substring(1, pathPatternPart.length() - 1);
                pathVariables.put(variableName, requestUriPart);
            }
        }
        return pathVariables;
    }

    public Object invokeMethod(HandlerMethod handlerMethod, HttpServletRequest request, HttpServletResponse response) throws Throwable {
        synchronized (lock) {
            var method = handlerMethod.getMethod();
            var anno = method.getAnnotation(RequestMapping.class);
            var requestType = request.getMethod();
            if (!anno.method().toString().equals(requestType)) {
                throw new IllegalArgumentException(
                        String.format("请求方法错误，预期为：%s, 实际为：%s", anno.method(), requestType)
                );
            }
            var params = method.getParameters();
            var args = new ArrayList<>();
            var caster = new DefaultClassCastHelper();
            var url = request.getRequestURI();
            var pathVarMap = matchPathVar(anno.value(), url);
            for (Parameter param : params) {
                if (param.isAnnotationPresent(RequestPara.class)){
                    var paramAnno = param.getAnnotation(RequestPara.class);
                    var paraName = paramAnno.value();
                    var value = request.getParameter(paraName);
                    if (value == null && paramAnno.need()){
                        throw new IllegalArgumentException("找不到请求参数: "+paraName);
                    }
                    args.add(caster.parse(value,param.getType()));
                }else if (param.isAnnotationPresent(RequestBody.class)) {
                    try {
                        args.add(converter.read(param.getType(),request));
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new IllegalArgumentException("请求体转换报错: "+param.getType());
                    }
                }else if (param.isAnnotationPresent(PathVariable.class)){
                    var paraName = param.getAnnotation(PathVariable.class).value();
                    if (pathVarMap.containsKey(paraName)){
                        args.add(caster.parse(pathVarMap.get(paraName),param.getType()));
                    }else {
                        throw new IllegalArgumentException("模板中找不到对应的路径参数: "+paraName);
                    }
                }else if (param.getType() == HttpServletRequest.class){
                    args.add(request);
                }else if (param.getType() == HttpServletResponse.class){
                    args.add(response);
                }
            }
            return method.invoke(handlerMethod.getBean(),args.toArray());
        }
    }


    private List<Class<?>> findController(String path) throws NoSuchMethodException {
        // 最长路径匹配
        var max = controllers.entrySet()
                .stream()
                .filter(i -> path.contains(i.getKey()))
                .max(Comparator.comparingInt(a -> a.getKey().split("/").length));
        if (max.isEmpty()){
            throw new NoSuchMethodException("没有匹配的controller");
        }
        return max.get().getValue();
    }
    public HandlerMethod getHandlerMethod(String requestURL) throws NoSuchMethodException {
        if (handlerMethods.containsKey(requestURL)) {
            return handlerMethods.get(requestURL);
        }
        var cls = findController(requestURL);
        //Log.debug(cls.toString());
        for (var c : cls){
            var router = c.getAnnotation(RequestMapping.class).value();
            for (Method declaredMethod : c.getDeclaredMethods()) {
                if (declaredMethod.isAnnotationPresent(RequestMapping.class)) {
                    var anno = declaredMethod.getAnnotation(RequestMapping.class);
                    if (matchPath(router,anno.value(),requestURL)) {
                        try {
                            var bean = context.getBean(c);
                            HandlerMethod handlerMethod = new HandlerMethod(declaredMethod, bean);
                            handlerMethods.put(requestURL, handlerMethod);
                            return handlerMethod;
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        throw new NoSuchMethodException("找不到此路径对应的方法:\t" + requestURL);
    }

    // 扫描注解
    private void afterPropertiesSet(String scanPath) {
        try {
            List<Class<?>> classes = MyClassScanner.scan(scanPath);
            for (var cls : classes) {
                if (cls.isAnnotationPresent(RestController.class)
                        && cls.isAnnotationPresent(RequestMapping.class)) {
                    var requestMapping = cls.getAnnotation(RequestMapping.class);
                    addRestController(requestMapping.value(), cls);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static boolean matchPath(String clsPath, String methodPath, String requestUrl) {
        // 去除开头和结尾的斜杠
        clsPath = clsPath.endsWith("/") ? clsPath.substring(0, clsPath.length() - 1) : clsPath;
        var pattern = clsPath + methodPath;
        String[] patternParts = pattern.split("/");
        String[] requestURLParts = requestUrl.split("/");
        int patternLength = patternParts.length;
        int requestURLLength = requestURLParts.length;
        if (patternLength != requestURLLength) {
            return false;
        }
        for (int i = 0; i < patternLength; i++) {
            String patternPart = patternParts[i];
            String requestURLPart = requestURLParts[i];
            if (!patternPart.equals(requestURLPart) && !patternPart.startsWith("{") && !patternPart.endsWith("}")) {
                return false;
            }
        }
        return true;
    }

}
