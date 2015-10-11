package mallorcatour.brains.modeller.creator;

import mallorcatour.bot.math.ProfitCorrections;

public class SkelbotCorrections implements ProfitCorrections {

	public double postflopCall = 0.14;
	public double postflopRaise = 0.18;

	@Override
	public double getPreflopCall() {
		return -0.03;
	}

	@Override
	public double getPreflopRaise() {
		return 0.4;
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
		return postflopCall;
	}

	@Override
	public double getPostflopRaise() {
		return postflopRaise;
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
