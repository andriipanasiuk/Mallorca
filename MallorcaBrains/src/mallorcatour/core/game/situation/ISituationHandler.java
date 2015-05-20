/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.game.situation;

import mallorcatour.core.game.interfaces.IGameObserver;
import mallorcatour.core.game.interfaces.IHeroObserver;

/**
 *
 * @author Andrew
 */
public interface ISituationHandler extends IHeroObserver, IGameObserver {

    LocalSituation onHeroSituation();
}
