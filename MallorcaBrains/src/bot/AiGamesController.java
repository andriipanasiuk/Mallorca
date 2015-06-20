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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import mallorcatour.bot.C;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.GameInfo;
import mallorcatour.core.game.OpenPlayerInfo;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.tools.FileUtils;
import mallorcatour.tools.Log;

/**
 * Class that parses strings given by the engine and stores values for later
 * use.
 */
public class AiGamesController {

	private int round;

	private Map<String, String> settings = new HashMap<String, String>();

	/**
	 * Used for internal needs. For external we use bot.getName().
	 */
	private String heroName = null;
	private String villainName = null;

	private GameInfo gameInfo;
	private IPlayer bot;

	public AiGamesController(GameInfo gameInfo, IPlayer observer) {
		this.gameInfo = gameInfo;
		this.bot = observer;
	}

	void updateSetting(String key, String value) {
		settings.put(key, value);
		if (key.equals("your_bot")) {
			if (value.equals("player1")) {
				villainName = "player2";
			} else {
				villainName = "player1";
			}
			heroName = value;
			gameInfo.heroInfo = new OpenPlayerInfo(bot.getName());
			gameInfo.villainInfo = new OpenPlayerInfo(villainName);
		} else if (key.equals("timebank")) {
		} else if (key.equals("time_per_move")) {
		} else if (key.equals("hands_per_level")) {
		} else if (key.equals("starting_stack")) {
			int stack = Integer.valueOf(value);
			gameInfo.bankrollAtRisk = stack;
		} else {
			System.err.printf("Unknown settings command: %s %s" + FileUtils.LINE_SEPARATOR, key, value);
		}
	}

	void updateMatch(String key, String value) {
		if (key.equals("round")) {
			round = Integer.valueOf(value);
			System.err.println("Round " + round);
			resetRoundVariables();
		} else if (key.equals("small_blind")) {
		} else if (key.equals("big_blind")) {
			gameInfo.bigBlind = Integer.valueOf(value);
		} else if (key.equals("on_button")) { 
			gameInfo.onButton = value.equals(heroName);
			gameInfo.heroInfo.isOnButton = value.equals(heroName);
			gameInfo.villainInfo.isOnButton = !value.equals(heroName);

		} else if (key.equals("max_win_pot")) {
			gameInfo.pot = Integer.valueOf(value);
			Log.d(C.POT + ": " + gameInfo.pot);
		} else if (key.equals("amount_to_call")) {
			gameInfo.heroAmountToCall = Integer.valueOf(value);
			Log.d(C.TO_CALL + ": " + gameInfo.heroAmountToCall);
		} else if (key.equals(C.TABLE_AIGAMES)) {
			gameInfo.board = Arrays.asList(parseCards(value));
			Log.d(C.TABLE + ": " + value);
			PokerStreet street;
			if (gameInfo.board.size() == 3) {
				street = PokerStreet.FLOP;
			} else if (gameInfo.board.size() == 4) {
				street = PokerStreet.TURN;
			} else if (gameInfo.board.size() == 5) {
				street = PokerStreet.RIVER;
			} else {
				throw new IllegalArgumentException("Not correct count of board cards");
			}
			gameInfo.changeStreet(street);
			bot.onStageEvent(street);
		} else {
			System.err.printf("Unknown match command: %s %s" + FileUtils.LINE_SEPARATOR, key, value);
		}
	}

	void updateMove(String bot, String key, String amountStr) {
		if (key.equals(C.STACK)) {
			int value = Integer.valueOf(amountStr);
			if (value < gameInfo.bankrollAtRisk) {
				gameInfo.bankrollAtRisk = value;
			}
		}
		if (key.equals(C.RAISE)) {
			int amount = Integer.valueOf(amountStr);
			gameInfo.bankrollAtRisk = Math.max(0, gameInfo.bankrollAtRisk - amount);
		}
		if (bot.equals(heroName)) {
			if (key.equals(C.STACK)) {
				gameInfo.changeStreet(PokerStreet.PREFLOP);
				this.bot.onHandStarted(gameInfo);
			} else if (key.equals(C.POST)) {
				gameInfo.bankrollAtRisk -= gameInfo.bigBlind;
				Log.d("Effective " + C.STACK + ": " + gameInfo.bankrollAtRisk);
			} else if (key.equals(C.HAND)) { // Your cards
				Card[] cards = parseCards(amountStr);
				Log.d(C.HOLE_CARDS + ": " + amountStr);
				this.bot.onHoleCards(cards[0], cards[1]);
			} else {
				// That should be all
			}
		} else {
			if (key.equals(C.POST)) {
				// do nothing
			} else if (key.equals(C.HAND)) {
				// Hand of the opponent on a showdown, not stored
			} else if (key.equals(C.FOLD) || key.equals(C.CHECK) || key.equals(C.CALL) || key.equals(C.RAISE)) {
				int amount = Integer.valueOf(amountStr);
				Log.d(C.VILLAIN + " " + key + " " + (amount != 0 ? amount : ""));
				Action action = fromString(key, amountStr);
				this.bot.onActed(action, -1, villainName);
			}
		}
		if (key.equals(C.WINS)) {
			this.bot.onHandEnded();
		}
		if (key.equals(C.RAISE)) {
			Log.d("Effective " + C.STACK  + " after " + C.RAISE + ": " + gameInfo.bankrollAtRisk);
		}
	}

	private static Action fromString(String key, String amount) {
		Action result;
		if (key.equals(C.FOLD)) {
			result = Action.fold();
		} else if (key.equals(C.CALL)) {
			result = Action.callAction(Integer.valueOf(amount));
		} else if (key.equals(C.CHECK)) {
			result = Action.checkAction();
		} else if (key.equals(C.RAISE)) {
			result = Action.raiseAction(Integer.valueOf(amount));
		} else {
			throw new IllegalArgumentException("Invalid key from engine: " + key);
		}
		return result;
	}

	/**
	 * Parse the input string from the engine to actual Card objects
	 * 
	 * @param String
	 *            value : input
	 * @return Card[] : array of Card objects
	 */
	private Card[] parseCards(String value) {
		if (value.endsWith("]")) {
			value = value.substring(0, value.length() - 1);
		}
		if (value.startsWith("[")) {
			value = value.substring(1);
		}
		if (value.length() == 0) {
			return new Card[0];
		}
		String[] parts = value.split(",");
		Card[] cards = new Card[parts.length];
		for (int i = 0; i < parts.length; ++i) {
			cards[i] = Card.valueOf(parts[i]);
		}
		return cards;
	}

	/**
	 * Reset all the variables at the start of the round, just to make sure we
	 * don't use old values
	 */
	private void resetRoundVariables() {
		gameInfo.bigBlind = 0;
		gameInfo.pot = 0;
		gameInfo.resetStreetPots();
		gameInfo.board = Collections.emptyList();
		gameInfo.heroAmountToCall = 0;
		gameInfo.bankrollAtRisk = Double.MAX_VALUE;
	}

}
