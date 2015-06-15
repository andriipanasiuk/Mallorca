/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot;

import java.awt.Point;
import java.awt.Rectangle;

import mallorcatour.core.game.Action;
import mallorcatour.robot.hardwaremanager.MouseDragLimiter;
import mallorcatour.robot.interfaces.IGameRobot;
import mallorcatour.robot.ps.recognizer.PSTableRecognizer;
import mallorcatour.tools.IRandomizer;
import mallorcatour.tools.Log;
import mallorcatour.tools.ThreadUtils;
import mallorcatour.tools.UniformRandomizer;
import mallorcatour.util.robot.RobotUtils;
import mallorcatour.util.spline.SplineMouseMover;

/**
 *
 * @author Andrew
 */
public class KeyboardTableInteractor extends LoggingTableInteractor {

    private static final int MIN_PAUSE_BEFORE_ACTION = 100;
    private static final int MAX_PAUSE_BEFORE_ACTION = 500;
    private final IRandomizer randomizer;

    public KeyboardTableInteractor(String debug, String heroName,
            IGameRobot robot, PSTableRecognizer recognizer) {
        super(debug, heroName, robot, recognizer);
        this.randomizer = new UniformRandomizer();
    }

    private void pressButton(Action action) {
        if (action.isFold()) {
            RobotUtils.clickButton(HotKeySettings.FOLD_BUTTON);
        } else if (action.isCall()) {
            RobotUtils.clickButton(HotKeySettings.CALL_BUTTON);
        } else if (action.isCheck()) {
            RobotUtils.clickButton(HotKeySettings.CHECK_BUTTON);
        } else if (action.isAggressive()) {
            if (action.isAllin()) {
                RobotUtils.clickButton(HotKeySettings.ALLIN_BUTTON);
                RobotUtils.clickButton(HotKeySettings.RAISE_BUTTON);
            } else {
                double percent = action.getPercentOfPot();
                if (percent == 1) {
                    RobotUtils.clickButton(HotKeySettings.POT_BUTTON);
                } else if (percent == 0.7) {
                    RobotUtils.clickButton(HotKeySettings.SEVENTY_BANK_BUTTON);
                } else if (percent == 0.5) {
                    RobotUtils.clickButton(HotKeySettings.HALF_BANK_BUTTON);
                } else {
                    //if there is no percent of pot do minraise
                }
                RobotUtils.clickButton(HotKeySettings.RAISE_BUTTON);
            }
        } else {
            RobotUtils.clickButton(HotKeySettings.FOLD_BUTTON);
        }
    }

    private int delayBeforeAction() {
        int result = randomizer.getRandom(MIN_PAUSE_BEFORE_ACTION, MAX_PAUSE_BEFORE_ACTION);
        return result;
    }

    private int delayBeforeClicking() {
        int result = randomizer.getRandom(100, 450);
        return result;
    }

    public void doAction(Action action) {
        synchronized (KeyboardTableInteractor.class) {
            ThreadUtils.sleep(delayBeforeAction());
            ActionSynchronizer.beforeAction();
            if (!recognizer.isInFocus()) {
                MouseDragLimiter.switchDragging(false);
                Log.d("PSGameRobot " + robot + " is not in focus. We'll be activating");
                activateWindow();
                ThreadUtils.sleep(delayBeforeClicking());
            } else {
                Log.d("PSGameRobot " + robot + " is in focus. No need to activate");
            }
            pressButton(action);
            MouseDragLimiter.switchDragging(true);
            ActionSynchronizer.endOfAction();
        }
    }

    private void activateWindow() {
        Point topLeftPosition = recognizer.getTopLeftPosition();
        Rectangle clickRectangle = recognizer.getActivateRectangle();
        int xBias = (int) randomizer.getGaussRandom(clickRectangle.x, clickRectangle.x + clickRectangle.width);
        int yBias = (int) randomizer.getGaussRandom(clickRectangle.y, clickRectangle.y + clickRectangle.height);
        Log.d("Before mouse move");
        RobotUtils.moveMouse(topLeftPosition.x + xBias, topLeftPosition.y + yBias,
                new SplineMouseMover());
        Log.d("After mouse move");
        RobotUtils.pressMouse();
        ThreadUtils.sleep(200);
    }

    public void onCreate(boolean activate) {
        if (activate) {
            synchronized (KeyboardTableInteractor.class) {
                activateWindow();
            }
        }
    }
}
