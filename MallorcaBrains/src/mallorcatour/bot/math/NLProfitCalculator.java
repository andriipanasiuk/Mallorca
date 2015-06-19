package mallorcatour.bot.math;

import mallorcatour.bot.C;
import mallorcatour.brains.IAdvisor;
import mallorcatour.brains.math.StrengthManager;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.interfaces.IAggressionInfo;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.spectrum.Spectrum;
import mallorcatour.tools.Log;

public class NLProfitCalculator implements IProfitCalculator {

	public NLProfitCalculator(IAdvisor villainModeller, StrengthManager strengthManager) {
		gameSolver = new NLGameSolver(villainModeller);
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
				Log.f(C.HERO + " " + C.FIRST + " " + C.ACTION + " on " + C.PREFLOP + " " + map.toString());
			} else {
				map = gameSolver.onSecondActionPreflop(gameInfo, situation, gameInfo.getBankRollAtRisk(), gameInfo
						.getPotSize(), gameInfo.getAmountToCall(), villainSpectrum,
						new HoleCards(holeCard1, holeCard2), strengthManager.preflop, gameInfo.onButton(), gameInfo
								.getBigBlindSize());
				Log.f(C.HERO + " " + C.SECOND + " " + C.ACTION + " on " + C.PREFLOP + " " + map.toString());
			}
		} else if (gameInfo.isFlop()) {
			if (gameInfo.getAmountToCall() == 0 && !gameInfo.onButton()) {
				map = gameSolver.onFirstActionFlop(gameInfo, situation, gameInfo.getBankRollAtRisk(),
						gameInfo.getPotSize(), villainSpectrum, gameInfo.getFlop(),
						new HoleCards(holeCard1, holeCard2), strengthManager.flop, wasVillainPreviousAggressive,
						gameInfo.onButton(), gameInfo.getBigBlindSize());
				Log.f(C.HERO + " " + C.FIRST + " " + C.ACTION + " on " + C.FLOP + " " + map.toString());
			} else {
				map = gameSolver.onSecondActionFlop(gameInfo, situation, gameInfo.getBankRollAtRisk(),
						gameInfo.getPotSize(), gameInfo.getAmountToCall(), villainSpectrum, gameInfo.getFlop(),
						new HoleCards(holeCard1, holeCard2), strengthManager.flop, gameInfo.onButton(),
						gameInfo.getBigBlindSize());
				Log.f(C.HERO + " " + C.SECOND + " " + C.ACTION + " on " + C.FLOP + ": " + map.toString());
			}
		} else if (gameInfo.isTurn()) {
			if (gameInfo.getAmountToCall() == 0 && !gameInfo.onButton()) {
				map = gameSolver.onFirstActionTurn(gameInfo, situation, gameInfo.getBankRollAtRisk(), gameInfo
						.getPotSize(), villainSpectrum, gameInfo.getFlop(), gameInfo.getTurn(), new HoleCards(
						holeCard1, holeCard2), strengthManager.turn, wasVillainPreviousAggressive, gameInfo.onButton(),
						gameInfo.getBigBlindSize());
				Log.f(C.HERO + " " + C.FIRST + " " + C.ACTION + " on " + C.TURN + ": " + map.toString());
			} else {
				map = gameSolver.onSecondActionTurn(gameInfo, situation, gameInfo.getBankRollAtRisk(),
						gameInfo.getPotSize(), gameInfo.getAmountToCall(), villainSpectrum, gameInfo.getFlop(),
						gameInfo.getTurn(), new HoleCards(holeCard1, holeCard2), strengthManager.turn,
						gameInfo.onButton(), gameInfo.getBigBlindSize());
				Log.f(C.HERO + " " + C.SECOND + " " + C.ACTION + " on " + C.TURN + ": " + map.toString());
			}
		} else if (gameInfo.isRiver()) {
			if (gameInfo.getAmountToCall() == 0 && !gameInfo.onButton()) {
				map = gameSolver.onFirstActionRiver(gameInfo, situation, gameInfo.getBankRollAtRisk(),
						gameInfo.getPotSize(), villainSpectrum, gameInfo.getFlop(), gameInfo.getTurn(),
						gameInfo.getRiver(), new HoleCards(holeCard1, holeCard2), strengthManager.river,
						wasVillainPreviousAggressive, gameInfo.onButton(), gameInfo.getBigBlindSize());
				Log.f(C.HERO + " " + C.FIRST + " " + C.ACTION + " on " + C.RIVER + ": " + map.toString());
			} else {
				map = gameSolver.onSecondActionRiver(gameInfo, situation, gameInfo.getBankRollAtRisk(),
						gameInfo.getPotSize(), gameInfo.getAmountToCall(), villainSpectrum, gameInfo.getFlop(),
						gameInfo.getTurn(), gameInfo.getRiver(), new HoleCards(holeCard1, holeCard2),
						strengthManager.river, gameInfo.onButton(), gameInfo.getBigBlindSize());
				Log.f(C.HERO + " " + C.SECOND + " " + C.ACTION + " on " + C.RIVER + ": " + map.toString());
			}
		} else {
			throw new RuntimeException();
		}
		return map;
	}

}