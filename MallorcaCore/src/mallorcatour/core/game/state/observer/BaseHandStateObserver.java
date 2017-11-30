package mallorcatour.core.game.state.observer;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.game.interfaces.IGameObserver;
import mallorcatour.core.game.state.HandState;
import mallorcatour.core.game.state.HandStateHolder;
import mallorcatour.tools.Log;

/**
 * Следит за игрой и в каждый момент может построить обьект {@link HandState},
 * который определяет текущую сложившуюся ситуацию в игре между 2 игроками.
 * Переименовать в HandStateObserver
 */
public class BaseHandStateObserver implements HandStateHolder, IGameObserver<IGameInfo> {
	private int heroActionCount, countOfHeroAggressive, villainActionCount, countOfOppAggressive;
	private boolean wasHeroPreviousAggressive, wasVillainPreviousAggressive;
	protected IGameInfo gameInfo;
	private double effectiveStack;
	private double pot;
	private final String hero;
	private final boolean trackHero;
	private double toCall;
	private double playerToCall;

	public BaseHandStateObserver(String hero, boolean trackHero) {
		this.hero = hero;
		this.trackHero = trackHero;
	}

	@Override
	public HandState getSituation() throws IllegalStateException {
		boolean isOnButton = gameInfo.onButton(hero) ^ !trackHero;
		double potAfterCall = playerToCall + pot;
		double potOdds = playerToCall / potAfterCall;
		double potToStackCoeff = potAfterCall / (potAfterCall + effectiveStack);

		Log.f(this + " to call: " + playerToCall + " " + "pot: " + pot);
		PokerStreet street = gameInfo.getStage();
		if (street == null) {
			throw new IllegalStateException("Incorrect street value");
		}
		HandState result = new HandState(street.intValue());
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
		return "Sit. handler for "
				+ (trackHero ? ("hero" + " " + hero) : ("villain" + " " + gameInfo.getVillain(hero).name));
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