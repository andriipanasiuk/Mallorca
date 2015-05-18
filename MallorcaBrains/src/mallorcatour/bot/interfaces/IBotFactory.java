/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.interfaces;

import mallorcatour.core.game.situation.IDecisionListener;
import mallorcatour.core.game.situation.ISpectrumListener;
import mallorcatour.bot.modeller.BaseVillainModeller;

/**
 *
 * @author Andrew
 */
public interface IBotFactory {

    IPlayer createBot(BaseVillainModeller modeller, ISpectrumListener spectrumListener,
            IDecisionListener decisionListener, String debug);
}
