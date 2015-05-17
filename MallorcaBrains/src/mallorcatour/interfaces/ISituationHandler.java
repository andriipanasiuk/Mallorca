/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.interfaces;

import mallorcatour.game.situation.LocalSituation;

/**
 *
 * @author Andrew
 */
public interface ISituationHandler extends IGameHandler {

    LocalSituation onHeroSituation();
}
