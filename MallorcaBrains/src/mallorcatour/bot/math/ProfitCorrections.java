package mallorcatour.bot.math;

public interface ProfitCorrections {
	double getPreflopCall();

	double getPreflopRaise();

	double getPreflopCheck();

	double getPreflopBet();

	double getPostflopCall();

	double getPostflopRaise();

	double getPostflopCheck();

	double getPostflopBet();
}