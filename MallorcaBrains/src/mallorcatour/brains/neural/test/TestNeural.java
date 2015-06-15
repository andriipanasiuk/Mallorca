package mallorcatour.brains.neural.test;

import java.util.Arrays;
import java.util.Random;

import mallorcatour.brains.neural.gusxensen.FlopNeuralInfo;
import mallorcatour.brains.neural.gusxensen.RiverNeuralInfo;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.advice.SmartPostflopAdviceCreator;
import mallorcatour.core.vector.BaseVector;
import mallorcatour.core.vector.VectorInterpreter;
import mallorcatour.neural.core.NeuralCreator;
import mallorcatour.tools.Log;

import org.neuroph.core.NeuralNetwork;

public class TestNeural {
	public static void equalFromCodeFile() {
		NeuralNetwork<?> networkFromFile = NeuralNetwork.createFromFile("GusXensenNeurals/river.mlp");
		NeuralNetwork<?> networkFromCode = NeuralCreator.createPerceptron(new RiverNeuralInfo());
		Log.d("From file info: " + networkFromFile.getLayersCount());
		Log.d("From code info: " + networkFromCode.getLayersCount());
		Log.d("From file info: " + Arrays.toString(networkFromFile.getWeights()));
		Log.d("From code info: " + Arrays.toString(networkFromCode.getWeights()));
		double[] array = new double[12];
		Random random = new Random(System.currentTimeMillis());
		for (int i = 0; i < 10000; i++) {
			for (int j = 0; j < array.length; j++) {
				array[j] = (double) random.nextInt(100) / 100;
			}
			array[1] = 1;
			networkFromFile.setInput(array);
			networkFromCode.setInput(array);
			networkFromFile.calculate();
			networkFromCode.calculate();
			double[] output1 = networkFromFile.getOutput();
			double[] output2 = networkFromCode.getOutput();
			if (!Arrays.equals(output1, output2)) {
				throw new RuntimeException(i + "");
			}
		}
	}

	public static void testInput(String input){
		NeuralNetwork<?> networkFromCode = NeuralCreator.createPerceptron(new FlopNeuralInfo());
		BaseVector vector = BaseVector.valueOf(input);
		networkFromCode.setInput(new VectorInterpreter(true).createInput(vector));
		networkFromCode.calculate();
		Log.d(Advice.create(new SmartPostflopAdviceCreator(), true, networkFromCode.getOutput()).toString());
	}

	public static void main(String... args) {
		testInput("0.826, 1, 1, 2.0, 2.0, 2.0, 0, 1, 0.872, 0.25, 0.168, 0.089");
	}
}
