package golden;

import java.util.Random;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.interfaces.GameContext;
import mallorcatour.core.player.interfaces.Player;

final class ScriptedOpponent implements Player {

	private static final int SEAT = 1;
	private static final double[] POT_FRACTIONS = { 0, 0, 0.5, 0.5, 0.75, 0.75, 1, -1 };

	private final String name;
	private final Random random;
	private final DecisionHarness harness;

	ScriptedOpponent(String name, long seed, DecisionHarness harness) {
		this.name = name;
		this.random = new Random(seed);
		this.harness = harness;
	}

	@Override
	public Action getAction() {
		ShadowHand hand = harness.shadow();
		long toCall = hand.toCall(SEAT);
		Long min = hand.raiseMin(SEAT);
		double roll = random.nextDouble();
		if (toCall > 0) {
			if (roll < 1.0 / 3) {
				return Action.fold();
			}
			if (roll < 2.0 / 3 || min == null) {
				return Action.callAction(Math.min(toCall, hand.stacks[SEAT]));
			}
			return raise(hand, min, toCall);
		}
		if (roll < 0.5 || min == null) {
			return Action.checkAction();
		}
		return raise(hand, min, toCall);
	}

	private Action raise(ShadowHand hand, long min, long toCall) {
		long max = hand.raiseMax(SEAT);
		double fraction = POT_FRACTIONS[random.nextInt(POT_FRACTIONS.length)];
		long to;
		if (fraction < 0) {
			to = max;
		} else {
			to = hand.currentBet() + Math.round(fraction * (hand.pot() + toCall));
		}
		to = Math.max(min, Math.min(max, to));
		return Action.raiseAction(to - hand.currentBet());
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void onHandStarted(GameContext gameInfo) {
	}

	@Override
	public void onStageEvent(PokerStreet street) {
	}

	@Override
	public void onActed(Action action, double toCall, String name) {
	}

	@Override
	public void onHandEnded() {
	}

	@Override
	public void onHoleCards(Card c1, Card c2) {
	}
}
