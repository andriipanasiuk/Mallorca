/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.game.interfaces;

import mallorcatour.core.game.Action;

/**
 *
 * @author Andrew
 */
public interface IActionPreprocessor {

    Action preprocessAction(Action action, IPlayerGameInfo gameInfo);
}
