package mallorcatour.interfaces;

import mallorcatour.core.game.advice.Advice;

public interface IAdviceInterpreter{
	Advice create(boolean canRaise, double... output);
}