package DIframework.utils;

public class OperationConstants {
    public static final String WELCOME = """
           ██╗  ██╗  ██████╗  ███╗     ███╗      ██████╗
           ██║  ██║  ██╔════╝ ██╔╝     ██╔╝     ██╔═══██╗
           ███████║  █████╗   ██║      ██║      ██║   ██║
           ██╔══██║  ██╔══╝   ██║      ██║      ██║   ██║
           ██║  ██║  ███████╗ ███████╗ ███████╗ ╚██████╔╝
           ╚═╝  ╚═╝  ╚══════╝ ╚══════╝ ╚══════╝  ╚═════╝  DI framework -version 1.0 :)
            """;

    public static final int AOP_ASPECT_BASE = 0x7ff;

    public static final int AOP_BEFORE = 1;

    public static final int AOP_AFTER = 2;

    public static void welcome(){
        String green = "\u001B[32m";
        System.out.print(green);
        System.out.println(OperationConstants.WELCOME);
        System.out.print("\u001B[0m");
    }

}
