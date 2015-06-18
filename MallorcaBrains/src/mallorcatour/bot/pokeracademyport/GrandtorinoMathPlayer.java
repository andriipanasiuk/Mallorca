/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.pokeracademyport;

import mallorcatour.bot.IStudent;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.brains.neural.gusxensen.GusXensenFactory;

/**
 * 
 * @author Andrew
 */
public class GrandtorinoMathPlayer extends AbstractPABot {

	IPlayer myBot;

	public GrandtorinoMathPlayer() {
		super("MathBot");
		myBot = new GusXensenFactory().createBot(getVillainModeller(), ISpectrumListener.EMPTY, getDecisionListener(),
				IStudent.NONE, null, getDebugPath());
	}

	@Override
	protected IPlayer getRealBot() {
		return myBot;
	}
}
