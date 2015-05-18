/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.math;

import java.util.Map;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.advice.Advice;

/**
 *
 * @author Andrew
 */
public abstract class BaseAdviceCreatorFromMap {

    abstract Advice create(Map<Action, Double> map);
}
