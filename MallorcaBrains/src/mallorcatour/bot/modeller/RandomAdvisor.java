package mallorcatour.bot.modeller;

import mallorcatour.brains.IAdvisor;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.game.interfaces.IPokerStats;
import mallorcatour.core.game.situation.LocalSituation;

public class RandomAdvisor implements IAdvisor {

	@Override
	public IPokerStats getStats() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IAdvice getAdvice(LocalSituation situation, HoleCards cards, IPlayerGameInfo gameInfo) {
		int fold = 1, passive = 1, aggressive = 1;
		if (!situation.canRaise()) {
			aggressive = 0;
		}
		if (situation.getPotOdds() == 0) {
			fold = 0;
		}
		return Advice.create(fold, passive, aggressive);
	}

	@Override
	public String getName() {
		return "Random advisor";
	}

}
