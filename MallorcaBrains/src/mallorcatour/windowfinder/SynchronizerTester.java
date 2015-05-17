/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.windowfinder;

import br.com.wagnerpaz.javahook.NativeKeyboardEvent;
import br.com.wagnerpaz.javahook.NativeKeyboardListener;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;
import java.awt.event.KeyEvent;
import mallorcatour.grandtorino.robot.ActionSynchronizer;
import mallorcatour.hookmanager.KeyboardHookManager;
import mallorcatour.util.ExecutorUtils;
import mallorcatour.util.Log;
import mallorcatour.util.OnExceptionListener;
import mallorcatour.util.ThreadUtils;

/**
 *
 * @author Andrew
 */
public class SynchronizerTester implements WNDENUMPROC {

    private final static Object lock = new Object();
    private final static String filename = "hs_err_pid1504.log";
    public static void main(String[] args) {
        new SynchronizerTester().test();
    }

    public void test() {
        KeyboardHookManager.addListener(new NativeKeyboardListener() {

            public boolean keyPressed(NativeKeyboardEvent nke) {
                if (nke.getKeyCode() == KeyEvent.VK_F3) {
                    System.exit(0);
                    return false;
                }
                return true;
            }

            public boolean keyReleased(NativeKeyboardEvent nke) {
                return true;
            }
        });
        ExecutorUtils.newSingleThreadExecutor(OnExceptionListener.EMPTY).submit(new Runnable() {

            public void run() {
                int i = 0;
                while (true) {
                    synchronized (lock) {
                        i++;
                        ActionSynchronizer.beforeAction();
                        Log.d("Action");
                        Log.d("Action");
                        Log.d("Action");
                        Log.d("Action");
                        Log.d("Action");
                        Log.d("Action");
                        Log.d("Action");
                        Log.d("Action");
                        Log.d("Action");
                        Log.d("Action");
                        ActionSynchronizer.endOfAction();
                    }
                }
            }
        });
        ExecutorUtils.newSingleThreadExecutor(OnExceptionListener.EMPTY).submit(new Runnable() {

            public void run() {
                while (true) {
                    ActionSynchronizer.beforeLogic();
                    Log.d("Logic1");
                    Log.d("Logic1");
                    Log.d("Logic1");
                    Log.d("Logic1");
                    Log.d("Logic1");
                    Log.d("Logic1");
                    Log.d("Logic1");
                    Log.d("Logic1");
                    Log.d("Logic1");
                    ActionSynchronizer.endOfLogic();
                    ThreadUtils.sleep(150);
                }
            }
        });
        ExecutorUtils.newSingleThreadExecutor(OnExceptionListener.EMPTY).submit(new Runnable() {

            public void run() {
                while (true) {
                    ActionSynchronizer.beforeLogic();
                    Log.d("Logic2");
                    Log.d("Logic2");
                    Log.d("Logic2");
                    Log.d("Logic2");
                    Log.d("Logic2");
                    Log.d("Logic2");
                    Log.d("Logic2");
                    Log.d("Logic2");
                    Log.d("Logic2");
                    ActionSynchronizer.endOfLogic();
                    ThreadUtils.sleep(150);
                }
            }
        });
        ExecutorUtils.newSingleThreadExecutor(OnExceptionListener.EMPTY).submit(new Runnable() {

            public void run() {
                while (true) {
                    ActionSynchronizer.beforeLogic();
                    Log.d("Logic3");
                    Log.d("Logic3");
                    Log.d("Logic3");
                    Log.d("Logic3");
                    Log.d("Logic3");
                    Log.d("Logic3");
                    Log.d("Logic3");
                    Log.d("Logic3");
                    Log.d("Logic3");
                    ActionSynchronizer.endOfLogic();
                    ThreadUtils.sleep(150);
                }
            }
        });
        ExecutorUtils.newSingleThreadExecutor(OnExceptionListener.EMPTY).submit(new Runnable() {

            public void run() {
                while (true) {
                    ActionSynchronizer.beforeLogic();
                    Log.d("Logic4");
                    Log.d("Logic4");
                    Log.d("Logic4");
                    Log.d("Logic4");
                    Log.d("Logic4");
                    Log.d("Logic4");
                    Log.d("Logic4");
                    Log.d("Logic4");
                    Log.d("Logic4");
                    ActionSynchronizer.endOfLogic();
                    ThreadUtils.sleep(150);
                }
            }
        });

    }

    public boolean callback(HWND hwnd, Pointer pntr) {
        return true;
    }
}
