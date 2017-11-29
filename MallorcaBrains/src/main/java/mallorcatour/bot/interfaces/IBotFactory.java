/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.interfaces;

import mallorcatour.Advisor;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.interfaces.ISpectrumListener;
import mallorcatour.core.player.interfaces.IPlayer;


/**
 *
 * @author Andrew
 */
public interface IBotFactory {

    IPlayer createBot(Advisor villainModel, ISpectrumListener spectrumListener,
                      AdvisorListener villainListener, AdvisorListener heroListener, String name, String debug);
}
