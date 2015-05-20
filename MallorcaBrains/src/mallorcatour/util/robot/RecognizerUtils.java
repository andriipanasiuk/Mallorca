package mallorcatour.util.robot;

import java.awt.Point;
import java.awt.Rectangle;

public class RecognizerUtils {
	public static Rectangle getGlobalRectangle(Rectangle localRectangle, Point topLeftPosition) {
		if (topLeftPosition == null) {
			throw new NullPointerException();
		}
		Rectangle result = new Rectangle(localRectangle);
		result.translate(topLeftPosition.x, topLeftPosition.y);
		return result;
	}
}
