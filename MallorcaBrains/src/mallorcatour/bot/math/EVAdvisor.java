package mallorcatour.bot.math;

import mallorcatour.bot.interfaces.ISpectrumHolder;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.math.StrengthManager;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.game.interfaces.IPokerStats;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.tools.Log;

public class EVAdvisor implements IAdvisor {

	private ISpectrumHolder villainSpectrumHolder;
	private BaseAdviceCreatorFromMap adviceCreator;
	private IProfitCalculator profitCalculator;
	private String DEBUG_PATH;

	public EVAdvisor(IAdvisor villainModel, StrengthManager strengthManager, ISpectrumHolder villainSpectrumHolder,
			String debug) {
		this.villainSpectrumHolder = villainSpectrumHolder;
		profitCalculator = new NLProfitCalculator(villainModel, strengthManager);
		adviceCreator = new LessVarianceActionFromMap();
//		adviceCreator = new ActionDistributionFromMap();
//		adviceCreator = new MostProfitActionFromMap();
		this.DEBUG_PATH = debug;
	}

	@Override
	public IAdvice getAdvice(LocalSituation situation, HoleCards cards, IPlayerGameInfo gameInfo) {
		ActionDistribution actionProfitMap = profitCalculator.getProfitMap(gameInfo, situation, cards.first, cards.second,
				villainSpectrumHolder.getSpectrum());
		Log.f(DEBUG_PATH, actionProfitMap.toString());
		IAdvice advice = adviceCreator.create(actionProfitMap, gameInfo);
		return advice;
	}

	@Override
	public String getName() {
		return "EV advisor";
	}

	@Override
	public IPokerStats getStats() {
		throw new UnsupportedOperationException();
	}

}