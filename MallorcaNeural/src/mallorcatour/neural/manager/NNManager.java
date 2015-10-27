/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.neural.manager;

import java.util.Arrays;
import java.util.List;

import mallorcatour.core.vector.IOutputInterpreter;
import mallorcatour.core.vector.IVector;
import mallorcatour.core.vector.VectorUtils;
import mallorcatour.neural.core.ILearningExample;
import mallorcatour.neural.core.LearningExample;
import mallorcatour.neural.core.VectorCreator;
import mallorcatour.tools.MyFileWriter;

import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.nnet.Kohonen;
import org.neuroph.nnet.MultiLayerPerceptron;

/**
 *
 * @author Andrew
 */
public class NNManager {

    public static <T extends IVector> double errorOnExamples(NeuralNetwork<?> neuralNetwork,
            List<? extends ILearningExample<?, ?>> examples, IOutputInterpreter<T> creator) {
        double error = 0;
        for (ILearningExample<?, ?> example : examples) {
            error += errorOnExample(neuralNetwork, example, creator);
        }
        error /= examples.size();
        return error;
    }

    public static double errorOnExamples(NeuralNetwork<?> neuralNetwork,
            List<? extends ILearningExample<?, ?>> examples) {
        return errorOnExamples(neuralNetwork, examples, new VectorCreator());
    }

    public static double errorOnExample(NeuralNetwork<?> neuralNetwork,
            LearningExample example) {
        return errorOnExample(neuralNetwork, example, new VectorCreator());
    }

    public static <T extends IVector> double errorOnExample(NeuralNetwork<?> neuralNetwork,
            ILearningExample<?, ?> example, IOutputInterpreter<T> creator) {
        neuralNetwork.setInput(LEManager.createInputArray(example));
        neuralNetwork.calculate();
        double[] realOutput = neuralNetwork.getOutput();
        IVector desiredVector = example.getOutput();

        if (realOutput.length != desiredVector.getValues().size()) {
            throw new IllegalArgumentException("Desired output dimension does not"
                    + "match to neural network output dimension.");
        }
        IVector nnVector = creator.create(realOutput);
        double error = VectorUtils.getDistance(nnVector, desiredVector);
        return error;
    }

    public static int[] createErrorSample(NeuralNetwork<?> nn,
            List<LearningExample> examples, double sectionSize, double maxError) {
        return createErrorSample(nn, examples, sectionSize, maxError, new VectorCreator());
    }

    public static <T extends IVector> int[] createErrorSample(NeuralNetwork<?> nn,
            List<LearningExample> examples, double sectionSize, double maxError,
            IOutputInterpreter<T> creator) {
        int sectionCount = (int) Math.round(maxError / sectionSize);
        int[] result = new int[sectionCount];
        for (LearningExample example : examples) {
            double error = errorOnExample(nn, example, creator);
            int index = (int) (error / sectionSize);
            if (index >= sectionCount) {
                result[sectionCount - 1]++;
            } else {
                result[index]++;
            }
        }
        return result;
    }

    public static int getCluster(Kohonen kohonenNN, IVector example) {
        kohonenNN.setInput(LEManager.createInputArray(example));
        kohonenNN.calculate();
        double[] output = kohonenNN.getOutput();
        double minWeight = Double.MAX_VALUE;
        int result = -1;
        for (int i = 0; i < output.length; i++) {
            if (output[i] < minWeight) {
                minWeight = output[i];
                result = i;
            }
        }
        return result;
    }

    public static double getDistance(Kohonen kohonenNN, IVector example,
            int clusterNumber, int digits) {
        kohonenNN.setInput(LEManager.createInputArray(example));
        kohonenNN.calculate();
        double[] output = kohonenNN.getOutput();
        double result = output[clusterNumber];
        int number = (int) Math.pow(10, digits);
        result = (double) (int) (result * number) / number;
        return result;
    }

    public static void weightsToFile(MultiLayerPerceptron perceptron, String path) {
        MyFileWriter writer = MyFileWriter.prepareForWriting(path, false);
        for (int i = 0; i < perceptron.getLayersCount() - 1; i++) {
            writer.addToFile((i + 1) + " layer", true);
            Layer current = perceptron.getLayerAt(i);
            int count = current.getNeuronsCount();
            for (int j = 0; j < count; j++) {
                int outputConnections = current.getNeuronAt(j).getOutConnections().length;
                double[] weights = new double[outputConnections];
                for (int k = 0; k < outputConnections; k++) {
                    weights[k] = current.getNeuronAt(j).getOutConnections()[k].getWeight().getValue();
                }
                writer.addToFile(Arrays.toString(weights), true);
            }
        }
        writer.endWriting();
    }
}
