package mallorcatour.util;

public abstract interface OnExceptionListener {
	public static final OnExceptionListener EMPTY = new OnExceptionListener() {
		public void onException(Exception e) {
		}
	};

	public abstract void onException(Exception paramException);
}
