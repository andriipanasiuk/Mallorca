package mallorcatour.neural.core;

import mallorcatour.core.vector.IVector;

public interface ILearningExample<I extends IVector, O extends IVector> {
	public I getInput();

	public O getOutput();

	public abstract int getOutputDimension();

	public abstract int getInputDimension();
}