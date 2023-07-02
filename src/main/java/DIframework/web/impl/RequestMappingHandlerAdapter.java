package DIframework.web.impl;

import DIframework.utils.Log;
import com.alibaba.fastjson2.JSON;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Predicate;

public class RequestMappingHandlerAdapter {

    private String baseDir = "";

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    private static byte[] readFileAsBytes(String fileName) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IllegalArgumentException("File not found in resource folder: " + fileName);
        }
        return inputStream.readAllBytes();
    }


    private static final Predicate<String> extendFilter = s -> {
        String[] allowedType = {
                "html", "js", "css", "ico", "jpg", "png", "txt","jfif"
        };
        for (var m :allowedType){
            if (s.matches(".+\\."+m)){
                return true;
            }
        }
        return false;
    };

    public boolean isFileQuery(String s){
        var tmp = s.split("/");
        if (tmp.length == 0){
            return false;
        }
        return extendFilter.test(tmp[tmp.length-1]);
    }

    public void adaptReturnType(HttpServletResponse response,Object returnValue){
        if (returnValue instanceof String str){
            if (str.matches(".+\\.html")){
                response.setContentType("text/html");
                response.setHeader("Connection","keep-alive");
                response.setHeader("Keep-Alive","timeout=60");
            }else if (str.matches(".+\\.js")){
               response.setContentType("application/javascript");
            }else if (str.matches(".+\\.css")){
                response.setContentType("text/css");
            }else if (str.matches(".+\\.ico")){
                response.setContentType("image/x-icon");
            }else if (str.matches(".+\\.png")){
                response.setContentType("image/png");
            }else if (str.matches(".+\\.jpg")){
                response.setContentType("image/jpeg");
            } else {
                response.setContentType("application/octet-stream");
            }
        }
    }

    public byte[] handleReturnValue(Object returnValue){
        if (returnValue instanceof String str){
            if (extendFilter.test(str)){
                try {
                    return readFileAsBytes(baseDir+str);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }else {
                return str.getBytes();
            }
        }else {
            return JSON.toJSONString(returnValue).getBytes();
        }
    }
}
