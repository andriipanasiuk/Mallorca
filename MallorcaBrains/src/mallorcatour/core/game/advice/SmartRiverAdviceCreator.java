/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.game.advice;

/**
 * Advice creator that overrides SmartAdviceCreator and adds one rule for river
 * situations. The rule is following:
 * 1) if aggression is bigger than MIN_PERCENT_TO_OTHER_ZERO passive 0 and vice versa;
 * For example if output is (0,25,75) this class create advice (0,0,100).
 *
 * @author Andrew
 */
public class SmartRiverAdviceCreator extends SmartPostflopAdviceCreator {

    private static final int MIN_PERCENT_TO_OTHER_ZERO = 70;
    private final boolean isBetAction;

    public SmartRiverAdviceCreator(boolean isBetAction) {
        super();
        this.isBetAction = isBetAction;
    }

    @Override
    public Advice create(boolean canRaise, double... output) {
        Advice result = super.create(canRaise, output);
        int foldCount = result.getFoldPercent();
        int passiveCount = result.getPassivePercent();
        int aggressiveCount = result.getAggressivePercent();
        if (!isBetAction) {
            if (aggressiveCount >= MIN_PERCENT_TO_OTHER_ZERO) {
                aggressiveCount += passiveCount;
                passiveCount = 0;
            } else if (passiveCount >= MIN_PERCENT_TO_OTHER_ZERO) {
                passiveCount += aggressiveCount;
                aggressiveCount = 0;
            }
        }
        return new Advice(foldCount, passiveCount, aggressiveCount);
    }
}
