package mallorcatour.core.stats;


/**
 * Immutable реализация статистики игрока.
 */
public class PokerStatsImpl implements PokerStats {

	private final double vpip, pfr, aggrFreq, foldFreq;

	public PokerStatsImpl(double vpip, double pfr, double aggrFreq, double foldFreq) {
		super();
		this.vpip = vpip;
		this.pfr = pfr;
		this.aggrFreq = aggrFreq;
		this.foldFreq = foldFreq;
	}

	@Override
	public double getVpip() {
		return vpip;
	}

	@Override
	public double getPfr() {
		return pfr;
	}

	@Override
	public double getAggressionFrequency() {
		return aggrFreq;
	}

	@Override
	public double getFoldFrequency() {
		return foldFreq;
	}

}
