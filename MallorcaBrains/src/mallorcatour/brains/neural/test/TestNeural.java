package mallorcatour.brains.neural.test;

import java.util.Arrays;
import java.util.Random;

import mallorcatour.brains.neural.gusxensen.RiverNeuralInfo;
import mallorcatour.neural.core.NeuralCreator;
import mallorcatour.util.Log;

import org.neuroph.core.NeuralNetwork;

public class TestNeural {

	public static void main(String... args) {
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
			array[1] =1;
			networkFromFile.setInput(array);
			networkFromCode.setInput(array);
			networkFromFile.calculate();
			networkFromCode.calculate();
			double[] output1 = networkFromFile.getOutput();
			double[] output2 = networkFromCode.getOutput();
			if (!Arrays.equals(output1, output2)) {
				throw new RuntimeException(i+"");
			}
		}
	}
}
