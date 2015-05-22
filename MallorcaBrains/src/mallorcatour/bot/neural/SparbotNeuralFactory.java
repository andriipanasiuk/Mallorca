/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.neural;

import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.interfaces.IVillainModeller;

/**
 * 
 * @author Andrew
 */
public class SparbotNeuralFactory implements IBotFactory {

	public IPlayer createBot(IVillainModeller modeller, ISpectrumListener spectrumListener,
			IDecisionListener decisionListener, String debug) {
		// TODO implement when neurals will be ready
		return null;
	}
}
