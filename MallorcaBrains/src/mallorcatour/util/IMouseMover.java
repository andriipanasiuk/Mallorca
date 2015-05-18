package mallorcatour.util;

import java.awt.Point;
import java.awt.Robot;

public abstract interface IMouseMover {
	public abstract void moveTo(Robot paramRobot, Point paramPoint);
}
