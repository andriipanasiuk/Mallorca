/**
 * www.TheAIGames.com 
 * Heads Up Omaha pokerbot
 *
 * Last update: May 07, 2014
 *
 * @author Jim van Eeden, Starapple
 * @version 1.0
 * @License MIT License (http://opensource.org/Licenses/MIT)
 */

package bot;

import mallorcatour.core.equilator.PokerEquilatorBrecher;
import mallorcatour.core.equilator.preflop.simple.EquilatorPreflopSimple;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.util.Log;

/**
 * This class is the brains of your bot. Make your calculations here and return
 * the best move with GetMove
 */
public class BotStarter implements Bot {

	@Override
	public PokerMove getMove(BotState state, Long timeOut) {
		HoleCards cards = state.getHand();
		Card[] table = state.getTable();
		double strength;
		if (table.length == 0) {
			strength = EquilatorPreflopSimple.strengthByFormula(cards.first, cards.second);
		} else {
			strength = PokerEquilatorBrecher.strengthVsRandom(cards.first, cards.second, table);
		}
		Advice advice;
		if (state.getAmountToCall() == 0) {
			if (strength < 0.25) {
				advice = Advice.create(0, 0.9, 0.1);
			} else if (strength < 0.5) {
				advice = Advice.create(0, 0.75, 0.25);
			} else if (strength < 0.75) {
				advice = Advice.create(0, 0.4, 0.6);
			} else {
				advice = Advice.create(0, 0.2, 0.8);
			}
		} else {
			if (strength < 0.3) {
				advice = Advice.create(1, 0, 0);
			} else if (strength < 0.5) {
				advice = Advice.create(0, 1, 0);
			} else if (strength < 0.6) {
				advice = Advice.create(0, 0.8, 0.2);
			} else if (strength < 0.8) {
				advice = Advice.create(0, 0.5, 0.5);
			} else {
				advice = Advice.create(0, 0.2, 0.8);
			}
		}
		Action action = advice.getAction();
		return convert(action, state);
	}

	static PokerMove convert(Action action, BotState state) {
		if (action.isAggressive()) {
			int amount = (int) (0.66 * (state.getPot() + state.getAmountToCall()));
			return new PokerMove(state.getMyName(), "raise", amount);
		} else if (action.isPassive()) {
			return new PokerMove(state.getMyName(), "call", 0);
		} else {
			return new PokerMove(state.getMyName(), "fold", 0);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Log.WRITE_TO_ERR = true;
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}

}
