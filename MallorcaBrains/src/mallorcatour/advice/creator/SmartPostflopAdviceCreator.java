/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.advice.creator;

import mallorcatour.core.game.advice.Advice;

/**
 * Advice creator that has three rules before it creates advice:
 * 1) if aggression is bigger than MIN_AGGRESSIVE_TO_ZERO_FOLD then foldCount =0;
 * 2) if passiveCount is bigger than foldCount passiveCount += foldCount; foldCount = 0;
 * 3) if passiveCount is less than foldCount foldCount += passiveCount; passiveCount = 0;
 * For example if output is (5,30,65) this class create advice (0,30,70).
 * For example if output is (45,50,5) this class create advice (0,95,5).
 *
 * @author Andrew
 */
public class SmartPostflopAdviceCreator extends SmartAdviceCreator {

    private final static int MIN_FOLD_TO_OTHER_ZERO = 60;

    public SmartPostflopAdviceCreator() {
        super();
    }

    @Override
    public Advice create(boolean canRaise, double... output) {
        Advice result = super.create(canRaise, output);

        int foldCount = result.getFoldPercent();
        int passiveCount = result.getPassivePercent();
        int aggressiveCount = result.getAggressivePercent();

        //rules of this creator
        if (foldCount >= MIN_FOLD_TO_OTHER_ZERO) {
            foldCount = 100;
            passiveCount = 0;
            aggressiveCount = 0;
        }
        return new Advice(foldCount, passiveCount, aggressiveCount);
    }
}
