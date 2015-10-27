package mallorcatour.core.game.advice;

import mallorcatour.core.game.Action;
import mallorcatour.core.vector.IVector;

public interface IAdvice extends IVector {
	Action getAction();

	double getFold();

	double getPassive();

	double getAggressive();
}