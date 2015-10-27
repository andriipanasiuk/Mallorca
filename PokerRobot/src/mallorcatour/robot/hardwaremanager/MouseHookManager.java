/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.hardwaremanager;

import br.com.wagnerpaz.javahook.HookManager;
import br.com.wagnerpaz.javahook.NativeMouseEvent;
import br.com.wagnerpaz.javahook.NativeMouseListener;
import br.com.wagnerpaz.javahook.NativeMouseWheelEvent;

import java.util.ArrayList;
import java.util.List;

import mallorcatour.tools.Log;

/**
 *
 * @author Andrew
 */
public class MouseHookManager {

    private final static List<NativeMouseListener> listeners;
    private final static Object lock = new Object();

    static {
        listeners = new ArrayList<NativeMouseListener>();
    }

    public static void addListener(NativeMouseListener listener) {
        if (listeners.isEmpty()) {
            HookManager.installMouseHook(new NativeMouseListener() {

                public boolean mousePressed(NativeMouseEvent nme) {
                    boolean result = true;
                    synchronized (lock) {
                        for (NativeMouseListener listener : listeners) {
                            result = result && listener.mousePressed(nme);
                        }
                    }
                    return result;
                }

                public boolean mouseReleased(NativeMouseEvent nme) {
                    boolean result = true;
                    synchronized (lock) {
                        for (NativeMouseListener listener : listeners) {
                            result = result && listener.mouseReleased(nme);
                        }
                    }
                    return result;
                }

                public boolean mouseMoved(NativeMouseEvent nme) {
                    boolean result = true;
                    synchronized (lock) {
                        for (NativeMouseListener listener : listeners) {
                            result = result && listener.mouseMoved(nme);
                        }
                    }
                    return result;
                }

                public boolean mouseWheelMoved(NativeMouseWheelEvent nmwe) {
                    boolean result = true;
                    synchronized (lock) {
                        for (NativeMouseListener listener : listeners) {
                            result = result && listener.mouseWheelMoved(nmwe);
                        }
                    }
                    return result;
                }
            });
        }
        listeners.add(listener);
        Log.d("MouseHookManager. added listener");
        Log.d("MouseHookManager. Listeners: " + listeners);
    }

    public static boolean removeListener(NativeMouseListener listener) {
        boolean result;
        synchronized (lock) {
            result = listeners.remove(listener);
            if (listeners.isEmpty()) {
                HookManager.uninstallMouseHook();
            }
        }
        Log.d("MouseHookManager. remove listener");
        Log.d("MouseHookManager. Listeners: " + listeners);
        return result;
    }

    public static void removeAll() {
        listeners.clear();
        HookManager.uninstallMouseHook();
    }
}
