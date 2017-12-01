package mallorcatour.bot.math;

import mallorcatour.core.game.advice.Advisor;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.interfaces.GameContext;
import mallorcatour.core.game.state.HandState;
import mallorcatour.core.spectrum.Spectrum;

public class NLProfitCalculator implements IProfitCalculator {

	public NLProfitCalculator(Advisor villainModel, StrengthManager strengthManager) {
		gameSolver = new NLGameSolver(villainModel);
		this.strengthManager = strengthManager;
	}

	private final NLGameSolver gameSolver;
	private final StrengthManager strengthManager;

	@Override
	public ActionDistribution getProfitMap(GameContext gameInfo, HandState situation, Card holeCard1,
										   Card holeCard2, Spectrum villainSpectrum) {
		int heroActionCount = situation.getHeroActionCount();
		boolean wasVillainPreviousAggressive = situation.wasVillainPreviousAggresive();
		ActionDistribution map;
		boolean onButton = situation.isHeroOnButton();
		double amountToCall = situation.getAmountToCall();
		if (gameInfo.isPreFlop()) {
			if (onButton && heroActionCount == 0) {
				map = gameSolver.onFirstActionPreFlop(gameInfo, situation, gameInfo.getBankRollAtRisk(),
						gameInfo.getPotSize(), villainSpectrum, new HoleCards(holeCard1, holeCard2),
						strengthManager.preflop, false, true, gameInfo.getBigBlindSize());
			} else {
				map = gameSolver.onSecondActionPreflop(gameInfo, situation, gameInfo.getBankRollAtRisk(), gameInfo
						.getPotSize(), amountToCall, villainSpectrum,
						new HoleCards(holeCard1, holeCard2), strengthManager.preflop, onButton, gameInfo
								.getBigBlindSize());
			}
		} else if (gameInfo.isFlop()) {
			if (amountToCall == 0 && !onButton) {
				map = gameSolver.onFirstActionFlop(gameInfo, situation, gameInfo.getBankRollAtRisk(),
						gameInfo.getPotSize(), villainSpectrum, gameInfo.getFlop(),
						new HoleCards(holeCard1, holeCard2), strengthManager.flop, wasVillainPreviousAggressive,
						onButton, gameInfo.getBigBlindSize());
			} else {
				map = gameSolver.onSecondActionFlop(gameInfo, situation, gameInfo.getBankRollAtRisk(),
						gameInfo.getPotSize(), amountToCall, villainSpectrum, gameInfo.getFlop(),
						new HoleCards(holeCard1, holeCard2), strengthManager.flop, onButton,
						gameInfo.getBigBlindSize());
			}
		} else if (gameInfo.isTurn()) {
			if (amountToCall == 0 && !onButton) {
				map = gameSolver.onFirstActionTurn(gameInfo, situation, gameInfo.getBankRollAtRisk(), gameInfo
						.getPotSize(), villainSpectrum, gameInfo.getFlop(), gameInfo.getTurn(), new HoleCards(
						holeCard1, holeCard2), strengthManager.turn, wasVillainPreviousAggressive, onButton,
						gameInfo.getBigBlindSize());
			} else {
				map = gameSolver.onSecondActionTurn(gameInfo, situation, gameInfo.getBankRollAtRisk(),
						gameInfo.getPotSize(), amountToCall, villainSpectrum, gameInfo.getFlop(),
						gameInfo.getTurn(), new HoleCards(holeCard1, holeCard2), strengthManager.turn,
						onButton, gameInfo.getBigBlindSize());
			}
		} else if (gameInfo.isRiver()) {
			if (amountToCall == 0 && !onButton) {
				map = gameSolver.onFirstActionRiver(gameInfo, situation, gameInfo.getBankRollAtRisk(),
						gameInfo.getPotSize(), villainSpectrum, gameInfo.getFlop(), gameInfo.getTurn(),
						gameInfo.getRiver(), new HoleCards(holeCard1, holeCard2), strengthManager.river,
						wasVillainPreviousAggressive, onButton, gameInfo.getBigBlindSize());
			} else {
				map = gameSolver.onSecondActionRiver(gameInfo, situation, gameInfo.getBankRollAtRisk(),
						gameInfo.getPotSize(), amountToCall, villainSpectrum, gameInfo.getFlop(),
						gameInfo.getTurn(), gameInfo.getRiver(), new HoleCards(holeCard1, holeCard2),
						strengthManager.river, onButton, gameInfo.getBigBlindSize());
			}
		} else {
			throw new RuntimeException();
		}
		return map;
	}

}