/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mallorcatour.core.bot;

/**
 *
 * @author Andrew
 */
public interface IGameRobot {

    boolean isPlaying();

    void pauseGame();

    void resumeGame(final boolean activate);

    void stopGame();
//    void doAction(Action action);
}
