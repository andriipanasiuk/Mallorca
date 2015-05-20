/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.game.situation;

import mallorcatour.core.game.IGameObserver;
import mallorcatour.core.game.IHeroObserver;

/**
 *
 * @author Andrew
 */
public interface ISituationHandler extends IHeroObserver, IGameObserver {

    LocalSituation onHeroSituation();
}
