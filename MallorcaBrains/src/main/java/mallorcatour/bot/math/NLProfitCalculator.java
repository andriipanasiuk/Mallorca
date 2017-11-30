package mallorcatour.bot.math;

import mallorcatour.core.game.advice.Advisor;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.interfaces.IAggressionInfo;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.spectrum.Spectrum;

public class NLProfitCalculator implements IProfitCalculator {

	public NLProfitCalculator(Advisor villainModel, StrengthManager strengthManager) {
		gameSolver = new NLGameSolver(villainModel);
		this.strengthManager = strengthManager;
	}

	private final NLGameSolver gameSolver;
	private final StrengthManager strengthManager;

	@Override
	public ActionDistribution getProfitMap(IPlayerGameInfo gameInfo, IAggressionInfo situation, Card holeCard1,
			Card holeCard2, Spectrum villainSpectrum) {
		int heroActionCount = situation.getHeroActionCount();
		boolean wasVillainPreviousAggressive = situation.wasVillainPreviousAggresive();
		ActionDistribution map;
		if (gameInfo.isPreFlop()) {
			if (gameInfo.onButton() && heroActionCount == 0) {
				map = gameSolver.onFirstActionPreFlop(gameInfo, situation, gameInfo.getBankRollAtRisk(),
						gameInfo.getPotSize(), villainSpectrum, new HoleCards(holeCard1, holeCard2),
						strengthManager.preflop, false, true, gameInfo.getBigBlindSize());
			} else {
				map = gameSolver.onSecondActionPreflop(gameInfo, situation, gameInfo.getBankRollAtRisk(), gameInfo
						.getPotSize(), gameInfo.getAmountToCall(), villainSpectrum,
						new HoleCards(holeCard1, holeCard2), strengthManager.preflop, gameInfo.onButton(), gameInfo
								.getBigBlindSize());
			}
		} else if (gameInfo.isFlop()) {
			if (gameInfo.getAmountToCall() == 0 && !gameInfo.onButton()) {
				map = gameSolver.onFirstActionFlop(gameInfo, situation, gameInfo.getBankRollAtRisk(),
						gameInfo.getPotSize(), villainSpectrum, gameInfo.getFlop(),
						new HoleCards(holeCard1, holeCard2), strengthManager.flop, wasVillainPreviousAggressive,
						gameInfo.onButton(), gameInfo.getBigBlindSize());
			} else {
				map = gameSolver.onSecondActionFlop(gameInfo, situation, gameInfo.getBankRollAtRisk(),
						gameInfo.getPotSize(), gameInfo.getAmountToCall(), villainSpectrum, gameInfo.getFlop(),
						new HoleCards(holeCard1, holeCard2), strengthManager.flop, gameInfo.onButton(),
						gameInfo.getBigBlindSize());
			}
		} else if (gameInfo.isTurn()) {
			if (gameInfo.getAmountToCall() == 0 && !gameInfo.onButton()) {
				map = gameSolver.onFirstActionTurn(gameInfo, situation, gameInfo.getBankRollAtRisk(), gameInfo
						.getPotSize(), villainSpectrum, gameInfo.getFlop(), gameInfo.getTurn(), new HoleCards(
						holeCard1, holeCard2), strengthManager.turn, wasVillainPreviousAggressive, gameInfo.onButton(),
						gameInfo.getBigBlindSize());
			} else {
				map = gameSolver.onSecondActionTurn(gameInfo, situation, gameInfo.getBankRollAtRisk(),
						gameInfo.getPotSize(), gameInfo.getAmountToCall(), villainSpectrum, gameInfo.getFlop(),
						gameInfo.getTurn(), new HoleCards(holeCard1, holeCard2), strengthManager.turn,
						gameInfo.onButton(), gameInfo.getBigBlindSize());
			}
		} else if (gameInfo.isRiver()) {
			if (gameInfo.getAmountToCall() == 0 && !gameInfo.onButton()) {
				map = gameSolver.onFirstActionRiver(gameInfo, situation, gameInfo.getBankRollAtRisk(),
						gameInfo.getPotSize(), villainSpectrum, gameInfo.getFlop(), gameInfo.getTurn(),
						gameInfo.getRiver(), new HoleCards(holeCard1, holeCard2), strengthManager.river,
						wasVillainPreviousAggressive, gameInfo.onButton(), gameInfo.getBigBlindSize());
			} else {
				map = gameSolver.onSecondActionRiver(gameInfo, situation, gameInfo.getBankRollAtRisk(),
						gameInfo.getPotSize(), gameInfo.getAmountToCall(), villainSpectrum, gameInfo.getFlop(),
						gameInfo.getTurn(), gameInfo.getRiver(), new HoleCards(holeCard1, holeCard2),
						strengthManager.river, gameInfo.onButton(), gameInfo.getBigBlindSize());
			}
		} else {
			throw new RuntimeException();
		}
		return map;
	}

}