package mallorcatour.bot.math;

import java.util.Map;

import mallorcatour.bot.interfaces.ISpectrumHolder;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.math.StrengthManager;
import mallorcatour.core.game.Action;
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
		adviceCreator = new AdviceCreatorFromMap();
		this.DEBUG_PATH = debug;
	}

	@Override
	public IAdvice getAdvice(LocalSituation situation, HoleCards cards, IPlayerGameInfo gameInfo) {
		Map<Action, Double> map = profitCalculator.getProfitMap(gameInfo, situation, cards.first, cards.second,
				villainSpectrumHolder.getSpectrum());
		Log.f(DEBUG_PATH, "Map<Action, Profit>: " + map.toString());
		IAdvice advice = adviceCreator.create(map);
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