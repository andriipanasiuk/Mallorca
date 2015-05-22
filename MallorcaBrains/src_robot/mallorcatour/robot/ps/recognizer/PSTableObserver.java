/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.ps.recognizer;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;
import java.awt.Dimension;
import java.awt.Point;

import mallorcatour.robot.recognizer.ITableListener;
import mallorcatour.util.ExecutorUtils;
import mallorcatour.util.Log;
import mallorcatour.util.OnExceptionListener;

/**
 *
 * @author Andrew
 */
public class PSTableObserver {

    private final ITableListener listener;
    private boolean isTracingStopped = true;
    private final Dimension screenSize;

    public PSTableObserver(ITableListener listener, Dimension screenSize) {
        if (listener == null) {
            throw new RuntimeException();
        }
        this.screenSize = screenSize;
        this.listener = listener;
    }

    public void traceTable() {
        isTracingStopped = false;
        ExecutorUtils.newSingleThreadExecutor(
                OnExceptionListener.EMPTY).submit(new Runnable() {

            public void run() {
                while (!isTracingStopped) {
                    final User32 user32 = User32.INSTANCE;

                    final int[] count = new int[]{0};
                    user32.EnumWindows(new WNDENUMPROC() {

                        public boolean callback(HWND hWnd, Pointer arg1) {
                            char[] windowTextC = new char[512];
                            user32.GetWindowText(hWnd, windowTextC, 512);
                            String windowText = Native.toString(windowTextC);
                            RECT rectangle = new RECT();
                            user32.GetWindowRect(hWnd, rectangle);
                            if (!windowText.isEmpty() && User32.INSTANCE.IsWindowVisible(hWnd) && rectangle.left != -32000) {
                                count[0]++;
//                                Log.d("Count = " + count[0]);
//                                Log.d(windowText);
//                                Log.d(rectangle + "");
                            }
                            if (windowText.contains("Hold'em")) {
                                if (!User32.INSTANCE.IsWindowVisible(hWnd)) {
                                    listener.onTableInvisible();
                                } else if (rectangle.left < 0) {
                                    listener.onTableInvisible();
                                } else if (rectangle.top < 0) {
                                    listener.onTableInvisible();
                                } else if (count[0] > 2) {
                                    listener.onTableInvisible();
                                } else if (rectangle.right > screenSize.width) {
                                    listener.onTableInvisible();
                                } else if (rectangle.bottom > screenSize.height) {
                                    listener.onTableInvisible();
                                } else {
                                    listener.onTableVisible(
                                            new Point(rectangle.left, rectangle.top),
                                            new Dimension(rectangle.right - rectangle.left, rectangle.bottom - rectangle.top));
                                }
                                Log.d("Count of PS = " + count[0]);
                            }
                            return true;
                        }
                    }, null);
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }

            }
        });
    }

    public void stopTracing() {
        isTracingStopped = true;
    }
}
