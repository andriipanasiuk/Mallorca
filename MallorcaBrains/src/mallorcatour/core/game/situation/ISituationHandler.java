/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.game.situation;

import mallorcatour.core.game.interfaces.IGameHandler;

/**
 *
 * @author Andrew
 */
public interface ISituationHandler extends IGameHandler {

    LocalSituation onHeroSituation();
}
