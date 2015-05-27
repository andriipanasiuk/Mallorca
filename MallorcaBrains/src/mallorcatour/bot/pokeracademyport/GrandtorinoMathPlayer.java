/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.pokeracademyport;

import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.neural.NeuralBotFactory;

/**
 * 
 * @author Andrew
 */
public class GrandtorinoMathPlayer extends AbstractPABot {

	IPlayer myBot;

	public GrandtorinoMathPlayer() {
		super("MathBot");
		myBot = new NeuralBotFactory().createBot(getVillainModeller(), ISpectrumListener.EMPTY, getDecisionListener(),
				null, getDebugPath());
	}

	@Override
	protected IPlayer getRealBot() {
		return myBot;
	}
}
