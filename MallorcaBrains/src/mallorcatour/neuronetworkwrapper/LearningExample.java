/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.neuronetworkwrapper;

import java.io.IOException;
import java.util.Comparator;

import mallorcatour.vector.IVector;
import mallorcatour.vector.VectorUtils;

/**
 *
 * @author Andrew
 */
public class LearningExample {

    private static final String VECTOR_SEPARATOR = "-->";
    protected IVector inputVector;
    protected IVector outputVector;

    protected LearningExample() {
        //do nothing
    }

    public LearningExample(IVector inputVector) {
        this.inputVector = inputVector;
    }

    public LearningExample(IVector inputVector, IVector outputVector) {
        this.inputVector = inputVector;
        this.outputVector = outputVector;
    }

    public static LearningExample valueOf(String value) throws IOException{
        IVector inputVector, outputVector;
        String[] vectors = value.split(VECTOR_SEPARATOR);
		if (vectors.length != 2) {
			throw new IOException("In every row must be " + "exactly 2 vectors");
        }
        inputVector = BaseVector.valueOf(vectors[0]);
        outputVector = BaseVector.valueOf(vectors[1]);
        return new LearningExample(inputVector, outputVector);
    }

    public IVector getInput() {
        return inputVector;
    }

    public IVector getOutput() {
        return outputVector;
    }

    @Override
    public String toString() {
        return VectorUtils.toString(inputVector) + VECTOR_SEPARATOR
                + VectorUtils.toString(outputVector);
    }

    public int getInputDimension() {
        return getInput().getValues().size();
    }

    public int getOutputDimension() {
        return getOutput().getValues().size();
    }

    public static Comparator<LearningExample> inputComparator(final int... columns) {
        return new Comparator<LearningExample>() {

            public int compare(LearningExample o1, LearningExample o2) {
                return VectorUtils.comparator(columns).compare(o1.getInput(), o2.getInput());
            }
        };
    }
}
