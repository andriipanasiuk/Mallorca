/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.hardwaremanager;

import br.com.wagnerpaz.javahook.NativeMouseEvent;
import br.com.wagnerpaz.javahook.NativeMouseListener;
import br.com.wagnerpaz.javahook.NativeMouseWheelEvent;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import mallorcatour.tools.IRectangleHolder;
import mallorcatour.tools.Log;
import mallorcatour.tools.ThreadUtils;

/**
 *
 * @author Andrew
 */
public class MouseClickLimiter {

    private final static List<IRectangleHolder> banRectangles;
    private final static List<IRectangleHolder> unbanRectangles;
    private static boolean pressedInBannedZone = false;

    static {
        banRectangles = new ArrayList<IRectangleHolder>();
        unbanRectangles = new ArrayList<IRectangleHolder>();
        MouseHookManager.addListener(new NativeMouseListener() {

            private boolean processEvent(NativeMouseEvent nme) {
                Point click = new Point(nme.getX(), nme.getY());
                Log.d("MouseClickLimiter. Click on " + click);
                for (IRectangleHolder banRectangle : banRectangles) {
                    if (banRectangle.getRectangle().contains(click)) {
                        for (IRectangleHolder unbanRectangle : unbanRectangles) {
                            if (unbanRectangle.getRectangle().contains(click)) {
                                Log.d("This point is in unban zone");
                                return true;
                            }
                        }
                        Log.d("This point is in ban zone");
                        return false;
                    }
                }
                Log.d("This point is not in ban zone");
                return true;
            }

            public boolean mousePressed(NativeMouseEvent nme) {
                pressedInBannedZone = !processEvent(nme);
                return !pressedInBannedZone;
            }

            public boolean mouseReleased(NativeMouseEvent nme) {
                return !pressedInBannedZone || processEvent(nme);
            }

            public boolean mouseMoved(NativeMouseEvent nme) {
                return true;
            }

            public boolean mouseWheelMoved(NativeMouseWheelEvent nmwe) {
                return true;
            }
        });
    }

    public static void ban(IRectangleHolder rectangle) {
        banRectangles.add(rectangle);
    }

    public static void unban(IRectangleHolder rectangle) {
        unbanRectangles.add(rectangle);
    }

    public static boolean removeBan(IRectangleHolder rectangle) {
        return banRectangles.remove(rectangle);
    }

    public static boolean removeUnban(IRectangleHolder rectangle) {
        return unbanRectangles.remove(rectangle);
    }

    public static void removeAllBans() {
        banRectangles.clear();
    }

    public static void removeAllUnbans() {
        unbanRectangles.clear();
    }

    public static void main(String[] args) {
        final Rectangle rectangle = new Rectangle(0, 0, 500, 500);
        ThreadUtils.sleep(2000);
        MouseClickLimiter.ban(new IRectangleHolder() {

            public Rectangle getRectangle() {
                return rectangle;
            }
        });
        MouseClickLimiter.unban(new IRectangleHolder() {

            public Rectangle getRectangle() {
                return new Rectangle(0, 0, 200, 200);
            }
        });
        Log.d("Mouse click is switched off");
        ThreadUtils.sleep(17000);
        MouseClickLimiter.removeAllBans();
        Log.d("Mouse click is switched on");
    }
}
