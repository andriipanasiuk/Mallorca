package mallorcatour.core.game.situation;

import mallorcatour.bot.C;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.game.interfaces.IGameObserver;
import mallorcatour.tools.Log;

public class NoStrengthSituationHandler implements ISituationHandler, IGameObserver<IGameInfo> {
	private int heroActionCount, countOfHeroAggressive, villainActionCount, countOfOppAggressive;
	private boolean wasHeroPreviousAggressive, wasVillainPreviousAggressive;
	protected IGameInfo gameInfo;
	private double effectiveStack;
	private double pot;
	private final String hero;
	private final boolean trackHero;
	private double toCall;
	private double playerToCall;

	public NoStrengthSituationHandler(String hero, boolean trackHero) {
		this.hero = hero;
		this.trackHero = trackHero;
	}

	@Override
	public LocalSituation getSituation() throws IllegalStateException {
		boolean isOnButton = gameInfo.onButton(hero) ^ !trackHero;
		double potAfterCall = playerToCall + pot;
		double potOdds = playerToCall / potAfterCall;
		double potToStackCoeff = potAfterCall / (potAfterCall + effectiveStack);

		Log.f(this + " " + C.TO_CALL + ": " + playerToCall + " " + C.POT + ": " + pot);
		PokerStreet street = gameInfo.getStage();
		if (street == null) {
			throw new IllegalStateException("Incorrect street value");
		}
		LocalSituation result = new LocalSituation(street.intValue());
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
		double amount;
		if (action.isAggressive()) {
			amount = action.getAmount();
			if (amount >= effectiveStack) {
				amount = effectiveStack;
			}
		} else {
			amount = 0;
		}
		if (!isTrackPlayer(name)) {
			playerToCall = amount;
		}
		toCall = amount;
		pot += amount;
		effectiveStack -= amount;
		if (isTrackPlayer(name)) {
			onHeroActed(action);
		} else {
			onVillainActed(action);
		}
	}

}