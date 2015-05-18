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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import mallorcatour.core.equilator13.EquilatorPreflop;
import mallorcatour.core.equilator13.PokerEquilatorBrecher;
import mallorcatour.game.advice.Advice;
import mallorcatour.game.core.Action;
import mallorcatour.game.core.Card;
import mallorcatour.game.core.HoleCards;

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
			strength = EquilatorPreflop.strengthByFormula(cards.first, cards.second);
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
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					BotStarter.class.getResourceAsStream("test.txt")));
			String line = reader.readLine();
			System.err.println(line);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}

}
