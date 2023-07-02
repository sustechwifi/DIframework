package DIframework.web.impl;

import DIframework.core.interfaces.ApplicationContext;
import DIframework.utils.Log;
import DIframework.web.interfaces.HttpMessageConverter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DispatcherServlet extends HttpServlet {

    private final ApplicationContext context;
    private final String scanPath;
    private HttpMessageConverter converter;
    private RequestMappingHandlerMapping handlerMapping;
    private RequestMappingHandlerAdapter handlerAdapter;


    public DispatcherServlet(String scanPath, ApplicationContext context) {
        this.scanPath = scanPath;
        this.context = context;

    }

    @Override
    public void init() {
        // 初始化RequestMappingHandlerMapping 和 RequestMappingHandlerAdapter
        handlerMapping = new RequestMappingHandlerMapping(scanPath, context);
        handlerAdapter = new RequestMappingHandlerAdapter();
        converter = new DefaultHttpMessageConverter();
        if (context.containsBean("resource")){
            try {
                handlerAdapter.setBaseDir((String) context.getBean("resource"));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    // 仅识别 ip:port/路径下的文件
    private boolean mvcPreProcess(HttpServletRequest request, HttpServletResponse response){
        String url = request.getRequestURI();
        if (handlerAdapter.isFileQuery(url)){
            handleReturnValue(url, response);
            return true;
        }
        return false;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (mvcPreProcess(request, response))return;
        // 获取请求的URL和其他条件
        String url = request.getRequestURI();
        // 根据URL和其他条件，使用RequestMappingHandlerMapping确定要调用的处理器方法
        HandlerMethod handlerMethod = findHandlerMethod(url);
        if (handlerMethod != null) {
            // 调用处理器方法
            Object result = invokeHandlerMethod(handlerMethod, request, response);
            // 处理返回值并设置到响应中
            if (handlerMethod.getMethod().getReturnType() == void.class){
                return;
            }
            handleReturnValue(result, response);
        } else {
            // 处理找不到处理器方法的情况
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private HandlerMethod findHandlerMethod(String request) {
        // 根据URL和其他条件，使用RequestMappingHandlerMapping找到匹配的处理器方法
        try {
            return handlerMapping.getHandlerMethod(request);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Object invokeHandlerMethod(HandlerMethod handlerMethod, HttpServletRequest request, HttpServletResponse response) {
        // 调用处理器方法
        Object res = null;
        try {
            res = handlerMapping.invokeMethod(handlerMethod, request,response);
        } catch (Throwable e) {
            var msg = String.format("在调用 %s 方法时出现错误",handlerMethod.getMethod());
            Log.error(msg);
            e.printStackTrace();
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }

    private void handleReturnValue(Object result, HttpServletResponse response) {
        // 处理返回值并设置到响应中
        converter.write(result, response,handlerAdapter);
    }
}
