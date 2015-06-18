package mallorcatour.brains.stats;

import java.util.List;

import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IPokerStats;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.neural.core.PokerLearningExample;

public class StatCalculator {

	public static IPokerStats calculate(List<PokerLearningExample> list) {
		PokerStatInfo info = new PokerStatInfo();
		for (PokerLearningExample item : list) {
			LocalSituation situation = item.getSituation();
			Advice advice = item.getAdvice();
			StatCalculator.changeStat(situation, advice, info);
		}
		return info;
	}

	public static void changeStat(LocalSituation situation, IAdvice advice, PokerStatInfo info){
		if (situation.getStreet() != PokerStreet.PREFLOP_VALUE) {
			if (situation.getPotOdds() != 0) {
				info.foldFrequency.first += advice.getFold();
				info.foldFrequency.second += 1;
			}
			info.aggressionFrequency.first += advice.getAggressive();
			info.aggressionFrequency.second += (1 - advice.getFold());
		} else {
			info.pfr.second += 1;
			info.pfr.first += advice.getAggressive();
			if (situation.getHeroActionCount() == 0) {
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
