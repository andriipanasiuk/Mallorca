package mallorcatour.brains.stats;


public class PokerStats implements IPokerStats {

	private double vpip, pfr, aggrFreq, foldFreq;

	public PokerStats(double vpip, double pfr, double aggrFreq, double foldFreq) {
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
