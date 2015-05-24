package mallorcatour.bot.math;

import java.util.Map;
import java.util.Map.Entry;

import mallorcatour.brains.IActionChecker;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.core.spectrum.Spectrum;

public class NLRiverActionChecker implements IActionChecker {

	private final IProfitCalculator profitCalculator;

	private final static double MIN_VALUE_FOR_CALL_DECISION = 10;
    private final static double MIN_VALUE_FOR_BET_DECISION = 10;

	public NLRiverActionChecker(IProfitCalculator profitCalculator) {
		this.profitCalculator = profitCalculator;
	}

	@Override
	public Action checkAction(Action action, LocalSituation situation, IGameInfo gameInfo, HoleCards cards,
			Spectrum villainSpectrum) {
		if (!gameInfo.isRiver() || gameInfo.getLimitType() != LimitType.NO_LIMIT) {
			return action;
		}
		Map<Action, Double> map = profitCalculator.getProfitMap(gameInfo, situation, cards.first,
				cards.second, villainSpectrum);
		if (gameInfo.getHeroAmountToCall() > 0) {
			if (action.isFold()) {
				for (Entry<Action, Double> entry : map.entrySet()) {
					if (entry.getKey().isPassive() && entry.getValue() >= MIN_VALUE_FOR_CALL_DECISION) {
						return Action.callAction(gameInfo.getHeroAmountToCall());
					}
				}
			} else if (action.isPassive()) {
				for (Entry<Action, Double> entry : map.entrySet()) {
					if (entry.getKey().isPassive() && entry.getValue() < MIN_VALUE_FOR_CALL_DECISION) {
						return Action.foldAction();
					}
				}
			}
		} else if (action.isAggressive()) {
			for (Entry<Action, Double> entry : map.entrySet()) {
				if (entry.getKey().isAggressive() && entry.getValue() < MIN_VALUE_FOR_BET_DECISION) {
					return Action.checkAction();
				}
			}
		}
		return action;
	}

}
