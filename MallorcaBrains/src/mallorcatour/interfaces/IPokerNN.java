/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mallorcatour.interfaces;

import mallorcatour.game.advice.Advice;
import mallorcatour.game.core.HoleCards;
import mallorcatour.game.situation.LocalSituation;

/**
 *
 * @author Andrew
 */
public interface IPokerNN {

    Advice getAdvice(LocalSituation situation, HoleCards cards);
}
