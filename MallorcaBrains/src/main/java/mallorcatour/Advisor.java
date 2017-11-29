/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mallorcatour;

import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.game.situation.HandState;
import mallorcatour.core.player.interfaces.HasName;

public interface Advisor extends HasName {

	/**
	 * Return advice for action in any situation with hole cards.
	 * 
	 * @param gameInfo
	 *            TODO
	 */
	IAdvice getAdvice(HandState situation, HoleCards cards, IPlayerGameInfo gameInfo);

	Advisor UNSUPPORTED = new Advisor() {

		@Override
		public String getName() {
			throw new UnsupportedOperationException();
		}

		@Override
		public IAdvice getAdvice(HandState situation, HoleCards cards, IPlayerGameInfo gameInfo) {
			return null;
		}
	};
}
