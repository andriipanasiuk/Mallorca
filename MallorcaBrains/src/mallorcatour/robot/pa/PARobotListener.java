/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.pa;

import mallorcatour.core.game.advice.Advice;

/**
 *
 * @author Andrew
 */
public interface PARobotListener {

    void onAction(Advice actionDistribution);

    void onGameStarted();

    void onGameEnded();

    void onGameResumed();

    void onGamePaused();
}
