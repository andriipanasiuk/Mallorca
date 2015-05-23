package mallorcatour.core.game.advice;

import mallorcatour.core.game.Action;

public interface IAdvice {
	Action getAction();

	double getFold();

	double getPassive();

	double getAggressive();
}