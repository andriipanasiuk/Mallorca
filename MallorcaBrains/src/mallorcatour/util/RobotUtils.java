package mallorcatour.util;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;

public class RobotUtils {
	private static Robot robot;

	private static void initRobot() {
		if (robot == null)
			try {
				robot = new Robot();
			} catch (AWTException ex) {
				throw new RuntimeException(ex);
			}
	}

	public static void pressLeftMouse(int delay, int x, int y) {
		robot.delay(delay);
		robot.mouseMove(x, y);
		robot.mousePress(16);
		robot.delay(400);
		robot.mouseRelease(16);
		Log.d("pressMouse() x: " + x + " y: " + y);
	}

	public static void pressRightMouse(int delay, int x, int y) {
		robot.delay(delay);
		robot.mouseMove(x, y);
		robot.mousePress(8);
		robot.mouseRelease(8);
		Log.d("pressMouse() x: " + x + " y: " + y);
	}

	public static void pressMouse(int delay) {
		robot.delay(delay);
		robot.mousePress(16);
		robot.mouseRelease(16);
	}

	public static void pressMouse() {
		pressMouse(0);
	}

	public static void clickButton(String buttonLetter) {
		Log.d("Press button() button: " + buttonLetter);
		if (buttonLetter.length() != 1) {
			throw new IllegalArgumentException("Button must be only letter on the keyboard");
		}
		Field keyCodeField = null;
		int keyCode = 0;
		try {
			keyCodeField = KeyEvent.class.getField("VK_" + buttonLetter.toUpperCase());
			keyCode = keyCodeField.getInt(null);
		} catch (NoSuchFieldException ex) {
			throw new RuntimeException(ex);
		} catch (SecurityException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalArgumentException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
		clickButton(keyCode);
	}

	public static void clickButton(int keyCode) {
		clickButton(keyCode, 60);
	}

	public static void clickButton(int keyCode, int delay) {
		robot.keyPress(keyCode);
		robot.delay(delay);
		robot.keyRelease(keyCode);
	}

	public static void pressButton(int keyCode) {
		robot.keyPress(keyCode);
	}

	public static void releaseButton(int keyCode) {
		robot.keyRelease(keyCode);
	}

	public static boolean pressMouseOnImage(int delay, String path, Rectangle rect) {
		Point p = ImageUtils.isOnScreen(path, rect);
		if (p != null) {
			pressLeftMouse(delay, rect.x + p.x + 1, rect.y + p.y + 1);
			return true;
		}
		return false;
	}

	public static boolean pressMouseOnImage(String path, Rectangle rect) {
		return pressMouseOnImage(0, path, rect);
	}

	public static boolean pressMouseOnImage(String path) {
		return pressMouseOnImage(0, path, ImageUtils.screenRectangle);
	}

	public static void delay(int ms) {
		robot.delay(ms);
	}

	public static synchronized void moveMouse(int x, int y, IMouseMover mouseMover) {
		mouseMover.moveTo(robot, new Point(x, y));
	}

	static {
		initRobot();
	}
}
