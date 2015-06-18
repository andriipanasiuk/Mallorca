package mallorcatour.brains.stats;

import mallorcatour.bot.AdvisorListener;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IPokerStats;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.tools.DoubleUtils;
import mallorcatour.tools.Pair;

public class PokerStatInfo implements IPokerStats, AdvisorListener {
	public Pair<Double, Double> vpip = new Pair<>(0d, 0d);
	public Pair<Double, Double> pfr = new Pair<>(0d, 0d);
	public Pair<Double, Double> aggressionFrequency = new Pair<>(0d, 0d);
	public Pair<Double, Double> foldFrequency = new Pair<>(0d, 0d);

	public void addAggressionFrequencyInfo(Pair<Integer, Integer> aggressionFreq) {
		this.aggressionFrequency.first += aggressionFreq.first;
		this.aggressionFrequency.second += aggressionFreq.second;
	}

	public void addVpipInfo(Pair<Integer, Integer> vpip) {
		this.vpip.first += vpip.first;
		this.vpip.second += vpip.second;
	}

	public void addFoldInfo(Pair<Integer, Integer> fold) {
		this.foldFrequency.first += fold.first;
		this.foldFrequency.second += fold.second;
	}

	@Override
	public double getFoldFrequency() {
		return foldFrequency.first / foldFrequency.second;
	}

	@Override
	public double getAggressionFrequency() {
		return aggressionFrequency.first / aggressionFrequency.second;
	}

	@Override
	public double getVpip() {
		return vpip.first / vpip.second;
	}

	@Override
	public double getPfr() {
		return pfr.first / pfr.second;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("Vpip: ").append(DoubleUtils.digitsAfterComma(getVpip(), 2));
		result.append(" Pfr: ").append(DoubleUtils.digitsAfterComma(getPfr(), 2));
		result.append(" Aggr.: ").append(DoubleUtils.digitsAfterComma(getAggressionFrequency(), 2));
		result.append(" Fold: ").append(DoubleUtils.digitsAfterComma(getFoldFrequency(), 2));
		return result.toString();
	}

	@Override
	public void onAdvice(LocalSituation situation, IAdvice advice) {
		StatCalculator.changeStat(situation, advice, this);
	}
}