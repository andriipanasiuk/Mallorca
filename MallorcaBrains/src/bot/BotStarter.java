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

import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.neural.NeuralBotFactory;
import mallorcatour.brains.IAdvisor;
import mallorcatour.core.equilator.preflop.EquilatorPreflop;
import mallorcatour.core.equilator.preflop.EquilatorPreflop.LoadFrom;
import mallorcatour.util.Log;

/**
 * This class is the brains of your bot. Make your calculations here and return
 * the best move with GetMove
 */
public class BotStarter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Log.WRITE_TO_ERR = true;
		EquilatorPreflop.loadFrom = LoadFrom.CODE;
		NeuralBotFactory factory = new NeuralBotFactory();
		BotParser parser = new BotParser(factory.createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY,
				IDecisionListener.EMPTY, ""));
		parser.run();
	}

}
