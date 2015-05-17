/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.grandtorino.mathbot;

import java.util.Map;
import mallorcatour.game.advice.Advice;
import mallorcatour.game.core.Action;

/**
 *
 * @author Andrew
 */
public abstract class BaseAdviceCreatorFromMap {

    abstract Advice create(Map<Action, Double> map);
}
