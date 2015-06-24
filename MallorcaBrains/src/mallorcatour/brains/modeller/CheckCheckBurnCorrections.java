package mallorcatour.brains.modeller;

public class CheckCheckBurnCorrections implements ProfitCorrections {

	@Override
	public double getPreflopCall() {
		return 0.4;
	}

	@Override
	public double getPreflopRaise() {
		return -0.15;
	}

	@Override
	public double getPreflopCheck() {
		return 0.3;
	}

	@Override
	public double getPreflopBet() {
		return -0.1;
	}

	@Override
	public double getPostflopCall() {
		return -0.17;
	}

	@Override
	public double getPostflopRaise() {
		return -0.2;
	}

	@Override
	public double getPostflopCheck() {
		return -0.3;
	}

	@Override
	public double getPostflopBet() {
		return 0;
	}

}
