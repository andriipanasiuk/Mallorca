package mallorcatour.core.game.advice;


public class AggroAdviceCreator extends SmartPostflopAdviceCreator {
	@Override
    public Advice create(boolean canRaise, double... output) {
        Advice result = super.create(canRaise, output);

        int foldCount = result.getFoldPercent();
        int passiveCount = result.getPassivePercent();
        int aggressiveCount = result.getAggressivePercent();

		if (aggressiveCount > 20) {
			aggressiveCount += passiveCount;
			passiveCount = 0;
		}
        return new Advice(foldCount, passiveCount, aggressiveCount);
    }
}