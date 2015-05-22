/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.recognizer;

/**
 *
 * @author Andrew
 */
public interface OnMyActionListener {

    public static final int TIMEOUT = -1;
    public static final int MY_ACTION = 1;
    public static final int END_TOURNAMENT = 2;
    public static final int EMPTY_SEAT = 3;
    public static final int PA_PROCEED_WINDOW = 4;

    void onMyAction(int response);
}
