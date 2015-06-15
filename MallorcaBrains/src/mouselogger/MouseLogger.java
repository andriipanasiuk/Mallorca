/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mouselogger;

import java.awt.MouseInfo;
import java.awt.Point;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mallorcatour.tools.Log;
import mallorcatour.tools.MyFileWriter;

/**
 *
 * @author Andrew
 */
public class MouseLogger {

    private final String LOG_PATH;
    private static final int TIME_INTERVAL = 3;
    private boolean isStopped = false;
    private long time = 0;
    private ExecutorService executor;
    private boolean isClicked = false;

    public MouseLogger(String path) {
        LOG_PATH = path;
        executor = Executors.newSingleThreadExecutor();
    }

    public void click() {
        isClicked = true;
    }

    public void start() {
        final MyFileWriter fileWriter = MyFileWriter.prepareForWriting(LOG_PATH, false);
        executor.submit(new Runnable() {

            public void run() {
                while (!isStopped) {
                    if (isClicked) {
                        fileWriter.addToFile("Click", true);
                        isClicked = false;
                    }
                    Point position = MouseInfo.getPointerInfo().getLocation();
                    // logging
                    String log = "X: " + position.x + " Y: " + position.y + " Time: " + time;
                    fileWriter.addToFile(log, true);
                    //
                    time += TIME_INTERVAL;
                    try {
                        Thread.sleep(TIME_INTERVAL);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                fileWriter.endWriting();

            }
        });

    }

    public void stop() {
        isStopped = true;
    }
}
