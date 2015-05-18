package mallorcatour.bot.modeller;

import java.util.Map;

import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.interfaces.IVillainModeller;
import mallorcatour.bot.math.NLGameSolver;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Flop;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.LimitType;
import mallorcatour.util.CollectionUtils;
import mallorcatour.util.Log;

public class NLSpectrumSituationHandler extends BaseSpectrumSituationHandler {

	public NLSpectrumSituationHandler(IVillainModeller villainModeller, boolean modelPreflop,
			boolean modelPostflop, ISpectrumListener villainSpectrumListener,
			IDecisionListener villainDecisionListener, String debug) {
		super(villainModeller, LimitType.NO_LIMIT, modelPreflop, modelPostflop, villainSpectrumListener,
				villainDecisionListener, debug);
		gameSolver = new NLGameSolver(villainModeller);
	}

	private final NLGameSolver gameSolver;

	public Map<Action, Double> getProfitMap() {
		if (gameInfo.isPreFlop()) {
			if (gameInfo.getButtonName().equals(heroName)
					&& gameInfo.getHeroAmountToCall() == gameInfo.getBigBlindSize() / 2) {
				return gameSolver.onFirstActionPreFlop(heroActionCount, countOfHeroAggressive, villainActionCount,
						countOfOppAggressive, gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(), villainSpectrum,
						new HoleCards(holeCard1, holeCard2), preflopStrengthMap,/*
																				 * was
																				 * villain
																				 * aggressive
																				 */false, true,
						gameInfo.getBigBlindSize());
			} else {
				return gameSolver.onSecondActionPreflop(heroActionCount, countOfHeroAggressive, villainActionCount,
						countOfOppAggressive, gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(),
						gameInfo.getHeroAmountToCall(), villainSpectrum, new HoleCards(holeCard1, holeCard2),
						preflopStrengthMap, gameInfo.getButtonName().equals(heroName), gameInfo.getBigBlindSize());
			}
		} else if (gameInfo.isFlop()) {
			if (gameInfo.getHeroAmountToCall() == 0 && !gameInfo.getButtonName().equals(heroName)) {
				Map<Action, Double> map = gameSolver.onFirstActionFlop(heroActionCount, countOfHeroAggressive,
						villainActionCount, countOfOppAggressive, gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(),
						villainSpectrum, new Flop(flop1, flop2, flop3), new HoleCards(holeCard1, holeCard2),
						flopStrengthMap, wasVillainPreviousAggressive, gameInfo.getButtonName().equals(heroName),
						gameInfo.getBigBlindSize());
				double profit = CollectionUtils.maxValue(map);
				Log.d("Hero's first action on flop " + map.toString());
				Log.d("\nHero profit: " + profit);
				return map;
			} else {
				Map<Action, Double> map = gameSolver.onSecondActionFlop(heroActionCount, countOfHeroAggressive,
						villainActionCount, countOfOppAggressive, gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(),
						gameInfo.getHeroAmountToCall(), villainSpectrum, new Flop(flop1, flop2, flop3), new HoleCards(
								holeCard1, holeCard2), flopStrengthMap, gameInfo.getButtonName().equals(heroName),
						gameInfo.getBigBlindSize());
				double profit = CollectionUtils.maxValue(map);
				Log.d("Flop. Second action. " + map.toString());
				Log.d("\nHero profit: " + profit);
				return map;
			}
		} else if (gameInfo.isTurn()) {
			if (gameInfo.getHeroAmountToCall() == 0 && !gameInfo.getButtonName().equals(heroName)) {
				Map<Action, Double> map = gameSolver.onFirstActionTurn(heroActionCount, countOfHeroAggressive,
						villainActionCount, countOfOppAggressive, gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(),
						villainSpectrum, new Flop(flop1, flop2, flop3), turn, new HoleCards(holeCard1, holeCard2),
						turnStrengthMap, wasVillainPreviousAggressive, gameInfo.getButtonName().equals(heroName),
						gameInfo.getBigBlindSize());
				double profit = CollectionUtils.maxValue(map);
				Log.d("Hero's first action on turn " + map.toString());
				Log.d("\nHero profit: " + profit);
				return map;
			} else {
				Map<Action, Double> map = gameSolver.onSecondActionTurn(heroActionCount, countOfHeroAggressive,
						villainActionCount, countOfOppAggressive, gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(),
						gameInfo.getHeroAmountToCall(), villainSpectrum, new Flop(flop1, flop2, flop3), turn,
						new HoleCards(holeCard1, holeCard2), turnStrengthMap,
						gameInfo.getButtonName().equals(heroName), gameInfo.getBigBlindSize());
				double profit = CollectionUtils.maxValue(map);
				Log.d("Turn. Second action. " + map.toString());
				Log.d("\nHero profit: " + profit);
				return map;
			}
		} else if (gameInfo.isRiver()) {
			if (gameInfo.getHeroAmountToCall() == 0 && !gameInfo.getButtonName().equals(heroName)) {
				Map<Action, Double> map = gameSolver.onFirstActionRiver(heroActionCount, countOfHeroAggressive,
						villainActionCount, countOfOppAggressive, gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(),
						villainSpectrum, new Flop(flop1, flop2, flop3), turn, river,
						new HoleCards(holeCard1, holeCard2), riverStrengthMap, wasVillainPreviousAggressive, gameInfo
								.getButtonName().equals(heroName), gameInfo.getBigBlindSize());
				double profit = CollectionUtils.maxValue(map);
				Log.f(DEBUG_PATH, "Hero's first action on river: " + map.toString());
				Log.d("\nHero profit: " + profit);
				return map;
			} else {
				Map<Action, Double> map = gameSolver.onSecondActionRiver(heroActionCount, countOfHeroAggressive,
						villainActionCount, countOfOppAggressive, gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(),
						gameInfo.getHeroAmountToCall(), villainSpectrum, new Flop(flop1, flop2, flop3), turn, river,
						new HoleCards(holeCard1, holeCard2), riverStrengthMap, gameInfo.getButtonName()
								.equals(heroName), gameInfo.getBigBlindSize());
				double profit = CollectionUtils.maxValue(map);
				Log.d("River. Second action: " + map.toString());
				Log.f(DEBUG_PATH, "River. Second action: " + map.toString());
				Log.d("\nHero profit: " + profit);
				return map;
			}
		} else {
			throw new RuntimeException();
		}
	}

}