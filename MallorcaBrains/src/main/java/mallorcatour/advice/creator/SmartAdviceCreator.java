/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.advice.creator;

import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.advice.AdviceCreator;
import mallorcatour.core.vector.IOutputInterpreter;

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
public class SmartAdviceCreator extends AdviceCreator implements IOutputInterpreter<Advice> {

    private static final int MIN_AGGRESSIVE_TO_ZERO_FOLD = 60;

    public SmartAdviceCreator() {
    }

    @Override
    public Advice create(boolean canRaise, double... output) {
        Advice result = super.create(canRaise, output);

        int foldCount = result.getFoldPercent();
        int passiveCount = result.getPassivePercent();
        int aggressiveCount = result.getAggressivePercent();

        //rules of this creator
        if (!canRaise) {
            passiveCount += aggressiveCount;
            aggressiveCount = 0;
        }
        if (aggressiveCount >= MIN_AGGRESSIVE_TO_ZERO_FOLD) {
            aggressiveCount += foldCount;
            foldCount = 0;
        } else if (passiveCount > foldCount) {
            passiveCount += foldCount;
            foldCount = 0;
        } else if (passiveCount < foldCount) {
            foldCount += passiveCount;
            passiveCount = 0;
        }
        return new Advice(foldCount, passiveCount, aggressiveCount);
    }

	@Override
	public Advice create(double... output) {
		return create(true, output);
	}
}
