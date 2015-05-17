/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.hookmanager;

import br.com.wagnerpaz.javahook.NativeMouseEvent;
import br.com.wagnerpaz.javahook.NativeMouseListener;
import br.com.wagnerpaz.javahook.NativeMouseWheelEvent;
import mallorcatour.util.Log;
import mallorcatour.util.ThreadUtils;

/**
 *
 * @author Andrew
 */
public class MouseDragLimiter {

    private static boolean switched = true;
    private static boolean pressed = false;

    static {
        MouseHookManager.addListener(new NativeMouseListener() {

            public boolean mousePressed(NativeMouseEvent nme) {
                pressed = true;
                return true;
            }

            public boolean mouseReleased(NativeMouseEvent nme) {
                pressed = false;
                return true;
            }

            public boolean mouseMoved(NativeMouseEvent nme) {
                if (pressed && !switched) {
                    return false;
                }
                return true;
            }

            public boolean mouseWheelMoved(NativeMouseWheelEvent nmwe) {
                return true;
            }
        });
    }

    public static void switchDragging(boolean switchOn) {
        switched = switchOn;
    }

    public static void main(String[] args) {
        ThreadUtils.sleep(3000);
        MouseDragLimiter.switchDragging(false);
        Log.d("Mouse is switched off");
        ThreadUtils.sleep(3000);
        MouseDragLimiter.switchDragging(true);
    }
}
