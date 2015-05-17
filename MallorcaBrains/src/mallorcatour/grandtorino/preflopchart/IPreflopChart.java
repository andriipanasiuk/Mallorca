/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.grandtorino.preflopchart;

import mallorcatour.game.core.Action;
import mallorcatour.game.core.HoleCards;
import mallorcatour.game.situation.LocalSituation;

/**
 *
 * @author Andrew
 */
public interface IPreflopChart {

    Action getAction(LocalSituation situation, HoleCards cards);
}
