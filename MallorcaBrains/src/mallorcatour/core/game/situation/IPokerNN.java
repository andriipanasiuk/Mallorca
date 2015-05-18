/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mallorcatour.core.game.situation;

import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.Advice;

/**
 *
 * @author Andrew
 */
public interface IPokerNN {

    Advice getAdvice(LocalSituation situation, HoleCards cards);
}
