/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.fellomen.bot;

import mallorcatour.core.bot.IBotFactory;
import mallorcatour.core.bot.IPlayer;
import mallorcatour.game.situation.IDecisionListener;
import mallorcatour.game.situation.ISpectrumListener;
import mallorcatour.grandtorino.nn.modeller.BaseVillainModeller;

/**
 *
 * @author Andrew
 */
public class FellOmenFactory implements IBotFactory {

    public IPlayer createBot(BaseVillainModeller modeller, ISpectrumListener spectrumListener, IDecisionListener decisionListener, String debug) {
        return new FellOmen2(debug);
    }
}
