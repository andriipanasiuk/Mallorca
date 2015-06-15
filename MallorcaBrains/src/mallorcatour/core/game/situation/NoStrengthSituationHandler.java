package mallorcatour.core.game.situation;

import mallorcatour.bot.C;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.game.interfaces.IGameObserver;
import mallorcatour.tools.Log;

public class NoStrengthSituationHandler implements ISituationHandler, IGameObserver<IGameInfo> {
	private int heroActionCount, countOfHeroAggressive, villainActionCount, countOfOppAggressive;
	private boolean wasHeroPreviousAggressive, wasVillainPreviousAggressive;
	protected IGameInfo gameInfo;
	protected final LimitType limitType;
	private double effectiveStack;
	private double pot;
	private final String hero;
	private final boolean trackHero;
	private double toCall;
	private double playerToCall;

	public NoStrengthSituationHandler(LimitType limitType, String hero, boolean trackHero) {
		this.limitType = limitType;
		this.hero = hero;
		this.trackHero = trackHero;
	}

	@Override
	public LocalSituation getSituation() throws IllegalStateException {
		LocalSituation result = null;
		boolean isOnButton = gameInfo.onButton(hero) ^ !trackHero;
		double potAfterCall = playerToCall + pot;
		double potOdds = playerToCall / potAfterCall;
		double potToStackCoeff = potAfterCall / (potAfterCall + effectiveStack);

		Log.f(this + " " + C.TO_CALL + ": " + playerToCall + " " + C.POT + ": " + pot);
		if (gameInfo.isPreFlop()) {
			result = new LocalSituation(LocalSituation.PREFLOP, limitType);
		} else if (gameInfo.isFlop()) {
			result = new LocalSituation(LocalSituation.FLOP, limitType);
		} else if (gameInfo.isTurn()) {
			result = new LocalSituation(LocalSituation.TURN, limitType);
		} else if (gameInfo.isRiver()) {
			result = new LocalSituation(LocalSituation.RIVER, limitType);
		} else {
			throw new IllegalStateException("Incorrect street value");
		}
		result.setHeroAggresionActionCount(countOfHeroAggressive);
		result.setHeroActionCount(heroActionCount);
		result.setVillainAggresionActionCount(countOfOppAggressive);
		result.setVillainActionCount(villainActionCount);
		result.wasHeroPreviousAggresive(wasHeroPreviousAggressive);
		result.wasOpponentPreviousAggresive(wasVillainPreviousAggressive);
		result.setPotOdds(potOdds);
		result.isOnButton(isOnButton);
		result.setPotToStackOdds(potToStackCoeff);
		result.canRaise(effectiveStack > 0);
		return result;
	}

	@Override
	public void onHandStarted(IGameInfo gameInfo) {
		this.gameInfo = gameInfo;
		effectiveStack = gameInfo.getBankRollAtRisk();
		if (gameInfo.onButton(hero) ^ !trackHero) {
			playerToCall = gameInfo.getBigBlindSize() / 2;
		}
		pot = gameInfo.getBigBlindSize() * 3 / 2;
		toCall = gameInfo.getBigBlindSize() / 2;
		heroActionCount = 0;
		countOfHeroAggressive = 0;
		villainActionCount = 0;
		countOfOppAggressive = 0;
		wasHeroPreviousAggressive = false;
		wasVillainPreviousAggressive = false;
	}

	private void onHeroActed(Action action) {
		wasHeroPreviousAggressive = action.isAggressive();
		if (action.isAggressive()) {
			countOfHeroAggressive++;
		}
		heroActionCount++;
	}

	private void onVillainActed(Action action) {
		wasVillainPreviousAggressive = action.isAggressive();
		if (action.isAggressive()) {
			countOfOppAggressive++;
		} else if (action.isPassive()) {
		}
		villainActionCount++;
	}

	@Override
	public void onHandEnded() {
		// do nothing
	}

	@Override
	public void onStageEvent(PokerStreet street) {
		toCall = 0;
		playerToCall = 0;
	}

	private boolean isTrackPlayer(String name) {
		return name.equals(hero) ^ !trackHero;
	}

	@Override
	public String toString() {
		return "Sit. " + C.HANDLER + " for "
				+ (trackHero ? (C.HERO + " " + hero) : (C.VILLAIN + " " + gameInfo.getVillain(hero).name));
	}

	@Override
	public void onActed(Action action, double toCallParam, String name) {
		if (!action.isFold()) {
			pot += toCall;
		}
		if (action.isAggressive()) {
			if (action.getAmount() < effectiveStack) {
				if (!isTrackPlayer(name)) {
					playerToCall = action.getAmount();
				}
				toCall = action.getAmount();
				pot += action.getAmount();
				effectiveStack -= action.getAmount();
			} else {
				if (!isTrackPlayer(name)) {
					playerToCall = effectiveStack;
				}
				toCall = effectiveStack;
				pot += effectiveStack;
				effectiveStack = 0;
			}
		} else {
			if (!isTrackPlayer(name)) {
				playerToCall = 0;
			}
			toCall = 0;
		}
		if (isTrackPlayer(name)) {
			onHeroActed(action);
		} else {
			onVillainActed(action);
		}
	}

}