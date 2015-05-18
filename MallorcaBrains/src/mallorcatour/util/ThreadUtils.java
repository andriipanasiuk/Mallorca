package mallorcatour.util;

public class ThreadUtils {
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		}
	}
}
