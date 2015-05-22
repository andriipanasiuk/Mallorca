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

import java.util.Scanner;

import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.core.game.Action;
import mallorcatour.robot.controller.HUGameInfo;

/**
 * Class that reads the engine's input and asks the bot Class to calculate the
 * next move. Then returns that move to the engine.
 */
public class BotParser {

	final Scanner scan;

	final IPlayer bot;

	public BotParser(IPlayer bot) {
		this.scan = new Scanner(System.in);
		this.bot = bot;
	}

	public void run() {
		HUGameInfo gameInfo = new HUGameInfo();
		AiGamesController controller = new AiGamesController(gameInfo, bot);
		while (scan.hasNextLine()) {
			String line = scan.nextLine().trim();
			if (line.length() == 0) {
				continue;
			}
			String[] parts = line.split("\\s+");
			if (parts.length == 3 && parts[0].equals("Action")) {
				Action move = bot.getAction();
				System.out.println(actionToString(move));
				System.out.flush();
			} else if (parts.length == 3 && parts[0].equals("Settings")) {
				controller.updateSetting(parts[1], parts[2]);
			} else if (parts.length == 3 && parts[0].equals("Match")) { 
				controller.updateMatch(parts[1], parts[2]);
			} else if (parts.length == 3 && parts[0].startsWith("player")) { 
				controller.updateMove(parts[0], parts[1], parts[2]);
			} else {
				System.err.printf("Unable to parse line ``%s''\n", line);
			}
		}
	}

	private static String actionToString(Action action) {
		String act;
		if (action.isFold()) {
			act = "fold";
		} else if (action.isPassive()) {
			act = "call";
		} else {
			act = "raise";
		}
		return String.format("%s %d", act, action.getAmount());
	}
}
