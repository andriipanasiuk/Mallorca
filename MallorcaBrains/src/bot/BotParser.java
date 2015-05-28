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

import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.neural.NeuralAggroBotFactory;
import mallorcatour.brains.IAdvisor;
import mallorcatour.core.equilator.preflop.EquilatorPreflop;
import mallorcatour.core.equilator.preflop.EquilatorPreflop.LoadFrom;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.GameInfo;
import mallorcatour.util.Log;

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
		GameInfo gameInfo = new GameInfo();
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
			if (action.isCheck()) {
				act = "check";
			} else {
				act = "call";
			}
		} else {
			act = "raise";
		}
		StringBuilder builder = new StringBuilder();
		builder.append(act);
		builder.append(" ");
		builder.append((int) action.getAmount());
		return builder.toString();
	}

	public static void main(String[] args) {
		Log.WRITE_TO_ERR = true;
		EquilatorPreflop.loadFrom = LoadFrom.CODE;
		NeuralAggroBotFactory factory = new NeuralAggroBotFactory();
		BotParser parser = new BotParser(factory.createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY,
				IDecisionListener.EMPTY, "Mallorca", ""));
		parser.run();
	}
}
