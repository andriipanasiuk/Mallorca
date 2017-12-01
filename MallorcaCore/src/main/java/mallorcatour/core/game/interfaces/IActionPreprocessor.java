/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.game.interfaces;

import mallorcatour.core.game.Action;

/**
 * Сделать одним интерфейсом с IActionChecker.
 */
public interface IActionPreprocessor {

    Action preprocessAction(Action action, GameContext gameInfo, double toCall);
}
