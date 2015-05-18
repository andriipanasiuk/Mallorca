package mallorcatour.util;


public class Log {
	public static void d(String log) {
		System.out.println(log);
	}

	public static void f(String path, String log) {
		MyFileWriter fileWriter = MyFileWriter.prepareForWriting(path, true);
		fileWriter.addToFile(log, true);
		fileWriter.endWriting();
	}
}
