package mallorcatour.core.game;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class Action implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final double THRESHOLD_FOR_GOING_ALLIN = 0.6D;
	private static final double PERCENT_OF_POT_FOR_BET = 0.7D;
	private static final double PERCENT_OF_POT_FOR_RAISE = 0.7D;
	private double amount;
	private Type type;
	private double percentOfPot;
	private static final Action ALL_IN_ACTION = new Action(Type.AGGRESSIVE, Double.MAX_VALUE);

	private Action(Type type, double amount) {
		this.type = type;
		this.amount = amount;
	}

	private Action(Type type) {
		this.type = type;
	}

	public double getAmount() {
		return this.amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public double getPercentOfPot() {
		return this.percentOfPot;
	}

	public boolean isFold() {
		return this.type == Type.FOLD;
	}

	public boolean isPassive() {
		return this.type == Type.PASSIVE;
	}

	public boolean isAggressive() {
		return this.type == Type.AGGRESSIVE;
	}

	public boolean isAllin() {
		return this == ALL_IN_ACTION;
	}

	public static Action passive() {
		Action result = new Action(Type.PASSIVE);
		return result;
	}

	public static Action aggressive() {
		Action result = new Action(Type.AGGRESSIVE);
		return result;
	}

	public static Action createBetAction(double pot, double percent, double effectiveStack) {
		double amountOfBet = percent * pot;
		if (effectiveStack - amountOfBet < THRESHOLD_FOR_GOING_ALLIN * (pot + 2 * amountOfBet)) {
			return allInAction();
		}
		Action result = new Action(Type.AGGRESSIVE, amountOfBet);
		result.percentOfPot = percent;
		return result;
	}

	public static Action createBetAction(double pot, double effectiveStack) {
		return createBetAction(pot, PERCENT_OF_POT_FOR_BET, effectiveStack);
	}

	public static Action createRaiseAction(double toCall, double pot, double effectiveStack) {
		return createRaiseAction(toCall, pot, effectiveStack, PERCENT_OF_POT_FOR_RAISE);
	}

	public static Action createRaiseAction(double toCall, double pot, double effectiveStack, double percent) {
		double raiseAmount = percent * (toCall + pot);
		if (effectiveStack - raiseAmount < THRESHOLD_FOR_GOING_ALLIN * (pot + toCall + 2 * raiseAmount)) {
			return ALL_IN_ACTION;
		}
		Action result = new Action(Type.AGGRESSIVE, raiseAmount);
		result.percentOfPot = percent;
		return result;
	}

	public static Action createRaiseAction(double amount, double percent) {
		Action result = new Action(Type.AGGRESSIVE, amount);
		result.percentOfPot = percent;
		return result;
	}

	public static Action allInAction() {
		return ALL_IN_ACTION;
	}

	public static Action foldAction() {
		return new Action(Type.FOLD);
	}

	public static Action checkAction() {
		return new Action(Type.PASSIVE, 0.0D);
	}

	public static Action betAction(double amount) {
		return new Action(Type.AGGRESSIVE, amount);
	}

	public static Action raiseAction(double amount) {
		return new Action(Type.AGGRESSIVE, amount);
	}

	public static Action callAction(double toCall) {
		Action result = new Action(Type.PASSIVE, toCall);
		return result;
	}

	public boolean isCall() {
		return (this.type == Type.PASSIVE) && (this.amount != 0.0D);
	}

	public boolean isCheck() {
		return (this.type == Type.PASSIVE) && (this.amount == 0.0D);
	}

	public String toString() {
		if (this.type == Type.AGGRESSIVE) {
			if (this != ALL_IN_ACTION) {
				return "Raise " + this.amount;
			}
			return "All-in";
		}
		if (this.type == Type.PASSIVE) {
			if (this.amount == 0.0D) {
				return "Check";
			}

			return "Call " + this.amount;
		}

		return this.type.toString();
	}

	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Action other = (Action) obj;
		if ((other.type == Type.FOLD) && (this.type == Type.FOLD)) {
			return true;
		}
		if (Double.doubleToLongBits(this.amount) != Double.doubleToLongBits(other.amount)) {
			return false;
		}
		if ((this.type != other.type) && ((this.type == null) || (!this.type.equals(other.type)))) {
			return false;
		}
		if (Double.doubleToLongBits(this.percentOfPot) != Double.doubleToLongBits(other.percentOfPot)) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		int hash = 5;
		return hash;
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		if (this.type.equals(Type.FOLD))
			this.type = Type.FOLD;
		else if (this.type.equals(Type.PASSIVE))
			this.type = Type.PASSIVE;
		else if (this.type.equals(Type.AGGRESSIVE))
			this.type = Type.AGGRESSIVE;
		else
			throw new IllegalArgumentException();
	}

	private static class Type implements Serializable {
		private static final long serialVersionUID = 1L;
		private String action;
		public static final Type FOLD = new Type("Fold");
		public static final Type PASSIVE = new Type("Passive");
		public static final Type AGGRESSIVE = new Type("Aggressive");

		private Type(String action) {
			this.action = action;
		}

		public String toString() {
			return this.action;
		}

		public boolean equals(Object other) {
			if (!(other instanceof Type)) {
				return false;
			}
			if (!((Type) other).action.equals(this.action)) {
				return false;
			}
			return true;
		}
	}
}
