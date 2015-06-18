/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fellomen.bot;

import mallorcatour.bot.AdvisorListener;
import mallorcatour.bot.interfaces.IBotFactory;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.brains.IAdvisor;

/**
 *
 * @author Andrew
 */
public class FellOmenFactory implements IBotFactory {

    public IPlayer createBot(IAdvisor villainModel, ISpectrumListener spectrumListener, AdvisorListener villainListener, AdvisorListener heroListener, String name, String debug) {
        return new FellOmen2(debug);
    }
}
