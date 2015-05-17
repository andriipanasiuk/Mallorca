/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.grandtorino.robot;

import br.com.wagnerpaz.javahook.NativeKeyboardEvent;
import br.com.wagnerpaz.javahook.NativeKeyboardListener;
import mallorcatour.hookmanager.KeyboardHookManager;

/**
 *
 * @author Andrew
 */
public class ResultListener {

    private final static String TOURNAMENT_SUCCESS_END_WAV_PATH = "assets/sound/app-31.wav";
    private final static String TOURNAMENT_FAIL_END_WAV_PATH = "assets/sound/app-31.wav";
    private final mp3player.Mp3Player endTournamentSuccessPlayer =
            new mp3player.Mp3Player(TOURNAMENT_SUCCESS_END_WAV_PATH);
    private final mp3player.Mp3Player endTournamentFailPlayer =
            new mp3player.Mp3Player(TOURNAMENT_FAIL_END_WAV_PATH);

    public ResultListener() {
        KeyboardHookManager.addListener(new NativeKeyboardListener() {

            public boolean keyPressed(NativeKeyboardEvent nke) {
                if (nke.getKeyCode() == ManageKeySettings.STOP_END_PLAYER_KEY) {
                    if (endTournamentSuccessPlayer.isPlaying()) {
                        endTournamentSuccessPlayer.stop();
                    }
                    if (endTournamentFailPlayer.isPlaying()) {
                        endTournamentFailPlayer.stop();
                    }
                    return false;
                }
                return true;
            }

            public boolean keyReleased(NativeKeyboardEvent nke) {
                if (nke.getKeyCode() == ManageKeySettings.STOP_END_PLAYER_KEY) {
                    return false;
                }
                return true;
            }
        });
    }

    public void onTournamentWin() {
        if (!endTournamentSuccessPlayer.isPlaying()) {
            endTournamentSuccessPlayer.play(true);
        }
    }

    public void onTournamentFail() {
        if (!endTournamentFailPlayer.isPlaying()) {
            endTournamentFailPlayer.play(true);
        }
    }
}
