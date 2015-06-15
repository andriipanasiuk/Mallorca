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
	private String heroName = "";
	private String villainName = "";

	@SuppressWarnings("unused")
	private int timeBank, timePerMove;

	private GameInfo gameInfo;
	private IPlayer bot;

	public AiGamesController(GameInfo gameInfo, IPlayer observer) {
		this.gameInfo = gameInfo;
		this.bot = observer;
	}

	/**
	 * Parses the settings for this game
	 * 
	 * @param key
	 *            : key of the information given
	 * @param value
	 *            : value to be set for the key
	 */
	protected void updateSetting(String key, String value) {
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
		} else if (key.equals("timebank")) { // Maximum amount of time your bot
												// can take for one response
			timeBank = Integer.valueOf(value);
		} else if (key.equals("time_per_move")) { // The extra amount of time
													// you get per response
			timePerMove = Integer.valueOf(value);
		} else if (key.equals("hands_per_level")) { // Number of rounds before
													// the blinds are increased
		} else if (key.equals("starting_stack")) { // Starting stack for each
													// bot
			int stack = Integer.valueOf(value);
			gameInfo.bankrollAtRisk = stack;
		} else {
			System.err.printf("Unknown settings command: %s %s" + FileUtils.LINE_SEPARATOR, key, value);
		}
	}

	/**
	 * Parses the match information
	 * 
	 * @param key
	 *            : key of the information given
	 * @param value
	 *            : value to be set for the key
	 */
	protected void updateMatch(String key, String value) {
		if (key.equals("round")) { // Round number
			round = Integer.valueOf(value);
			System.err.println("Round " + round); // printing the round to the
													// output for debugging
			resetRoundVariables();
		} else if (key.equals("small_blind")) { // Value of the small blind
		} else if (key.equals("big_blind")) { // Value of the big blind
			gameInfo.bigBlind = Integer.valueOf(value);
		} else if (key.equals("on_button")) { // Which bot has the button,
												// onButton is true if it's your
												// bot
			gameInfo.onButton = value.equals(heroName);
			gameInfo.heroInfo.isOnButton = value.equals(heroName);
			gameInfo.villainInfo.isOnButton = !value.equals(heroName);

		} else if (key.equals("max_win_pot")) { // The size of the current pot
			gameInfo.pot = Integer.valueOf(value);
			Log.d(C.POT + ": " + gameInfo.pot);
		} else if (key.equals("amount_to_call")) { // The amount of the call
			gameInfo.heroAmountToCall = Integer.valueOf(value);
		} else if (key.equals(C.TABLE)) { // The cards on the table
			gameInfo.board = Arrays.asList(parseCards(value));
			Log.d("Table: " + value);
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

	/**
	 * Parses the information given about stacks, blinds and moves
	 * 
	 * @param bot
	 *            : bot that this move belongs to (either you or the opponent)
	 * @param key
	 *            : key of the information given
	 * @param amount
	 *            : value to be set for the key
	 */
	protected void updateMove(String bot, String key, String amount) {
		if (key.equals(C.STACK)) { // The amount in your starting stack
			int value = Integer.valueOf(amount);
			if (value < gameInfo.bankrollAtRisk) {
				gameInfo.bankrollAtRisk = value;
			}
		}
		if (key.equals(C.RAISE)) {
			int am = Integer.valueOf(amount);
			gameInfo.bankrollAtRisk = Math.max(0, gameInfo.bankrollAtRisk - am);
		}
		if (bot.equals(heroName)) {
			if (key.equals(C.STACK)) { // The amount in your starting stack
				gameInfo.changeStreet(PokerStreet.PREFLOP);
				this.bot.onHandStarted(gameInfo);
			} else if (key.equals(C.POST)) {
				gameInfo.bankrollAtRisk -= gameInfo.bigBlind;
				Log.d("Effective " + C.STACK + ": " + gameInfo.bankrollAtRisk);
			} else if (key.equals(C.HAND)) { // Your cards
				Card[] cards = parseCards(amount);
				Log.d("HoleCards: " + amount);
				this.bot.onHoleCards(cards[0], cards[1]);
			} else if (key.equals(C.WINS)) {
				this.bot.onHandEnded();
			} else {
				// That should be all
			}
		} else { // assume it's the opponent
			if (key.equals(C.POST)) { // The amount your opponent paid
										// for the blind
				// do nothing
			} else if (key.equals(C.HAND)) {
				// Hand of the opponent on a showdown, not stored
			} else if (key.equals(C.WINS)) {
				this.bot.onHandEnded();
			} else if (key.equals(C.FOLD) || key.equals(C.CHECK) || key.equals(C.CALL) || key.equals(C.RAISE)) {
				Log.d(C.VILLAIN + " " + key + " " + amount);
				Action action = fromString(key, amount);
				this.bot.onActed(action, -1, villainName);
			}
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
		gameInfo.board = Collections.emptyList();
		gameInfo.heroAmountToCall = 0;
		gameInfo.bankrollAtRisk = Double.MAX_VALUE;
	}

}
