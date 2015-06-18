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
		if (one.getAggressionFrequency() != Double.NaN && other.getAggressionFrequency() != Double.NaN) {
			sum += Math.pow(one.getAggressionFrequency() - other.getAggressionFrequency(), 2);
		}
		if (one.getFoldFrequency() != Double.NaN && other.getFoldFrequency() != Double.NaN) {
			sum += Math.pow(one.getFoldFrequency() - other.getFoldFrequency(), 2);
		}
		if (one.getVpip() != Double.NaN && other.getVpip() != Double.NaN) {
			sum += Math.pow(one.getVpip() - other.getVpip(), 2);
		}
		if (one.getPfr() != Double.NaN && other.getPfr() != Double.NaN) {
			sum += Math.pow(one.getPfr() - other.getPfr(), 2);
		}
		return Math.sqrt(sum);
	}
}
