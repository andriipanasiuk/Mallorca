package mallorcatour.brains.modeller.creator;

import mallorcatour.bot.math.ProfitCorrections;

public class DafishCorrections implements ProfitCorrections {

	@Override
	public double getPreflopCall() {
		return 0.1;
	}

	@Override
	public double getPreflopRaise() {
		return 0.34;
	}

	@Override
	public double getPreflopCheck() {
		return 0.1;
	}

	@Override
	public double getPreflopBet() {
		return 0.43;
	}

	@Override
	public double getPostflopCall() {
		return -0.11;
	}

	@Override
	public double getPostflopRaise() {
		return 0.09;
	}

	@Override
	public double getPostflopCheck() {
		return 0.2;
	}

	@Override
	public double getPostflopBet() {
		return 0.07;
	}

}
