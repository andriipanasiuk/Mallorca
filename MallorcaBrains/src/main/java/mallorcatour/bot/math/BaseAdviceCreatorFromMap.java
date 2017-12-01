/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.math;

import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.GameContext;

/**
 *
 * @author Andrew
 */
public abstract class BaseAdviceCreatorFromMap {

    abstract IAdvice create(ActionDistribution map, GameContext gameInfo, HoleCards cards);
}
