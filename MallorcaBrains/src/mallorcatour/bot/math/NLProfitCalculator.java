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
import mallorcatour.tools.CollectionUtils;
import mallorcatour.tools.FileUtils;
import mallorcatour.tools.Log;

public class NLProfitCalculator implements IProfitCalculator {

	public NLProfitCalculator(IAdvisor villainModeller, StrengthManager strengthManager) {
		gameSolver = new NLGameSolver(villainModeller);
		this.strengthManager = strengthManager;
		;
	}

	private final NLGameSolver gameSolver;
	private final StrengthManager strengthManager;

	@Override
	public Map<Action, Double> getProfitMap(IPlayerGameInfo gameInfo, IAggressionInfo situation, Card holeCard1,
			Card holeCard2, Spectrum villainSpectrum) {
		int heroActionCount = situation.getHeroActionCount();
		boolean wasVillainPreviousAggressive = situation.wasVillainPreviousAggresive();
		if (gameInfo.isPreFlop()) {
			if (gameInfo.onButton() && heroActionCount == 0) {
				return gameSolver.onFirstActionPreFlop(gameInfo, situation, gameInfo.getBankRollAtRisk(),
						gameInfo.getPotSize(), villainSpectrum, new HoleCards(holeCard1, holeCard2),
						strengthManager.preflop,/*
												 * was villain aggressive
												 */false, true, gameInfo.getBigBlindSize());
			} else {
				return gameSolver.onSecondActionPreflop(gameInfo, situation, gameInfo.getBankRollAtRisk(), gameInfo
						.getPotSize(), gameInfo.getAmountToCall(), villainSpectrum,
						new HoleCards(holeCard1, holeCard2), strengthManager.preflop, gameInfo.onButton(), gameInfo
								.getBigBlindSize());
			}
		} else if (gameInfo.isFlop()) {
			if (gameInfo.getAmountToCall() == 0 && !gameInfo.onButton()) {
				Map<Action, Double> map = gameSolver.onFirstActionFlop(gameInfo, situation,
						gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(), villainSpectrum, gameInfo.getFlop(),
						new HoleCards(holeCard1, holeCard2), strengthManager.flop, wasVillainPreviousAggressive,
						gameInfo.onButton(), gameInfo.getBigBlindSize());
				double profit = CollectionUtils.maxValue(map);
				Log.f("Hero's first action on flop " + map.toString());
				Log.f(FileUtils.LINE_SEPARATOR + "Hero profit: " + profit);
				return map;
			} else {
				Map<Action, Double> map = gameSolver.onSecondActionFlop(gameInfo, situation,
						gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(), gameInfo.getAmountToCall(),
						villainSpectrum, gameInfo.getFlop(), new HoleCards(holeCard1, holeCard2), strengthManager.flop,
						gameInfo.onButton(), gameInfo.getBigBlindSize());
				double profit = CollectionUtils.maxValue(map);
				Log.f("Flop. Second action. " + map.toString());
				Log.f(FileUtils.LINE_SEPARATOR + "Hero profit: " + profit);
				return map;
			}
		} else if (gameInfo.isTurn()) {
			if (gameInfo.getAmountToCall() == 0 && !gameInfo.onButton()) {
				Map<Action, Double> map = gameSolver.onFirstActionTurn(gameInfo, situation,
						gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(), villainSpectrum, gameInfo.getFlop(),
						gameInfo.getTurn(), new HoleCards(holeCard1, holeCard2), strengthManager.turn,
						wasVillainPreviousAggressive, gameInfo.onButton(), gameInfo.getBigBlindSize());
				double profit = CollectionUtils.maxValue(map);
				Log.f("Hero's first action on turn " + map.toString());
				Log.f(FileUtils.LINE_SEPARATOR + "Hero profit: " + profit);
				return map;
			} else {
				Map<Action, Double> map = gameSolver.onSecondActionTurn(gameInfo, situation,
						gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(), gameInfo.getAmountToCall(),
						villainSpectrum, gameInfo.getFlop(), gameInfo.getTurn(), new HoleCards(holeCard1, holeCard2),
						strengthManager.turn, gameInfo.onButton(), gameInfo.getBigBlindSize());
				double profit = CollectionUtils.maxValue(map);
				Log.f("Turn. Second action. " + map.toString());
				Log.f(FileUtils.LINE_SEPARATOR + "Hero profit: " + profit);
				return map;
			}
		} else if (gameInfo.isRiver()) {
			if (gameInfo.getAmountToCall() == 0 && !gameInfo.onButton()) {
				Map<Action, Double> map = gameSolver.onFirstActionRiver(gameInfo, situation,
						gameInfo.getBankRollAtRisk(), gameInfo.getPotSize(), villainSpectrum, gameInfo.getFlop(),
						gameInfo.getTurn(), gameInfo.getRiver(), new HoleCards(holeCard1, holeCard2),
						strengthManager.river, wasVillainPreviousAggressive, gameInfo.onButton(),
						gameInfo.getBigBlindSize());
				double profit = CollectionUtils.maxValue(map);
				Log.f("Hero's first action on river: " + map.toString());
				Log.f(FileUtils.LINE_SEPARATOR + "Hero profit: " + profit);
				return map;
			} else {
				Map<Action, Double> map = gameSolver.onSecondActionRiver(gameInfo, situation, gameInfo
						.getBankRollAtRisk(), gameInfo.getPotSize(), gameInfo.getAmountToCall(), villainSpectrum,
						gameInfo.getFlop(), gameInfo.getTurn(), gameInfo.getRiver(),
						new HoleCards(holeCard1, holeCard2), strengthManager.river, gameInfo.onButton(), gameInfo
								.getBigBlindSize());
				double profit = CollectionUtils.maxValue(map);
				Log.f("River. Second action: " + map.toString());
				Log.f(FileUtils.LINE_SEPARATOR + "Hero profit: " + profit);
				return map;
			}
		} else {
			throw new RuntimeException();
		}
	}

}