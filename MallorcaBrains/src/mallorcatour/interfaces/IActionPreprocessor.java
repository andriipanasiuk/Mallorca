/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.interfaces;

import mallorcatour.core.bot.IGameInfo;
import mallorcatour.game.core.Action;

/**
 *
 * @author Andrew
 */
public interface IActionPreprocessor {

    Action preprocessAction(Action action, IGameInfo gameInfo,
            String villainName);
}
