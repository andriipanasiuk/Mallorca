package mallorcatour.core.game.advice;


public interface IAdviceInterpreter{
	Advice create(boolean canRaise, double... output);
}