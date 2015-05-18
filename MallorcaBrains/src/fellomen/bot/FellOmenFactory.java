/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fellomen.bot;

import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.core.game.situation.IDecisionListener;
import mallorcatour.core.game.situation.ISpectrumListener;
import mallorcatour.bot.modeller.BaseVillainModeller;

/**
 *
 * @author Andrew
 */
public class FellOmenFactory implements IBotFactory {

    public IPlayer createBot(BaseVillainModeller modeller, ISpectrumListener spectrumListener, IDecisionListener decisionListener, String debug) {
        return new FellOmen2(debug);
    }
}
