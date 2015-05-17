/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.grandtorino.mathbot;

import mallorcatour.core.bot.IBotFactory;
import mallorcatour.core.bot.IPlayer;
import mallorcatour.game.situation.IDecisionListener;
import mallorcatour.game.situation.ISpectrumListener;
import mallorcatour.grandtorino.nn.danielxn.DanielxnNeurals;
import mallorcatour.grandtorino.nn.modeller.BasePokerNN;
import mallorcatour.grandtorino.nn.modeller.BaseVillainModeller;

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
