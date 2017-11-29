package mallorcatour.brains.modeller.creator;

import mallorcatour.bot.math.ProfitCorrections;

public class Dafish2Corrections implements ProfitCorrections {

	@Override
	public double getPreflopCall() {
		return 0.05;
	}

	@Override
	public double getPreflopRaise() {
		return 0.26;
	}

	@Override
	public double getPreflopCheck() {
		return 0.1;
	}

	@Override
	public double getPreflopBet() {
		return getPreflopRaise();
	}

	@Override
	public double getPostflopCall() {
		return 0.08;
	}

	@Override
	public double getPostflopRaise() {
		return 0.08;
	}

	@Override
	public double getPostflopCheck() {
		return 0.2;
	}

	@Override
	public double getPostflopBet() {
		return getPostflopRaise();
	}

}
