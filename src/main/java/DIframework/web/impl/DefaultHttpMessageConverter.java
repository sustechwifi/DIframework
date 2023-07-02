package DIframework.web.impl;

import DIframework.web.interfaces.HttpMessageConverter;
import com.alibaba.fastjson2.JSON;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

public class DefaultHttpMessageConverter implements HttpMessageConverter {

    @Override
    public <T> T read(Class<? extends T> clazz, HttpServletRequest request) {
        StringBuilder requestBody;
        try {
            BufferedReader reader = request.getReader();
            requestBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String requestBodyContent = requestBody.toString();
        return JSON.parseObject(requestBodyContent,clazz);
    }

    @Override
    public <T> void write(T data, HttpServletResponse response,RequestMappingHandlerAdapter handlerAdapter) {
        try {
            handlerAdapter.adaptReturnType(response,data);
            response.getOutputStream().write(handlerAdapter.handleReturnValue(data));
        } catch (IOException e) {
            e.printStackTrace();
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
