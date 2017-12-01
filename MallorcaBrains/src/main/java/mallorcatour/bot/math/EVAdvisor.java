package mallorcatour.bot.math;

import mallorcatour.core.game.advice.Advisor;
import mallorcatour.bot.interfaces.ISpectrumHolder;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.GameContext;
import mallorcatour.core.game.state.HandState;
import mallorcatour.tools.Log;

public class EVAdvisor implements Advisor {

	private ISpectrumHolder villainSpectrumHolder;
	private BaseAdviceCreatorFromMap adviceCreator;
	private IProfitCalculator profitCalculator;
	private String DEBUG_PATH;

	public EVAdvisor(Advisor villainModel, StrengthManager strengthManager, ISpectrumHolder villainSpectrumHolder,
					 String debug) {
		this.villainSpectrumHolder = villainSpectrumHolder;
		profitCalculator = new NLProfitCalculator(villainModel, strengthManager);
		adviceCreator = new LessVarianceActionFromMap();
		this.DEBUG_PATH = debug;
	}

	@Override
	public IAdvice getAdvice(HandState situation, HoleCards cards, GameContext gameInfo) {
		ActionDistribution actionProfitMap = profitCalculator.getProfitMap(gameInfo, situation, cards.first, cards.second,
				villainSpectrumHolder.getSpectrum());
		Log.f(DEBUG_PATH, actionProfitMap.toSmartString(gameInfo.getBigBlindSize(), gameInfo.getBankRollAtRisk()));
		IAdvice advice = adviceCreator.create(actionProfitMap, gameInfo, cards);
		return advice;
	}

}