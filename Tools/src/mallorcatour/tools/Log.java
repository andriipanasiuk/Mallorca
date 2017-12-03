package mallorcatour.tools;

public class Log {
    public static boolean WRITE_TO_ERR = false;
    public static boolean WRITE_TO_CONSOLE = true;
    public static String DEBUG_PATH = "debug.txt";

    public static void d(String log) {
        if (WRITE_TO_ERR) {
            System.err.println(log);
        } else {
            System.out.println(log);
        }
    }

    public static void f(String path, String log) {
        d(log);
//        f(0, path, log);
    }

    private static void f(int indent, String path, String log) {
        StringBuilder prefix = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            prefix.append("	");
        }
        log = prefix + log;
        if (WRITE_TO_ERR) {
            System.err.println(log);
        } else if (WRITE_TO_CONSOLE) {
            System.out.println(log);
        } else {
            MyFileWriter fileWriter = MyFileWriter.prepareForWriting(path, true);
            fileWriter.addToFile(log, true);
            fileWriter.endWriting();
        }
    }

    public static void f(int indent, String log) {
        d(log);
//        f(indent, DEBUG_PATH, log);
    }

    public static void f(String log) {
        f(DEBUG_PATH, log);
    }

}
