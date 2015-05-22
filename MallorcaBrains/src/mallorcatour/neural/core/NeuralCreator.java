package mallorcatour.neural.core;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.nnet.MultiLayerPerceptron;

public class NeuralCreator {

	public static NeuralNetwork<?> createPerceptron(INeuralInfo info) {
		int[] layerSizes = info.getLayerSizes();
		for (int i = 0; i < layerSizes.length - 1; i++) {
			layerSizes[i] -= 1;
		}
		MultiLayerPerceptron result = new MultiLayerPerceptron(layerSizes);
		result.setWeights(info.getWeights());
		return result;
	}

}
