/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.grandtorino.robot;

import mallorcatour.core.bot.IGameRobot;
import mallorcatour.grandtorino.robot.recognizer.PSTableRecognizer;
import mp3player.Mp3Player;
import mp3player.OnPlayerListener;

/**
 *
 * @author Andrew
 */
public abstract class AbstractHumanTableInteractor extends LoggingTableInteractor {

    private final static String TABLE_FOUND_PATH = "assets/sound/table_found.wav";
    private final mp3player.Mp3Player tableFoundPlayer = new Mp3Player(TABLE_FOUND_PATH);
    private boolean created = false;

    public AbstractHumanTableInteractor(String debug, String heroName,
            IGameRobot robot, PSTableRecognizer recognizer) {
        super(debug, heroName, robot, recognizer);
    }

    public void onCreate(boolean activate) {
        if (created) {
            return;
        }
        final Object lock = new Object();
        tableFoundPlayer.play(new OnPlayerListener() {

            public void onStop() {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });
        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        created = true;
    }
}
