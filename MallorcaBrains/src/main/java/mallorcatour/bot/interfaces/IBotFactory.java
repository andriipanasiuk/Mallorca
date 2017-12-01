/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.interfaces;

import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.player.interfaces.Player;


/**
 *
 * @author Andrew
 */
public interface IBotFactory {

    Player createBot(AdvisorListener villainListener, AdvisorListener heroListener, String name, String debug);
}
