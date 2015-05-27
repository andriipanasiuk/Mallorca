package mallorcatour.core.game.situation;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.game.interfaces.IGameObserver;
import mallorcatour.util.Log;

public class NoStrengthSituationHandler implements ISituationHandler, IGameObserver<IGameInfo> {
	protected Card flop1, flop2, flop3, turn, river;
	protected int heroActionCount, countOfHeroAggressive, villainActionCount, countOfOppAggressive;
	protected IGameInfo gameInfo;
	protected boolean wasHeroPreviousAggressive, wasVillainPreviousAggressive;
	protected final LimitType limitType;
	protected final String hero;
	private double heroToCall;
	private double effectiveStack;
	private double pot;

	public NoStrengthSituationHandler(LimitType limitType, String hero) {
		this.limitType = limitType;
		this.hero = hero;
	}

	@Override
	public LocalSituation getSituation() {
		LocalSituation result = null;
		boolean isOnButton = gameInfo.onButton(hero);
		double potAfterCall = heroToCall + pot;
		double potOdds = heroToCall / potAfterCall;

		Log.d("Ef. stack: " + effectiveStack);
		Log.d("Pot: " + pot);
		Log.d("To call: " + heroToCall);
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
		result.setPotToStackOdds(potAfterCall / (potAfterCall + effectiveStack));
		result.setFLPotSize(1 - (2 * gameInfo.getBigBlindSize()) / pot);
		result.canRaise(effectiveStack > 0);
		return result;
	}

	@Override
	public void onHandStarted(IGameInfo gameInfo) {
		this.gameInfo = gameInfo;
		effectiveStack = gameInfo.getBankRollAtRisk();
		if (gameInfo.onButton(hero)) {
			heroToCall = gameInfo.getBigBlindSize() / 2;
		}
		pot = gameInfo.getBigBlindSize() * 3 / 2;
		heroActionCount = 0;
		countOfHeroAggressive = 0;
		villainActionCount = 0;
		countOfOppAggressive = 0;
		wasHeroPreviousAggressive = false;
		wasVillainPreviousAggressive = false;
	}

	private void onHeroActed(Action action) {
		if (action.isAggressive()) {
			wasHeroPreviousAggressive = true;
			countOfHeroAggressive++;
		} else if (action.isPassive()) {
			wasHeroPreviousAggressive = false;
		}
		heroActionCount++;
	}

	private void onVillainActed(Action action) {
		if (action.isAggressive()) {
			this.heroToCall = action.getAmount();
			wasVillainPreviousAggressive = true;
			villainActionCount++;
			countOfOppAggressive++;
		} else if (action.isPassive()) {
			this.heroToCall = 0;
			wasVillainPreviousAggressive = false;
			villainActionCount++;
		}
	}

	@Override
	public void onHandEnded() {
		// do nothing
	}

	@Override
	public void onStageEvent(PokerStreet street) {
		heroToCall = 0;
	}

	@Override
	public void onActed(Action action, double toCall, String name) {
		pot += toCall;
		if (action.isAggressive()) {
			if (action.getAmount() < effectiveStack) {
				effectiveStack -= action.getAmount();
				toCall = action.getAmount();
				pot += action.getAmount();
			} else {
				// real and not side pot
				pot += effectiveStack;
				toCall = effectiveStack;
				effectiveStack = 0;
			}
		} else {
			toCall = 0;
		}
		if (name.equals(hero)) {
			onHeroActed(action);
		} else {
			onVillainActed(action);
		}
	}

}