/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.pa.robot;

import mallorcatour.util.Log;

/**
 *
 * @author Andrew
 */
public class ContinuedPARobotListener extends AdviceWritingPARobotListener {

    private PAGameRobot robot;
    private int globalCount;
    private int localCount = 0;

    public ContinuedPARobotListener(PAGameRobot robot, int countOfPlays) {
        this.robot = robot;
        this.globalCount = countOfPlays;
    }

    @Override
    public void onGameEnded() {
    	Log.d("onGameEnded");
        localCount++;
        super.onGameEnded();
        if (localCount < globalCount) {
            robot.startGame();
        }
    }
}
