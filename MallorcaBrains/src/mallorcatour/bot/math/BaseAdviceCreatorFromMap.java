/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.math;

import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IGameInfo;

/**
 *
 * @author Andrew
 */
public abstract class BaseAdviceCreatorFromMap {

    abstract IAdvice create(ActionDistribution map, IGameInfo gameInfo);
}
