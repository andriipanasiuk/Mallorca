/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.humanadvisor;

import mallorcatour.bot.interfaces.IExternalAdvisor;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.util.ExecutorUtils;
import mallorcatour.util.OnExceptionListener;

/**
 *
 * @author Andrew
 */
public abstract class AbstractHumanAdvisor implements IExternalAdvisor {

    public Action getAction(final IGameInfo gameInfo) {
        final Action[] result = new Action[1];
        final Object lock = new Object();
        final IActionHolder holder = new IActionHolder() {

            public void actionGot(Action action) {
                result[0] = action;
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        };
        ExecutorUtils.newSingleThreadExecutor(OnExceptionListener.EMPTY).
                submit(new Runnable() {

            public void run() {
                getPreflopAction(gameInfo, holder);
            }
        });
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
        return result[0];
    }

    public static Action createHeroRaiseAction(IGameInfo gameInfo, double percent) {
        double effectiveStack = gameInfo.getBankRollAtRisk();
        double toCall = gameInfo.getHeroAmountToCall();
        double pot = gameInfo.getPotSize();
        return Action.createRaiseAction(toCall, pot, effectiveStack, percent);
    }

    protected abstract void getPreflopAction(IGameInfo gameInfo, final IActionHolder actionHolder);
}
