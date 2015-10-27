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
import java.io.BufferedReader;

import mallorcatour.robot.ActionSynchronizer;
import mallorcatour.robot.hardwaremanager.KeyboardHookManager;
import mallorcatour.robot.util.RobotUtils;
import mallorcatour.robot.util.spline.SplineMouseMover;
import mallorcatour.tools.ExecutorUtils;
import mallorcatour.tools.Log;
import mallorcatour.tools.OnExceptionListener;
import mallorcatour.tools.ReaderUtils;
import mallorcatour.tools.ThreadUtils;

/**
 *
 * @author Andrew
 */
public class MouseTester implements WNDENUMPROC {

    private final static Object lock = new Object();
    private final static String filename = "hs_err_pid1504.log";

    public static void main(String[] args) {
        new MouseTester().test();
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
                        Log.d("Start of action");
                        if (i % 2 == 0) {
                            RobotUtils.moveMouse(700, 700, new SplineMouseMover());
                        } else {
                            RobotUtils.moveMouse(200, 200, new SplineMouseMover());
                        }
                        Log.d("End of action");
                        ActionSynchronizer.endOfAction();
                    }
                }
            }
        });
        for (int i = 0; i < 200; i++) {
            final int index = i;
            ExecutorUtils.newSingleThreadExecutor(OnExceptionListener.EMPTY).submit(new Runnable() {

                public void run() {
                    while (true) {
                        ActionSynchronizer.beforeLogic();
                        Log.d("Start of logic " + index);
                        BufferedReader reader = ReaderUtils.initReader(filename);
                        Log.d("Middle of logic " + index);
                        String buffer;
                        while ((buffer = ReaderUtils.readLineFrom(reader)) != null) {
                        }
                        Log.d("End of logic " + index);
                        ActionSynchronizer.endOfLogic();
                        ThreadUtils.sleep(100);
                    }
                }
            });
        }

    }

    public boolean callback(HWND hwnd, Pointer pntr) {
        return true;
    }
}
