package mallorcatour.brains.stats;

import java.io.File;
import java.util.List;

import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.hhparser.SituationIO;
import mallorcatour.neural.core.PokerLearningExample;
import mallorcatour.tools.Log;

public class StatCalculator {

	public static class PreflopStats {
		double vpip, pfr, pfr2;
	}

	public static class PostflopStats {
		double aggrFreq, foldFreq;
	}

	public static PostflopStats getPostflop(List<PokerLearningExample> list) {
		double aggressive = 0, aggressiveCount = 0;
		double fold = 0, foldCount = 0;
		for (PokerLearningExample item : list) {
			LocalSituation situation = item.getSituation();
			Advice advice = item.getAdvice();

			if (situation.getStreet() == PokerStreet.PREFLOP_VALUE) {
				continue;
			}
			if (situation.getPotOdds() != 0) {
				foldCount += 1;
				fold += advice.getFold();
			}
			aggressive += advice.getAggressive();
			aggressiveCount += (1 - advice.getFold());
		}
		PostflopStats result = new PostflopStats();
		result.foldFreq = fold / foldCount;
		result.aggrFreq = aggressive / aggressiveCount;
		return result;
	}

	public static PreflopStats getPreflop(List<PokerLearningExample> list) {
		double vpip = 0, pfr = 0, vpipCount = 0, pfrCount = 0;
		for (PokerLearningExample item : list) {
			LocalSituation situation = item.getSituation();
			Advice advice = item.getAdvice();

			if (situation.getStreet() != PokerStreet.PREFLOP_VALUE) {
				continue;
			}
			pfrCount += 1;
			pfr += advice.getAggressive();
			if (situation.getHeroActionCount() == 0) {
				vpipCount += 1;
				if (situation.getVillainActionCount() == 0) {
					vpip += (1 - advice.getFold());
				} else if (situation.getVillainActionCount() == 1) {
					if (situation.getVillainAggresionActionCount() == 1) {
						vpip += (1 - advice.getFold());
					} else if (situation.getVillainAggresionActionCount() == 0) {
						vpip += advice.getAggressive();
					}
				}
			}
		}
		PreflopStats result = new PreflopStats();
		result.vpip = vpip / vpipCount;
		result.pfr = pfr / pfrCount;
		return result;
	}

	public static void main(String... args) {
		{
			String path = "Cuba";
			List<PokerLearningExample> list = SituationIO.readFromDir(new File(path));
			PreflopStats preflopStats = getPreflop(list);
			PostflopStats postflopStats = getPostflop(list);
			Log.d("Vpip: " + preflopStats.vpip + " Pfr: " + preflopStats.pfr);
			Log.d("Aggr.: " + postflopStats.aggrFreq + " Fold: " + postflopStats.foldFreq);
		}
		{
			String path = "learning_examples/GusXensen";
			List<PokerLearningExample> list = SituationIO.readFromDir(new File(path));
			PreflopStats preflopStats = getPreflop(list);
			PostflopStats postflopStats = getPostflop(list);
			Log.d("Vpip: " + preflopStats.vpip + " Pfr: " + preflopStats.pfr);
			Log.d("Aggr.: " + postflopStats.aggrFreq + " Fold: " + postflopStats.foldFreq);
		}
	}
}
