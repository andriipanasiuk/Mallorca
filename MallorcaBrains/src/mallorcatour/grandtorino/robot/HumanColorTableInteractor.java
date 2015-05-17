/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.grandtorino.robot;

import br.com.wagnerpaz.javahook.NativeMouseEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import mallorcatour.core.bot.IGameRobot;
import mallorcatour.game.core.Action;
import mallorcatour.grandtorino.robot.recognizer.PSTableRecognizer;
import mallorcatour.hookmanager.MouseClickLimiter;
import mallorcatour.hookmanager.MouseClickListener;
import mallorcatour.hookmanager.MouseHookManager;
import mallorcatour.util.IRectangleHolder;
import mallorcatour.util.Log;
import mallorcatour.util.RecognizerUtils;
import mallorcatour.util.ThreadUtils;

/**
 *
 * @author Andrew
 */
class HumanColorTableInteractor extends AbstractHumanTableInteractor {

    private final AdviceFrame frame;
    private final Object lock = new Object();
    private final IRectangleHolder repeatRectangleHolder;
    private final IRectangleHolder foldRectangleHolder;
    private final IRectangleHolder passiveRectangleHolder;
    private final IRectangleHolder aggressiveRectangleHolder;
    private MouseClickListener clickListener;

    public HumanColorTableInteractor(String debug, String heroName,
            IGameRobot robot, PSTableRecognizer recognizerL) {
        super(debug, heroName, robot, recognizerL);
        this.frame = new AdviceFrame();
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
        final Color color;
        log(action);
        if (action.isFold()) {
            color = Color.RED;
            clickRectangleHolder = foldRectangleHolder;
        } else if (action.isCall()) {
            color = Color.BLUE;
            if (recognizer.isPassiveButton()) {
                Log.d(robot + " there is passive button");
                clickRectangleHolder = passiveRectangleHolder;
            } else {
                Log.d(robot + " there is no passive button");
                clickRectangleHolder = aggressiveRectangleHolder;
            }
        } else if (action.isCheck()) {
            color = Color.BLUE;
            clickRectangleHolder = passiveRectangleHolder;
        } else if (action.isAggressive()) {
            color = Color.GREEN;
            clickRectangleHolder = aggressiveRectangleHolder;
        } else {
            throw new RuntimeException("Illegal action: " + action.toString());
        }
        Point location = new Point(recognizer.getTopLeftPosition());
        Dimension dimension = recognizer.getDimension();
        location.translate(dimension.width, dimension.height);
        location.translate(10, -5);
        frame.setLocation(location);
        frame.setColor(color);
        //uncomment
//        frame.setVisible(true);
        Log.d("Needed rectangle: " + clickRectangleHolder.getRectangle());
        clickListener = new ActionMouseClickListener(frame, clickRectangleHolder);
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

        private final AdviceFrame frame;
        private final IRectangleHolder clickRectangleHolder;

        public ActionMouseClickListener(AdviceFrame frame,
                IRectangleHolder clickRectangleHolder) {
            this.frame = frame;
            this.clickRectangleHolder = clickRectangleHolder;
        }

        @Override
        public void onClick(NativeMouseEvent nme) {
            if (!robot.isPlaying()) {
                return;
            }
            Point click = new Point(nme.getX(), nme.getY());
            Log.d("onClick() in HumanTableInteractor " + click);
            if (clickRectangleHolder.getRectangle().contains(click)) {
                Log.d("Click on action button: " + click);
                Log.d("Needed rectangle: " + clickRectangleHolder.getRectangle());
                frame.setVisible(false);
                synchronized (lock) {
                    lock.notifyAll();
                }
            } else if (repeatRectangleHolder.getRectangle().contains(click)) {
                Log.d("Click for repeat: " + click);
            }
            Log.d("onClick() in HumanActionExecutor end!");
        }
    }
}
