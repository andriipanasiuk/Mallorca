package golden;

import java.util.Map;

final class ShadowHand {

	final long bigBlind;
	final long[] stacks = new long[2];
	final long[] bets = new long[2];
	final long[] committed = new long[2];
	final boolean[] canRaise = { true, true };
	long lastRaise;

	ShadowHand(long bigBlind, long heroStack, long villainStack) {
		this.bigBlind = bigBlind;
		this.lastRaise = bigBlind;
		stacks[0] = heroStack;
		stacks[1] = villainStack;
	}

	void put(int seat, long amount) {
		bets[seat] += amount;
		committed[seat] += amount;
		stacks[seat] -= amount;
	}

	void newStreet() {
		bets[0] = 0;
		bets[1] = 0;
		lastRaise = bigBlind;
		canRaise[0] = true;
		canRaise[1] = true;
	}

	long currentBet() {
		return Math.max(bets[0], bets[1]);
	}

	long toCall(int seat) {
		return currentBet() - bets[seat];
	}

	long pot() {
		return committed[0] + committed[1];
	}

	void raiseTo(int seat, long to) {
		long increment = to - currentBet();
		put(seat, to - bets[seat]);
		if (increment >= Math.max(lastRaise, bigBlind)) {
			lastRaise = increment;
			canRaise[1 - seat] = true;
		} else {
			canRaise[1 - seat] = false;
		}
	}

	Long raiseMin(int seat) {
		long toCall = toCall(seat);
		if (!canRaise[seat] || stacks[seat] <= toCall || stacks[1 - seat] <= 0) {
			return null;
		}
		return Math.min(currentBet() + Math.max(lastRaise, bigBlind), bets[seat] + stacks[seat]);
	}

	long raiseMax(int seat) {
		return bets[seat] + stacks[seat];
	}

	Map<String, Object> legal(int seat) {
		long toCall = toCall(seat);
		Long min = raiseMin(seat);
		Object raise = min == null ? null : Json.object("min", min, "max", raiseMax(seat));
		return Json.object("fold", toCall > 0, "check", toCall == 0, "call",
				toCall > 0 ? (Object) Math.min(toCall, stacks[seat]) : null, "raise", raise);
	}

	boolean isLegal(int seat, String kind, long to) {
		long toCall = toCall(seat);
		if ("fold".equals(kind)) {
			return toCall > 0;
		}
		if ("check".equals(kind)) {
			return toCall == 0;
		}
		if ("call".equals(kind)) {
			return toCall > 0;
		}
		Long min = raiseMin(seat);
		return min != null && to >= min && to <= raiseMax(seat);
	}
}
