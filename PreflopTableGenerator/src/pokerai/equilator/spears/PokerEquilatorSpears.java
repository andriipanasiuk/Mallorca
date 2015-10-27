/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pokerai.equilator.spears;

import pokerai.game.eval.spears2p2.StateTableEvaluator;

/**
 *
 */
public class PokerEquilatorSpears {

	public static long time = 0;
	public static long timeCount = 0;

	static {
		StateTableEvaluator.initialize();
	}

	public static int combination(int[] allCards) {
		int numCards = allCards.length;
		{
			// Find the hand value
			int u = 53;
			for (int ii = 0; ii < numCards; ++ii) {
				u = StateTableEvaluator.handRanks[u + allCards[ii] + 1];
			}
			// Extra lookup pre-river
			if (numCards < 7) {
				throw new IllegalArgumentException();
				// return StateTableEvaluator.handRanks[u + 1];
			}
			return u;
		}
	}

}
