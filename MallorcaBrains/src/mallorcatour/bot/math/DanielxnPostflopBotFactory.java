/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.math;

import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.core.game.situation.IDecisionListener;
import mallorcatour.core.game.situation.ISpectrumListener;
import mallorcatour.grandtorino.nn.danielxn.DanielxnNeurals;
import mallorcatour.bot.modeller.BasePokerNN;
import mallorcatour.bot.modeller.BaseVillainModeller;

/**
 *
 * @author Andrew
 */
public class DanielxnPostflopBotFactory implements IBotFactory {

    public IPlayer createBot(BaseVillainModeller modeller, ISpectrumListener spectrumListener, IDecisionListener decisionListener, String debug) {
        return new MixBot(new BasePokerNN(new DanielxnNeurals(), false), modeller,
                spectrumListener, decisionListener, debug);
    }
}
