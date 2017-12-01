package mallorcatour.core.game.advice;

import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.interfaces.GameContext;
import mallorcatour.core.game.state.HandState;

public interface Advisor {

	/**
	 * Return advice for action in any state with hole cards.
	 */
	IAdvice getAdvice(HandState situation, HoleCards cards, GameContext gameInfo);

	Advisor UNSUPPORTED = (situation, cards, gameInfo) -> null;
}
