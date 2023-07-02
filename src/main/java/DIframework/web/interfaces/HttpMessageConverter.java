package DIframework.web.interfaces;

import DIframework.web.impl.RequestMappingHandlerAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HttpMessageConverter {

    <T> T read (Class<? extends T> clazz, HttpServletRequest request);

    <T> void write(T data, HttpServletResponse response, RequestMappingHandlerAdapter handlerAdapter);

}
