/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.hookmanager;

import br.com.wagnerpaz.javahook.NativeMouseEvent;
import br.com.wagnerpaz.javahook.NativeMouseListener;
import br.com.wagnerpaz.javahook.NativeMouseWheelEvent;
import java.awt.Point;
import mallorcatour.util.Log;

/**
 *
 * @author Andrew
 */
public abstract class MouseClickListener implements NativeMouseListener {

    private Point pressedPoint;

    public boolean mousePressed(NativeMouseEvent nme) {
        pressedPoint = new Point(nme.getX(), nme.getY());
        return true;
    }

    public boolean mouseReleased(NativeMouseEvent nme) {
        if (pressedPoint != null
                && nme.getX() == pressedPoint.x
                && nme.getY() == pressedPoint.y) {
            onClick(nme);
        }
        return true;
    }

    public boolean mouseMoved(NativeMouseEvent nme) {
        return true;
    }

    public boolean mouseWheelMoved(NativeMouseWheelEvent nmwe) {
        return true;
    }

    public abstract void onClick(NativeMouseEvent nme);

    public static void main(String[] args) {
        MouseHookManager.addListener(new MouseClickListener() {

            @Override
            public void onClick(NativeMouseEvent nme) {
                Log.d(nme.getX() + "");
            }
        });
    }
}
