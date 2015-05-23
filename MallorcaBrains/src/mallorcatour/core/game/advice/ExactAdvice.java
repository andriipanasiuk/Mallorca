package mallorcatour.core.game.advice;

import mallorcatour.core.game.Action;

public class ExactAdvice implements IAdvice {

	private final Action exactAction;

	public ExactAdvice(Action exactAction) {
		this.exactAction = exactAction;
	}

	@Override
	public Action getAction() {
		return exactAction;
	}

	@Override
	public double getFold() {
		return exactAction.isFold() ? 1 : 0;
	}

	@Override
	public double getPassive() {
		return exactAction.isPassive() ? 1 : 0;
	}

	@Override
	public double getAggressive() {
		return exactAction.isAggressive() ? 1 : 0;
	}

	@Override
	public String toString() {
		return "Exact action " + exactAction;
	}
}