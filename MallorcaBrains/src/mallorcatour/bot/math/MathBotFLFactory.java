/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.math;

import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.brains.IAdvisor;

/**
 *
 * @author Andrew
 */
public class MathBotFLFactory implements IBotFactory {

    public IPlayer createBot(IAdvisor modeller, ISpectrumListener spectrumListener, IDecisionListener decisionListener, String name, String debug) {
    	throw new RuntimeException();
    }
}
