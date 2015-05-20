/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.math;

import java.util.Map;
import java.util.Map.Entry;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.advice.Advice;

/**
 *
 * @author Andrew
 */
public class AggroAdviceCreatorFromMap extends BaseAdviceCreatorFromMap {

    public Advice create(Map<Action, Double> map) {
        double passive = 0;
        Double aggressive = null;
        for (Entry<Action, Double> entry : map.entrySet()) {
            if (entry.getKey().isPassive()) {
                passive = entry.getValue();
            } else if (entry.getKey().isAggressive()) {
                aggressive = entry.getValue();
            } else {
                throw new RuntimeException();
            }
        }
        double foldPercent = 0, passivePercent = 0, aggressivePercent = 0;
        if (aggressive == null) {
            if (passive >= 0) {
                passivePercent = 1;
            } else {
                foldPercent = 1;
            }
        } else if (aggressive > 0 && passive > 0) {
            aggressivePercent = aggressive;
            passivePercent = passive;
            if (aggressive > passive) {
                passivePercent = 0;
            }
        } else if (aggressive == 0 && passive == 0) {
            aggressivePercent = 50;
            passivePercent = 50;
        } else if (aggressive > 0) {
            aggressivePercent = aggressive;
            passivePercent = 0;
        } else if (passive > 0) {
            passivePercent = passive;
            aggressivePercent = 0;
        } else if (aggressive == 0) {
            aggressivePercent = 1;
            passivePercent = 0;
        } else if (passive == 0) {
            passivePercent = 1;
            aggressivePercent = 0;
        } else {
            foldPercent = 1;
        }
        return Advice.create(foldPercent, passivePercent, aggressivePercent);
    }
}
