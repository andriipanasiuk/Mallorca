/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot;

import br.com.wagnerpaz.javahook.NativeMouseEvent;
import java.awt.Point;
import java.awt.Rectangle;

import mallorcatour.core.game.Action;
import mallorcatour.robot.controller.PokerPreferences;
import mallorcatour.robot.hardwaremanager.MouseClickLimiter;
import mallorcatour.robot.hardwaremanager.MouseClickListener;
import mallorcatour.robot.hardwaremanager.MouseHookManager;
import mallorcatour.robot.ps.PSGameRobot;
import mallorcatour.robot.ps.recognizer.PSTableRecognizer;
import mallorcatour.util.IRectangleHolder;
import mallorcatour.util.Log;
import mallorcatour.util.RecognizerUtils;
import mallorcatour.util.ThreadUtils;
import mp3player.Mp3Player;

/**
 *
 * @author Andrew
 */
class HumanVoiceTableInteractor extends AbstractHumanTableInteractor {

    private final static String FOLD_SOUND_PATH = "assets/sound/fold.wav";
    private final static String CALL_SOUND_PATH = "assets/sound/call.wav";
    private final static String CHECK_SOUND_PATH = "assets/sound/check.wav";
    private final static String RAISE_SOUND_PATH = "assets/sound/aggressive.wav";
//    private final static String FIRST_SOUND_PATH = "assets/sound/first_table.wav";
//    private final static String SECOND_SOUND_PATH = "assets/sound/second_table.wav";
//    private final static String THIRD_SOUND_PATH = "assets/sound/third_table.wav";
    private final mp3player.Mp3Player foldPlayer = new Mp3Player(FOLD_SOUND_PATH);
    private final mp3player.Mp3Player checkPlayer = new Mp3Player(CHECK_SOUND_PATH);
    private final mp3player.Mp3Player callPlayer = new Mp3Player(CALL_SOUND_PATH);
    private final mp3player.Mp3Player raisePlayer = new Mp3Player(RAISE_SOUND_PATH);
//    private final mp3player.Mp3Player firstPlayer = new Mp3Player(FIRST_SOUND_PATH);
//    private final mp3player.Mp3Player secondPlayer = new Mp3Player(SECOND_SOUND_PATH);
//    private final mp3player.Mp3Player thirdPlayer = new Mp3Player(THIRD_SOUND_PATH);
    private final Object lock = new Object();
    private final IRectangleHolder repeatRectangleHolder;
    private final IRectangleHolder foldRectangleHolder;
    private final IRectangleHolder passiveRectangleHolder;
    private final IRectangleHolder aggressiveRectangleHolder;
    private MouseClickListener clickListener;

    public HumanVoiceTableInteractor(String debug, String heroName,
            PSGameRobot robot, final PSTableRecognizer recognizer) {
        super(debug, heroName, robot, recognizer);
        repeatRectangleHolder = new IRectangleHolder() {

            public Rectangle getRectangle() {
                return RecognizerUtils.getGlobalRectangle(
                        recognizer.getActivateRectangle(),
                        recognizer.getTopLeftPosition());
            }
        };
        foldRectangleHolder = new IRectangleHolder() {

            public Rectangle getRectangle() {
                return RecognizerUtils.getGlobalRectangle(
                        recognizer.getFoldButtonRectangle(),
                        recognizer.getTopLeftPosition());
            }
        };

        passiveRectangleHolder = new IRectangleHolder() {

            public Rectangle getRectangle() {
                return RecognizerUtils.getGlobalRectangle(
                        recognizer.getPassiveButtonRectangle(),
                        recognizer.getTopLeftPosition());
            }
        };
        aggressiveRectangleHolder = new IRectangleHolder() {

            public Rectangle getRectangle() {
                return RecognizerUtils.getGlobalRectangle(
                        recognizer.getAggressiveButtonRectangle(),
                        recognizer.getTopLeftPosition());
            }
        };
    }

    public void doAction(Action action) {
        final IRectangleHolder clickRectangleHolder;
        final Mp3Player player;
        if (action.isFold()) {
            player = foldPlayer;
            clickRectangleHolder = foldRectangleHolder;
        } else if (action.isCall()) {
            player = callPlayer;
            if (recognizer.isPassiveButton()) {
                clickRectangleHolder = passiveRectangleHolder;
            } else {
                clickRectangleHolder = aggressiveRectangleHolder;
            }
        } else if (action.isCheck()) {
            player = checkPlayer;
            clickRectangleHolder = passiveRectangleHolder;
        } else if (action.isAggressive()) {
            player = raisePlayer;
            clickRectangleHolder = aggressiveRectangleHolder;
        } else {
            throw new RuntimeException("Illegal action: " + action.toString());
        }
        player.play(false);
        Log.d("Needed rectangle: " + clickRectangleHolder.getRectangle());
        clickListener = new ActionMouseClickListener(player, clickRectangleHolder);
        MouseClickLimiter.unban(clickRectangleHolder);
        MouseHookManager.addListener(clickListener);
        Log.d("HumanActionExecutor doAction. Added click listener: " + clickListener);
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
        boolean result = MouseHookManager.removeListener(clickListener);
        Log.d("HumanActionExecutor doAction. Removed click listener: " + clickListener);
        MouseClickLimiter.removeUnban(clickRectangleHolder);
        Log.d("Mouse listener " + (result ? "not " : "") + "deleted");
        ThreadUtils.sleep(200);
    }

    private class ActionMouseClickListener extends MouseClickListener {

        private final Mp3Player player;
        private final IRectangleHolder clickRectangleHolder;

        public ActionMouseClickListener(Mp3Player player,
                IRectangleHolder clickRectangleHolder) {
            this.player = player;
            this.clickRectangleHolder = clickRectangleHolder;
        }

        @Override
        public void onClick(NativeMouseEvent nme) {
            if (!robot.isPlaying()) {
                return;
            }
            Point click = new Point(nme.getX(), nme.getY());
            Log.d("onClick() in HumanActionExecutor " + click);
            if (clickRectangleHolder.getRectangle().contains(click)) {
                Log.d("Click on action button: " + click);
                Log.d("Needed rectangle: " + clickRectangleHolder.getRectangle());
                synchronized (lock) {
                    lock.notifyAll();
                }
            } else if (repeatRectangleHolder.getRectangle().contains(click)) {
                Log.d("Click for repeat: " + click);
                player.play(false);
            }
            Log.d("onClick() in HumanActionExecutor end!");
        }
    }

    public static void main(String[] args) {
        new HumanVoiceTableInteractor(
                FOLD_SOUND_PATH, PokerPreferences.DEFAULT_HERO_NAME, null, null).raisePlayer.play(false);
    }
}
