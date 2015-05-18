/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mallorcatour.bot.interfaces;

import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.situation.LocalSituation;

/**
 *
 * @author Andrew
 */
public interface IPokerNN {

    Advice getAdvice(LocalSituation situation, HoleCards cards);
}
