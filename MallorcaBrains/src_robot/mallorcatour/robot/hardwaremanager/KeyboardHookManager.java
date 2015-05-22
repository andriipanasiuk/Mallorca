/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.hardwaremanager;

import br.com.wagnerpaz.javahook.HookManager;
import br.com.wagnerpaz.javahook.NativeKeyboardEvent;
import br.com.wagnerpaz.javahook.NativeKeyboardListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mallorcatour.util.Log;

/**
 *
 * @author Andrew
 */
public class KeyboardHookManager {

    private static List<NativeKeyboardListener> listeners;

    static {
        listeners = new ArrayList<NativeKeyboardListener>();
    }

    public static void addListener(NativeKeyboardListener listener) {
        if (listeners.isEmpty()) {
            HookManager.installKeyboardHook(new NativeKeyboardListener() {

                public boolean keyPressed(NativeKeyboardEvent nke) {
                    boolean result = true;
                    for (NativeKeyboardListener listener : listeners) {
                        result = result && listener.keyPressed(nke);
                    }
                    return result;
                }

                public boolean keyReleased(NativeKeyboardEvent nke) {
                    boolean result = true;
                    for (NativeKeyboardListener listener : listeners) {
                        result = result && listener.keyReleased(nke);
                    }
                    return result;
                }
            });
        }
        listeners.add(listener);
    }

    public static boolean remove(NativeKeyboardListener listener) {
        boolean result = listeners.remove(listener);
        if (listeners.isEmpty()) {
            HookManager.uninstallKeyboardHook();
        }
        return result;
    }

    public static void removeAll() {
        listeners.clear();
        HookManager.uninstallKeyboardHook();
    }

    public static void main(String[] args) {
        final NativeKeyboardListener listener1 = new NativeKeyboardListener() {

            public boolean keyPressed(NativeKeyboardEvent nke) {
                if (nke.getKeyCode() == KeyEvent.VK_F11) {
                    Log.d("F11");
                    return false;
                }
                return true;
            }

            public boolean keyReleased(NativeKeyboardEvent nke) {
                return true;
            }
        };
        NativeKeyboardListener listener2 = new NativeKeyboardListener() {

            public boolean keyPressed(NativeKeyboardEvent nke) {
                if (nke.getKeyCode() == KeyEvent.VK_F12) {
                    Log.d("F12");
                }
                return true;
            }

            public boolean keyReleased(NativeKeyboardEvent nke) {
                return true;
            }
        };
        KeyboardHookManager.addListener(listener1);
        KeyboardHookManager.addListener(listener2);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(KeyboardHookManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        KeyboardHookManager.removeAll();
    }
}
