package mallorcatour.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorUtils {
	public static ExecutorService newSingleThreadExecutor(final OnExceptionListener listener) {
		return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()) {
			public Future<?> submit(final Runnable task) {
				Runnable toRun = new Runnable() {
					public void run() {
						try {
							task.run();
						} catch (Exception ex) {
							ex.printStackTrace(System.out);
							listener.onException(ex);
						}
					}
				};
				return super.submit(toRun);
			}
		};
	}
}
