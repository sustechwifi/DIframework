package DIframework.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class MyClassScanner {

    public static List<Class<?>> scan(String basePackage) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        String basePath = basePackage.replace(".", "/");
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(basePath);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            File file = new File(resource.getFile());
            scanDir(file, basePackage, classes);
        }
        return classes;
    }

    private static void scanDir(File dir, String basePackage, List<Class<?>> classes) throws ClassNotFoundException {
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                scanDir(file, basePackage + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                String className = basePackage + "." + file.getName().substring(0, file.getName().length() - 6);
                classes.add(Class.forName(className));
            }
        }
    }
}
