/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.math;

import java.util.Map.Entry;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.math.RandomVariable;

/**
 *
 * @author Andrew
 */
public class MostProfitActionFromMap extends BaseAdviceCreatorFromMap {

	@Override
    public IAdvice create(ActionDistribution map, IGameInfo gameInfo, HoleCards cards) {
        double passive = 0;
        Double aggressive = null;
        for (Entry<Action, RandomVariable> entry : map.entrySet()) {
        	RandomVariable value = entry.getValue();
            if (entry.getKey().isPassive()) {
                passive = value.getEV();
			} else if (entry.getKey().isAggressive()) {
				aggressive = value != null ? value.getEV() : null;
			} else {
            	//do nothing
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
        if (aggressivePercent > passivePercent) {
            passivePercent = 0;
        }
        if (aggressivePercent < passivePercent) {
            aggressivePercent = 0;
        }
        return Advice.create(foldPercent, passivePercent, aggressivePercent);
    }
}
