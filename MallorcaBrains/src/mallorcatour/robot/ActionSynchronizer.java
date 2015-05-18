/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot;

/**
 *
 * @author Andrew
 */
public class ActionSynchronizer {

    private static boolean actionDoing = false;
    private static boolean actionWaiting = false;
    private static int logics = 0;
    private final static Object actionLock = new Object();
    private final static Object logicLock = new Object();
    private final static Object lock = new Object();

    public static void beforeLogic() {
        synchronized (lock) {
            synchronized (actionLock) {
                if (actionDoing || actionWaiting) {
                    try {
                        actionLock.wait();
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    logics++;
                    return;
                }
            }
        }
        logics++;
    }

    public static void beforeAction() {
        synchronized (lock) {
            synchronized (logicLock) {
                if (logics > 0) {
                    try {
                        actionWaiting = true;
                        logicLock.wait();
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    actionDoing = true;
                    return;
                }
            }
        }
        actionWaiting = false;
        actionDoing = true;
    }

    public static void endOfLogic() {
        synchronized (logicLock) {
            logics--;
            if (logics == 0) {
                logicLock.notifyAll();
            }
        }
    }

    public static void endOfAction() {
        synchronized (actionLock) {
            actionDoing = false;
            actionLock.notifyAll();
        }
    }
}
