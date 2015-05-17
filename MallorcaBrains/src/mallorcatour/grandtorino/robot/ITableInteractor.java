/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.grandtorino.robot;

import mallorcatour.game.core.Action;

/**
 *
 * @author Andrew
 */
public interface ITableInteractor {
  
    void onCreate(boolean activate);

    void doAction(Action action);
}
