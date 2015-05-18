/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.math;

import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.interfaces.IVillainModeller;
import mallorcatour.bot.modeller.BasePokerNN;
import mallorcatour.grandtorino.nn.danielxn.DanielxnNeurals;

/**
 *
 * @author Andrew
 */
public class DanielxnPostflopBotFactory implements IBotFactory {

    public IPlayer createBot(IVillainModeller modeller, ISpectrumListener spectrumListener, IDecisionListener decisionListener, String debug) {
        return new MixBot(new BasePokerNN(new DanielxnNeurals(), false), modeller,
                spectrumListener, decisionListener, debug);
    }
}
