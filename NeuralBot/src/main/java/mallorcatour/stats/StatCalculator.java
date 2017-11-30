package mallorcatour.stats;

import java.util.List;

import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.AdviceHolder;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.state.HandState;
import mallorcatour.core.game.state.HandStateHolder;

public class StatCalculator {

	public static <T extends HandStateHolder & AdviceHolder> PokerStats calculate(List<T> list) {
		PokerStatsBuffer info = new PokerStatsBuffer();
		for (T item : list) {
			HandState situation = item.getSituation();
			IAdvice advice = item.getAdvice();
			changeStat(situation, advice, info);
		}
		return info;
	}

	public static void changeStat(HandState situation, IAdvice advice, PokerStatsBuffer info){
		if (situation.getStreet() != PokerStreet.PREFLOP_VALUE) {
			if (situation.getPotOdds() != 0) {
				info.foldFrequency.first += advice.getFold();
				info.foldFrequency.second += 1;
			}
			info.aggressionFrequency.first += advice.getAggressive();
			info.aggressionFrequency.second += (1 - advice.getFold());
		} else {
			if (situation.getHeroActionCount() == 0) {
				info.pfr.second += 1;
				info.pfr.first += advice.getAggressive();
				info.vpip.second += 1;
				if (situation.getVillainActionCount() == 0) {
					info.vpip.first += (1 - advice.getFold());
				} else if (situation.getVillainActionCount() == 1) {
					if (situation.getVillainAggresionActionCount() == 1) {
						info.vpip.first += (1 - advice.getFold());
					} else if (situation.getVillainAggresionActionCount() == 0) {
						info.vpip.first += advice.getAggressive();
					}
				}
			}
		}
	
	}
}
