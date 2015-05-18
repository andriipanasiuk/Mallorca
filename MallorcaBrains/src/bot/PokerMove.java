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

/**
 * Class that represents the action of a bot.
 */
public class PokerMove {
	
	String player = null;
	String action = null;
	int amount;

	public PokerMove(String botName, String act, int amt)
	{
		player = botName;
		action = act;
		amount = amt;
	}

	public String getPlayer()
	{
		return player;
	}

	public String getAction()
	{
		return action;
	}
	
	public int getAmount()
	{
		return amount;
	}

	/**
	 * Returns a string representation of the move as a sentence of two words, being the action
	 * string and the action amount. Returning the player name to the engine is not needed
	 */
	public String toString() {
		return String.format("%s %d", action, amount);
	}
	
}
