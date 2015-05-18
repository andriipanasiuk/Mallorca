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
import mallorcatour.bot.modeller.BasePokerNN;
import mallorcatour.core.game.LimitType;
import mallorcatour.grandtorino.nn.sparbotFL.SparbotNeurals;
import mallorcatour.robot.humanadvisor.IHumanAdvisor;

/**
 *
 * @author Andrew
 */
public class SparbotNeuralFactory implements IBotFactory {

    public IPlayer createBot(IVillainModeller modeller, ISpectrumListener spectrumListener, IDecisionListener decisionListener, String debug) {
        return new GrandtorinoBot(new BasePokerNN(new SparbotNeurals(), true, 20),
                modeller, LimitType.FIXED_LIMIT, spectrumListener, decisionListener,
                IHumanAdvisor.EMPTY, false, false, false, debug);
    }
}
