/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot;

import mallorcatour.core.game.Action;

/**
 *
 * @author Andrew
 */
public interface ITableInteractor {
  
    void onCreate(boolean activate);

    void doAction(Action action);
}
