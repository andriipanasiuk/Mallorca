package mallorcatour.modeller;

import mallorcatour.core.game.advice.Advisor;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.game.state.HandState;

public class RandomAdvisor implements Advisor {

	@Override
	public IAdvice getAdvice(HandState situation, HoleCards cards, IPlayerGameInfo gameInfo) {
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
	public String toString() {
		return "Random advisor";
	}

}
