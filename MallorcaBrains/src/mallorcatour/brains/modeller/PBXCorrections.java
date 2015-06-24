package mallorcatour.brains.modeller;

public class PBXCorrections implements ProfitCorrections {

	@Override
	public double getPreflopCall() {
		return 0;
	}

	@Override
	public double getPreflopRaise() {
		return 1.25;
	}

	@Override
	public double getPreflopCheck() {
		return 0;
	}

	@Override
	public double getPreflopBet() {
		return 1.25;
	}

	@Override
	public double getPostflopCall() {
		return -1;
	}

	@Override
	public double getPostflopRaise() {
		return -0.9;
	}

	@Override
	public double getPostflopCheck() {
		return -0.8;
	}

	@Override
	public double getPostflopBet() {
		return 1.6;
	}

}
