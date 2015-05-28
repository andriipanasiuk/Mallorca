package mallorcatour.util;

public class Log {
	public static boolean WRITE_TO_ERR = false;
	public static String DEBUG_PATH = "debug.txt";

	public static void d(String log) {
		if (WRITE_TO_ERR) {
			System.err.println(log);
		} else {
			System.out.println(log);
		}
	}

	public static void f(String path, String log) {
		if (WRITE_TO_ERR) {
			System.err.println(log);
		} else {
			MyFileWriter fileWriter = MyFileWriter.prepareForWriting(path, true);
			fileWriter.addToFile(log, true);
			fileWriter.endWriting();
		}
	}

	public static void f(String log) {
		f(DEBUG_PATH, log);
	}

}
