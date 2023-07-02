package DIframework.utils;

public class Log {
    private static final Object lock = new Object();

    public static void log(String log){
        synchronized (lock){
            String yellow = "\u001B[33m";
            // 输出字符串前，先设置前景色为黄色
            System.out.print(yellow);
            // 输出日志信息
            System.out.println("\tLOG:"+log);
            // 恢复默认颜色
            System.out.print("\u001B[0m");
        }
    }

    public static void lifeCircle(String log){
        synchronized (lock) {
            // 前景色设为蓝色
            String yellow = "\u001B[34m";
            System.out.print(yellow);
            System.out.println("\tLifeCircle:"+log);
            System.out.print("\u001B[0m");
        }
    }

    public static void callback(String log){
        synchronized (lock) {
            // 前景色设为紫色
            String yellow = "\u001B[35m";
            System.out.print(yellow);
            System.out.println("\tCallback:"+log);
            System.out.print("\u001B[0m");
        }
    }

    public static void error(String log){
        synchronized (lock) {
            // 前景色设为红色
            String yellow = "\033[31m";
            System.out.print(yellow);
            System.out.println("\tError:"+log);
            System.out.print("\u001B[0m");
        }
    }

    public static void debug(String log){
        synchronized (lock) {
            System.out.println("\tDebug:"+log);
        }
    }
}
