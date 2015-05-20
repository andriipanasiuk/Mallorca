/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.humanadvisor;

import java.awt.MouseInfo;
import java.awt.Point;
import javax.swing.JFrame;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.frames.HumanActionFrame;
import mallorcatour.util.robot.RobotUtils;
import mallorcatour.util.spline.SplineMouseMover;

/**
 *
 * @author Andrew
 */
public class MouseHumanAdvisor extends AbstractHumanAdvisor {

    private Point pointerPosition;
    private HumanActionFrame frame;
    private ActionHolder holder;
    private final Point framePosition;

    public MouseHumanAdvisor() {
        frame = new HumanActionFrame();
        framePosition = new Point(350, 620);
        frame.setLocation(framePosition);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        holder = new ActionHolder();
        frame.setActionHolder(holder);
    }

    private class ActionHolder implements IActionHolder {

        private IActionHolder actionHolder;

        public void setHolder(IActionHolder actionHolder) {
            this.actionHolder = actionHolder;
        }

        public void actionGot(Action action) {
            frame.setVisible(false);
            RobotUtils.moveMouse(pointerPosition.x, pointerPosition.y,
                    new SplineMouseMover());
            actionHolder.actionGot(action);
        }
    }

    protected void getPreflopAction(IGameInfo gameInfo, final IActionHolder actionHolder) {
        pointerPosition = MouseInfo.getPointerInfo().getLocation();
        holder.setHolder(actionHolder);
        frame.setGameInfo(gameInfo);
        frame.setVisible(true);
        RobotUtils.moveMouse(framePosition.x, framePosition.y,
                new SplineMouseMover());
    }
}
