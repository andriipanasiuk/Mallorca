/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.util.mp3player;

import mallorcatour.util.Log;
import mp3player.Mp3Player;

/**
 *
 * @author Andrew
 */
public class Alarm {

    private static mp3player.Mp3Player player;
    public static final String ALARM_WAV_PATH = "assets/sound/alarm_no_table.wav";

    static {
        player = new Mp3Player(ALARM_WAV_PATH);
    }

    public static synchronized void alarm() {
        if (!player.isPlaying()) {
            Log.d("Alarm is played!");
            player.play(true);
        }
    }

    public static synchronized void stop() {
        if (player.isPlaying()) {
            Log.d("Alarm is stopped!");
            player.stop();
        }
    }

    public static void main(String args[]) {
        Alarm.alarm();
    }
}
