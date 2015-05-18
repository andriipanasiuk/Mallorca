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

import java.util.HashMap;
import java.util.Map;

import mallorcatour.core.game.Card;
import mallorcatour.core.game.HoleCards;

/**
 * Class that parses strings given by the engine and stores values for later use.
 */
public class BotState {
	
	private int round, smallBlind, bigBlind;
	
	private boolean onButton;
	
	private int myStack, opponentStack;
	
	private int pot;
	
	private PokerMove opponentMove;
	
	private int currentBet;
	
	private int amountToCall;
	
	private HoleCards holeCards;
	
	private Card[] table;
	
	private Map<String,String> settings = new HashMap<String,String>();
	
	private String myName = "";
	
	private int[] sidepots;
	
	private int timeBank, timePerMove;
	
	private int handsPerLevel;
	
	/**
	 * Parses the settings for this game
	 * @param key : key of the information given
	 * @param value : value to be set for the key
	 */
	protected void updateSetting(String key, String value) {
		settings.put(key, value);
		if( key.equals("your_bot") ) {
			myName = value;
		} else if ( key.equals("timebank") ) {			// Maximum amount of time your bot can take for one response
			timeBank = Integer.valueOf(value);
		} else if ( key.equals("time_per_move") ) {		// The extra amount of time you get per response
			timePerMove = Integer.valueOf(value);
		} else if ( key.equals("hands_per_level") ) {	// Number of rounds before the blinds are increased
			handsPerLevel = Integer.valueOf(value);
		} else if ( key.equals("starting_stack") ) {	// Starting stack for each bot
			myStack = Integer.valueOf(value);
			opponentStack = Integer.valueOf(value);
		} else {
			System.err.printf("Unknown settings command: %s %s\n", key, value);
		}
	}

	/**
	 * Parses the match information
	 * @param key : key of the information given
	 * @param value : value to be set for the key
	 */
	protected void updateMatch(String key, String value) {
		if( key.equals("round") ) { 				// Round number
			round = Integer.valueOf(value);
			System.err.println("Round " + round);   //printing the round to the output for debugging
            resetRoundVariables();
		} else if( key.equals("small_blind") ) {	// Value of the small blind
			smallBlind = Integer.valueOf(value);
		} else if( key.equals("big_blind") ) {		// Value of the big blind
			bigBlind = Integer.valueOf(value);
		} else if( key.equals("on_button") ) {		// Which bot has the button, onButton is true if it's your bot
			onButton = value.equals(myName);
		} else if( key.equals("max_win_pot") ) {	// The size of the current pot
			pot = Integer.valueOf(value);
		} else if( key.equals("amount_to_call") ) {	// The amount of the call
			amountToCall = Integer.valueOf(value);
		} else if ( key.equals("table") ) {			// The cards on the table
			table = parseCards(value);
		} else {
			System.err.printf("Unknown match command: %s %s\n", key, value);
		}
	}

	/**
	 * Parses the information given about stacks, blinds and moves
	 * @param bot : bot that this move belongs to (either you or the opponent)
	 * @param key : key of the information given
	 * @param amount : value to be set for the key
	 */
	protected void updateMove(String bot, String key, String amount) {
		if( bot.equals(myName) ) {
			if( key.equals("stack") ) {					// The amount in your starting stack
				myStack = Integer.valueOf(amount);
			}
			else if ( key.equals("post") ) {			// The amount you have to pay for the blind
				myStack -= Integer.valueOf(amount);
			}
			else if( key.equals("hand") ) {				// Your cards
				Card[] cards = parseCards(amount);
				holeCards = new HoleCards(cards[0], cards[1]);
			} else if ( key.equals("wins") ) {
				// Your winnings, not stored
			} else {
				// That should be all
			}
		} else { // assume it's the opponent
			if( key.equals("stack") ) {					// The amount in your opponent's starting stack
				opponentStack = Integer.valueOf(amount);
			} else if ( key.equals("post") ) {			// The amount your opponent paid for the blind
				opponentStack -= Integer.valueOf(amount);
			} else if ( key.equals("hand")){
				// Hand of the opponent on a showdown, not stored
			} else if ( key.equals("wins") ) {
				// Opponent winnings, not stored
			} else {									// The move your opponent did
                opponentMove = new PokerMove(bot, key, Integer.valueOf(amount));					
			}
		}
	}

	/**
	 * Parse the input string from the engine to actual Card objects
	 * @param String value : input
	 * @return Card[] : array of Card objects
	 */
	private Card[] parseCards(String value) {
		if( value.endsWith("]") ) { value = value.substring(0, value.length()-1); }
		if( value.startsWith("[") ) { value = value.substring(1); }
		if( value.length() == 0 ) { return new Card[0]; }
		String[] parts = value.split(",");
		Card[] cards = new Card[parts.length];
		for( int i = 0; i < parts.length; ++i ) {
			cards[i] = Card.valueOf(parts[i]);
		}
		return cards;
	}
	
	/**
	 * Reset all the variables at the start of the round,
	 * just to make sure we don't use old values
	 */
	private void resetRoundVariables() {
		smallBlind = 0;
		bigBlind = 0;
		pot = 0;
		opponentMove = null;
		amountToCall = 0;
		holeCards = null;
		table = new Card[0];
	}

	public int getRound() {
		return round;
	}

	public int getSmallBlind() {
		return smallBlind;
	}

	public int getBigBlind() {
		return bigBlind;
	}

	public boolean onButton() {
		return onButton;
	}

	public int getmyStack() {
		return myStack;
	}

	public int getOpponentStack() {
		return opponentStack;
	}
	
	public int getPot() {
		return pot;
	}
	
	public PokerMove getOpponentAction() {
		return opponentMove;
	}
	
	public int getCurrentBet() {
		return currentBet;
	}

	public HoleCards getHand() {
		return holeCards;
	}

	public Card[] getTable() {
		return table;
	}
	
	public String getSetting(String key) {
		return settings.get(key);
	}

	public int[] getSidepots() {
		return sidepots;
	}

	public String getMyName() {
		return myName;
	}
	
	public int getAmountToCall() {
		return amountToCall;
	}

}
