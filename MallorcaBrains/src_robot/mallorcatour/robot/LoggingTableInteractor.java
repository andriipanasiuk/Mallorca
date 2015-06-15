/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot;

import mallorcatour.core.game.Action;
import mallorcatour.robot.interfaces.IGameRobot;
import mallorcatour.robot.ps.recognizer.PSTableRecognizer;
import mallorcatour.tools.Log;
import mallorcatour.util.robot.RobotUtils;

/**
 *
 * @author Andrew
 */
public abstract class LoggingTableInteractor implements ITableInteractor {

    private final String DEBUG_PATH;
    private final String heroName;
    protected final PSTableRecognizer recognizer;
    protected final IGameRobot robot;

    public LoggingTableInteractor(String debug, String heroName,
            IGameRobot robot, PSTableRecognizer recognizer) {
        this.DEBUG_PATH = debug;
        this.robot = robot;
        this.heroName = heroName;
        this.recognizer = recognizer;

    }

    protected void log(Action action) {
        if (action.isFold()) {
            Log.d(robot + " " + heroName + " folds");
            Log.f(DEBUG_PATH, heroName + " folds");
        } else if (action.isCall()) {
            Log.d(robot + " " + heroName + " calls " + action.getAmount());
            Log.f(DEBUG_PATH, heroName + " calls " + action.getAmount());
        } else if (action.isCheck()) {
            Log.d(robot + " " + heroName + " checks");
            Log.f(DEBUG_PATH, heroName + " checks");
        } else if (action.isAggressive()) {
            if (action.isAllin()) {
                Log.d(robot + " " + heroName + " going all-in");
                Log.f(DEBUG_PATH, heroName + " going all-in");
            } else {
                RobotUtils.clickButton(HotKeySettings.RAISE_BUTTON);
                Log.d(robot + " " + heroName + " raises " + action.getAmount());
                Log.f(DEBUG_PATH, heroName + " raises " + action.getAmount());
            }
        } else {
            Log.f(DEBUG_PATH,
                    "<--------------------Somethind wrong in doAction(). "
                    + heroName + " folds.------------>");
        }
    }
}
