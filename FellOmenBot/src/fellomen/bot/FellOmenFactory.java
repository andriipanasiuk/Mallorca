/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fellomen.bot;

import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.brains.IAdvisor;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.interfaces.ISpectrumListener;

/**
 *
 * @author Andrew
 */
public class FellOmenFactory implements IBotFactory {

    public IPlayer createBot(IAdvisor villainModel, ISpectrumListener spectrumListener, AdvisorListener villainListener, AdvisorListener heroListener, String name, String debug) {
        return new FellOmen2(debug);
    }
}
