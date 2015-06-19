package mallorcatour.bot.math;

import java.util.Map.Entry;

import mallorcatour.bot.interfaces.ISpectrumHolder;
import mallorcatour.brains.IActionChecker;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.game.situation.LocalSituation;

public class NLRiverActionChecker implements IActionChecker {

	private final IProfitCalculator profitCalculator;
	private final ISpectrumHolder villainSpectrumHolder;

	private final static double MIN_VALUE_FOR_CALL_DECISION = 10;
    private final static double MIN_VALUE_FOR_BET_DECISION = 10;

	public NLRiverActionChecker(IProfitCalculator profitCalculator, ISpectrumHolder villain) {
		this.profitCalculator = profitCalculator;
		this.villainSpectrumHolder = villain;
	}

	@Override
	public Action checkAction(Action action, LocalSituation situation, IPlayerGameInfo gameInfo, HoleCards cards) {
		if (!gameInfo.isRiver() || gameInfo.getLimitType() != LimitType.NO_LIMIT) {
			return action;
		}
		ActionDistribution map = profitCalculator.getProfitMap(gameInfo, situation, cards.first,
				cards.second, villainSpectrumHolder.getSpectrum());
		if (gameInfo.getAmountToCall() > 0) {
			if (action.isFold()) {
				for (Entry<Action, RandomVariable> entry : map.entrySet()) {
					if (entry.getKey().isPassive() && entry.getValue().getEV() >= MIN_VALUE_FOR_CALL_DECISION) {
						return Action.callAction(gameInfo.getAmountToCall());
					}
				}
			} else if (action.isPassive()) {
				for (Entry<Action, RandomVariable> entry : map.entrySet()) {
					if (entry.getKey().isPassive() && entry.getValue().getEV() < MIN_VALUE_FOR_CALL_DECISION) {
						return Action.fold();
					}
				}
			}
		} else if (action.isAggressive()) {
			for (Entry<Action, RandomVariable> entry : map.entrySet()) {
				if (entry.getKey().isAggressive() && entry.getValue().getEV() < MIN_VALUE_FOR_BET_DECISION) {
					return Action.checkAction();
				}
			}
		}
		return action;
	}

}
