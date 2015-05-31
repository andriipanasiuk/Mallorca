package mallorcatour.bot.math;

import java.util.Map;

import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.math.StrengthManager;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.interfaces.IAggressionInfo;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.spectrum.Spectrum;
import mallorcatour.util.CollectionUtils;
import mallorcatour.util.Log;

public class NLProfitCalculator implements IProfitCalculator {

	public NLProfitCalculator(IAdvisor villainModeller, StrengthManager strengthManager) {
		gameSolver = new NLGameSolver(villainModeller);
		this.strengthManager  = strengthManager;;
	}

	private final NLGameSolver gameSolver;
	private final StrengthManager strengthManager;

	@Override
	public Map<Action, Double> getProfitMap(IPlayerGameInfo gameInfo, IAggressionInfo situation,
			Card holeCard1, Card holeCard2, Spectrum villainSpectrum) {
		int heroActionCount = situation.getHeroActionCount();
		int countOfHeroAggressive = situation.getHeroAggresionActionCount();
		int villainActionCount = situation.getVillainActionCount();
		int countOfOppAggressive = situation.getVillainAggresionActionCount();
		boolean wasVillainPreviousAggressive = situation.wasVillainPreviousAggresive();
		if (gameInfo.isPreFlop()) {
			if (gameInfo.onButton() && gameInfo.getAmountToCall() == gameInfo.getBigBlindSize() / 2) {
				return gameSolver.onFirstActionPreFlop(heroActionCount, countOfHeroAggressive, villainActionCount,
						countOfOppAggressive, gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(), villainSpectrum,
						new HoleCards(holeCard1, holeCard2), strengthManager.preflop,/*
																				 * was
																				 * villain
																				 * aggressive
																				 */false, true,
						gameInfo.getBigBlindSize());
			} else {
				return gameSolver.onSecondActionPreflop(heroActionCount, countOfHeroAggressive, villainActionCount,
						countOfOppAggressive, gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(),
						gameInfo.getAmountToCall(), villainSpectrum, new HoleCards(holeCard1, holeCard2),
						strengthManager.preflop, gameInfo.onButton(), gameInfo.getBigBlindSize());
			}
		} else if (gameInfo.isFlop()) {
			if (gameInfo.getAmountToCall() == 0 && !gameInfo.onButton()) {
				Map<Action, Double> map = gameSolver.onFirstActionFlop(heroActionCount, countOfHeroAggressive,
						villainActionCount, countOfOppAggressive, gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(),
						villainSpectrum, gameInfo.getFlop(), new HoleCards(holeCard1, holeCard2),
						strengthManager.flop, wasVillainPreviousAggressive, gameInfo.onButton(),
						gameInfo.getBigBlindSize());
				double profit = CollectionUtils.maxValue(map);
				Log.f("Hero's first action on flop " + map.toString());
				Log.f("\nHero profit: " + profit);
				return map;
			} else {
				Map<Action, Double> map = gameSolver.onSecondActionFlop(heroActionCount, countOfHeroAggressive,
						villainActionCount, countOfOppAggressive, gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(),
						gameInfo.getAmountToCall(), villainSpectrum, gameInfo.getFlop(), new HoleCards(
								holeCard1, holeCard2), strengthManager.flop, gameInfo.onButton(),
						gameInfo.getBigBlindSize());
				double profit = CollectionUtils.maxValue(map);
				Log.f("Flop. Second action. " + map.toString());
				Log.f("\nHero profit: " + profit);
				return map;
			}
		} else if (gameInfo.isTurn()) {
			if (gameInfo.getAmountToCall() == 0 && !gameInfo.onButton()) {
				Map<Action, Double> map = gameSolver.onFirstActionTurn(heroActionCount, countOfHeroAggressive,
						villainActionCount, countOfOppAggressive, gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(),
						villainSpectrum, gameInfo.getFlop(), gameInfo.getTurn(), new HoleCards(holeCard1, holeCard2),
						strengthManager.turn, wasVillainPreviousAggressive, gameInfo.onButton(),
						gameInfo.getBigBlindSize());
				double profit = CollectionUtils.maxValue(map);
				Log.f("Hero's first action on turn " + map.toString());
				Log.f("\nHero profit: " + profit);
				return map;
			} else {
				Map<Action, Double> map = gameSolver.onSecondActionTurn(heroActionCount, countOfHeroAggressive,
						villainActionCount, countOfOppAggressive, gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(),
						gameInfo.getAmountToCall(), villainSpectrum, gameInfo.getFlop(), gameInfo.getTurn(),
						new HoleCards(holeCard1, holeCard2), strengthManager.turn,
						gameInfo.onButton(), gameInfo.getBigBlindSize());
				double profit = CollectionUtils.maxValue(map);
				Log.f("Turn. Second action. " + map.toString());
				Log.f("\nHero profit: " + profit);
				return map;
			}
		} else if (gameInfo.isRiver()) {
			if (gameInfo.getAmountToCall() == 0 && !gameInfo.onButton()) {
				Map<Action, Double> map = gameSolver.onFirstActionRiver(heroActionCount, countOfHeroAggressive,
						villainActionCount, countOfOppAggressive, gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(),
						villainSpectrum, gameInfo.getFlop(), gameInfo.getTurn(), gameInfo.getRiver(),
						new HoleCards(
								holeCard1, holeCard2), strengthManager.river, wasVillainPreviousAggressive, gameInfo
								.onButton(), gameInfo.getBigBlindSize());
				double profit = CollectionUtils.maxValue(map);
				Log.f("Hero's first action on river: " + map.toString());
				Log.f("\nHero profit: " + profit);
				return map;
			} else {
				Map<Action, Double> map = gameSolver.onSecondActionRiver(heroActionCount, countOfHeroAggressive,
						villainActionCount, countOfOppAggressive, gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(),
						gameInfo.getAmountToCall(), villainSpectrum, gameInfo.getFlop(), gameInfo.getTurn(),
						gameInfo.getRiver(), new HoleCards(holeCard1, holeCard2), strengthManager.river,
						gameInfo.onButton(), gameInfo.getBigBlindSize());
				double profit = CollectionUtils.maxValue(map);
				Log.f("River. Second action: " + map.toString());
				Log.f("\nHero profit: " + profit);
				return map;
			}
		} else {
			throw new RuntimeException();
		}
	}

}