/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.bot;

import mallorcatour.game.situation.IDecisionListener;
import mallorcatour.game.situation.ISpectrumListener;
import mallorcatour.grandtorino.nn.modeller.BaseVillainModeller;

/**
 *
 * @author Andrew
 */
public interface IBotFactory {

    IPlayer createBot(BaseVillainModeller modeller, ISpectrumListener spectrumListener,
            IDecisionListener decisionListener, String debug);
}
