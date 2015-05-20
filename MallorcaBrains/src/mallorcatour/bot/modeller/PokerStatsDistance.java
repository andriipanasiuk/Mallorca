/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.modeller;

import mallorcatour.core.game.interfaces.IPokerStats;
import mallorcatour.interfaces.IDistanceCalculator;

/**
 *
 * @author Andrew
 */
public class PokerStatsDistance implements IDistanceCalculator<IPokerStats> {

    public double getDistance(IPokerStats one, IPokerStats other) {
        double sum = 0;
        sum += Math.pow(one.getAggressionFrequency() - other.getAggressionFrequency(), 2);
        sum += Math.pow(one.getFoldFrequency() - other.getFoldFrequency(), 2);
        return Math.sqrt(sum);
    }
}
