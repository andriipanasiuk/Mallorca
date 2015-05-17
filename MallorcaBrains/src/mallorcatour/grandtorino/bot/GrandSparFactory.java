/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.grandtorino.bot;

import mallorcatour.core.bot.IBotFactory;
import mallorcatour.core.bot.IPlayer;
import mallorcatour.core.bot.LimitType;
import mallorcatour.game.situation.IDecisionListener;
import mallorcatour.game.situation.ISpectrumListener;
import mallorcatour.grandtorino.nn.modeller.BasePokerNN;
import mallorcatour.grandtorino.nn.modeller.BaseVillainModeller;
import mallorcatour.grandtorino.nn.sparbotFL.SparbotNeurals;

/**
 *
 * @author Andrew
 */
public class GrandSparFactory implements IBotFactory {

    public IPlayer createBot(BaseVillainModeller modeller, ISpectrumListener spectrumListener, IDecisionListener decisionListener, String debug) {
        return new GrandtorinoBot(new BasePokerNN(new SparbotNeurals(), true, 20),
                modeller, LimitType.FIXED_LIMIT, spectrumListener, decisionListener,
                IHumanAdvisor.EMPTY, false, false, false, debug);
    }
}
