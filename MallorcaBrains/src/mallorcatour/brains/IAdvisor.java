/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mallorcatour.brains;

import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.interfaces.IPokerStats;
import mallorcatour.core.game.situation.LocalSituation;

/**
 * 
 * @author Andrew
 */
public interface IAdvisor extends IPokerStats {

	/**
	 * Return advice for action in any situation with hole cards.
	 */
	Advice getAdvice(LocalSituation situation, HoleCards cards);

	String getName();

	public static final IAdvisor UNSUPPORTED = new IAdvisor() {

		@Override
		public double getWtsd() {
			throw new UnsupportedOperationException();
		}

		@Override
		public double getFoldFrequency() {
			throw new UnsupportedOperationException();
		}

		@Override
		public double getAggressionFrequency() {
			throw new UnsupportedOperationException();
		}

		@Override
		public double getAggressionFactor() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getName() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Advice getAdvice(LocalSituation situation, HoleCards cards) {
			throw new UnsupportedOperationException();
		}
	};
}
