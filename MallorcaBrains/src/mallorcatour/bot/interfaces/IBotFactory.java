/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.interfaces;

import mallorcatour.bot.AdvisorListener;
import mallorcatour.brains.IAdvisor;


/**
 *
 * @author Andrew
 */
public interface IBotFactory {

    IPlayer createBot(IAdvisor villainModel, ISpectrumListener spectrumListener,
            AdvisorListener villainListener, AdvisorListener heroListener, String name, String debug);
}
