/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.humanadvisor;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.Advisor;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.game.state.HandState;
import mallorcatour.tools.ExecutorUtils;
import mallorcatour.tools.OnExceptionListener;

public abstract class AbstractHumanAdvisor implements Advisor {

    @Override
    public IAdvice getAdvice(HandState situation, HoleCards cards, IPlayerGameInfo gameInfo) {
        final Action[] result = new Action[1];
        final Object lock = new Object();
        final IActionHolder holder = action -> {
            result[0] = action;
            synchronized (lock) {
                lock.notifyAll();
            }
        };
        ExecutorUtils.newSingleThreadExecutor(OnExceptionListener.EMPTY).
                submit(() -> getPreflopAction(gameInfo, holder));
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
        return result[0];
    }

    public static Action createHeroRaiseAction(IPlayerGameInfo gameInfo, double percent) {
        double effectiveStack = gameInfo.getBankRollAtRisk();
        double toCall = gameInfo.getAmountToCall();
        double pot = gameInfo.getPotSize();
        return Action.createRaiseAction(toCall, pot, effectiveStack, percent);
    }

    protected abstract void getPreflopAction(IPlayerGameInfo gameInfo, final IActionHolder actionHolder);

}
