/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.neuronetworkwrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.core.vector.IVector;
import mallorcatour.core.vector.VectorInterpreter;
import mallorcatour.core.vector.VectorUtils;
import mallorcatour.interfaces.IDistanceCalculator;
import mallorcatour.interfaces.IInputInterpreter;
import mallorcatour.util.Log;
import mallorcatour.util.MyFileWriter;
import mallorcatour.util.ReaderUtils;

import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingElement;
import org.neuroph.core.learning.TrainingSet;

/**
 *
 * @author Andrew
 */
public class LEManager {

    private final static VectorInterpreter VECTOR_INTERPRETER =
            new VectorInterpreter(true);

    public static void toFile(String filename, IVector vector) {
        MyFileWriter fileWriter = MyFileWriter.prepareForWriting(filename, true);
        fileWriter.addToFile(VectorUtils.toString(vector), true);
        fileWriter.endWriting();
    }

    public static void toFile(String filename, IVector inputVector, IVector outputVector) {
        MyFileWriter fileWriter = MyFileWriter.prepareForWriting(filename, true);
        toFile(fileWriter, inputVector, outputVector);
        fileWriter.endWriting();
    }

    public static void toFile(MyFileWriter writer, IVector inputVector,
            IVector outputVector) {
        writer.addToFile(new LearningExample(inputVector, outputVector).toString(), true);
    }

    public static void toFile(MyFileWriter fileWriter, List<? extends LearningExample> examples) {
        for (LearningExample example : examples) {
            fileWriter.addToFile(example.toString(), true);
        }
    }

    public static void toFile(String path, List<LearningExample> examples) {
        MyFileWriter fileWriter = MyFileWriter.prepareForWriting(path, false);
        for (LearningExample example : examples) {
            fileWriter.addToFile(example.toString(), true);
        }
        fileWriter.endWriting();
    }

    public static List<LearningExample> readLearningExamples(String filename) {
        List<LearningExample> result = new ArrayList<LearningExample>();
        BufferedReader reader = ReaderUtils.initReader(filename);
        String buffer = ReaderUtils.readLine(reader);
        while (buffer != null) {
			if (!buffer.isEmpty()) {
				try {
					result.add(LearningExample.valueOf(buffer));
				} catch (IOException e) {
					Log.d("Invalid row in file with examples");
				}
			}
            buffer = ReaderUtils.readLine(reader);
        }
        return result;
    }

    /**
     *
     * @param examples
     * @param adaptForNeuroph defines if needed change all -1 to 0.
     * @return
     */
    public static TrainingSet<? extends TrainingElement> createTrainingSet(
            List<? extends LearningExample> examples) {
        if (examples.isEmpty()) {
            throw new IllegalArgumentException("List of examples must be not empty");
        }
        int input = examples.get(0).getInputDimension();
        int output = examples.get(0).getOutputDimension();

        TrainingSet<SupervisedTrainingElement> result =
                new TrainingSet<SupervisedTrainingElement>(input, output);
        for (LearningExample example : examples) {
            result.addElement(new SupervisedTrainingElementAdapter(example));
        }
        return result;
    }

    /**
     * Method creates TrainingSet from one learning example.
     * @param example
     * @param adaptForNeuroph
     * @return
     */
    public static TrainingSet<SupervisedTrainingElement> createTrainingSet(
            LearningExample example) {
        int input = example.getInputDimension();
        int output = example.getOutputDimension();

        TrainingSet<SupervisedTrainingElement> result =
                new TrainingSet<SupervisedTrainingElement>(input, output);
        result.addElement(new SupervisedTrainingElementAdapter(example));
        return result;
    }

    public static double[] createInputArray(LearningExample example) {
        return createInputArray(example.getInput());
    }

    public static double[] createInputArray(IVector vector) {
        return VECTOR_INTERPRETER.createInput(vector);
    }

    public static double[] createInputArray(IVector vector,
            IInputInterpreter<IVector> interpreter) {
        return interpreter.createInput(vector);
    }

    public static double[] getAverageOutput(List<? extends LearningExample> examples) {
        int dimension = examples.get(0).getOutput().getValues().size();
        double[] output = new double[dimension];
        for (LearningExample example : examples) {
            int i = 0;
            for (Number value : example.getOutput().getValues()) {
                output[i++] += value.doubleValue();
            }
        }
        for (int i = 0; i < dimension; i++) {
            output[i] /= examples.size();
        }
        return output;
    }

    public static List<PokerLearningExample> getSimilarSituations(
            List<PokerLearningExample> examples, LocalSituation similarTo,
            double maxDistance, IDistanceCalculator<LocalSituation> distance) {
        List<PokerLearningExample> result = new ArrayList<PokerLearningExample>();
        for (PokerLearningExample example : examples) {
            LocalSituation exampleSituation = example.getSituation();
            if (distance.getDistance(similarTo, exampleSituation) < maxDistance) {
                result.add(example);
            }
        }
        return result;
    }
}
