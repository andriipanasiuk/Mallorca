/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.pa;

import mallorcatour.core.game.advice.Advice;
import mallorcatour.util.DateUtils;
import mallorcatour.util.FileUtils;

/**
 *
 * @author Andrew
 */
public class AdviceWritingPARobotListener implements PARobotListener {

    private final String ADVICES = "advices_";
    private String path;

    public void onAction(Advice advice) {
        FileUtils.addToFile(advice.toString(), true);
    }

    public void onGameStarted() {
        path = ADVICES + DateUtils.getDate(false) + ".txt";
    }

    public void onGameEnded() {
        FileUtils.endWriting();
    }

    public void onGameResumed() {
        FileUtils.prepareForWriting(path, true);
    }

    public void onGamePaused() {
        FileUtils.endWriting();
    }
}
