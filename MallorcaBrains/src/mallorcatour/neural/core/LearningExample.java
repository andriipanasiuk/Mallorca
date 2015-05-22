/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.neural.core;

import java.io.IOException;
import java.util.Comparator;

import mallorcatour.core.vector.BaseVector;
import mallorcatour.core.vector.IVector;
import mallorcatour.core.vector.VectorUtils;

/**
 *
 * @author Andrew
 */
public class LearningExample implements ILearningExample<BaseVector, BaseVector>{

	private static final String VECTOR_SEPARATOR = "-->";
    protected BaseVector inputVector;
    protected BaseVector outputVector;

    protected LearningExample() {
        //do nothing
    }

    public LearningExample(BaseVector inputVector) {
        this.inputVector = inputVector;
    }

    public LearningExample(BaseVector inputVector, BaseVector outputVector) {
        this.inputVector = inputVector;
        this.outputVector = outputVector;
    }

    public static LearningExample valueOf(String value) throws IOException{
        BaseVector inputVector, outputVector;
        String[] vectors = value.split(VECTOR_SEPARATOR);
		if (vectors.length != 2) {
			throw new IOException("In every row must be " + "exactly 2 vectors");
        }
        inputVector = BaseVector.valueOf(vectors[0]);
        outputVector = BaseVector.valueOf(vectors[1]);
        return new LearningExample(inputVector, outputVector);
    }

    public BaseVector getInput() {
        return inputVector;
    }

    public BaseVector getOutput() {
        return outputVector;
    }

	public static String toString(IVector input, IVector output) {
		return VectorUtils.toString(input) + VECTOR_SEPARATOR + VectorUtils.toString(output);
	}

	@Override
	public String toString() {
		return toString(inputVector, outputVector);
	}

    @Override
	public int getInputDimension() {
        return getInput().getValues().size();
    }

    @Override
	public int getOutputDimension() {
        return getOutput().getValues().size();
    }

    public static Comparator<ILearningExample> inputComparator(final int... columns) {
        return new Comparator<ILearningExample>() {

            public int compare(ILearningExample o1, ILearningExample o2) {
                return VectorUtils.comparator(columns).compare(o1.getInput(), o2.getInput());
            }
        };
    }
}
