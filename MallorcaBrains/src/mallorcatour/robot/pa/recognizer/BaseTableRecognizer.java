/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.pa.recognizer;

import java.awt.Point;
import java.awt.Rectangle;
import mallorcatour.util.ImageUtils;

/**
 *
 * @author Andrew
 */
public abstract class BaseTableRecognizer {

    protected abstract String getMyActionPath();

    protected abstract String getRaisePath();

    protected abstract String getBetPath();

    public abstract Rectangle getMyActionRectangle(boolean global);

    protected abstract Rectangle getAggressiveRectangle(boolean global);

    protected abstract Rectangle getActivateRectangle(boolean global);

    public abstract Point getTopLeftPoint();

    public abstract double getPot();

    public boolean isBetAction() {
        Rectangle rect = getAggressiveRectangle(true);
        if (ImageUtils.isOnScreen(getBetPath(), rect) != null) {
            return true;
        }
        return false;
    }

    public boolean isRaiseAction() {
        if (ImageUtils.isOnScreen(getRaisePath(), getAggressiveRectangle(true)) != null) {
            return true;
        }
        return false;
    }
}
