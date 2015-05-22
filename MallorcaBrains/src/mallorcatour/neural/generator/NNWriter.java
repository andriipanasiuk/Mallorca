package mallorcatour.neural.generator;

import mallorcatour.util.MyFileWriter;

import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;

public class NNWriter {

	public static void weightsToJavaClass(MyFileWriter writer, NeuralNetwork<?> nn, String name) {
		Layer[] layers = nn.getLayers();
		writer.append("public class " + name + "NN {\n");
		writer.append("private static double[] layerSizeArray = new double[" + layers.length + "];\n");
		writer.append("static {\n");
		for (int i = 0; i < layers.length; i++) {
			Layer layer = layers[i];
			writer.append("layerSizeArray[" + i + "] = ");
			writer.append(String.valueOf(layer.getNeuronsCount()));
			writer.append(";\n");
		}
		writer.append("}\n");
		writer.append("private static double[] weights = new double[] {");
		Double[] weights = nn.getWeights();
		for (int i = 0; i < weights.length; i++) {
			double weight = weights[i];
			writer.append(String.valueOf(weight));
			if (i != weights.length - 1) {
				writer.append(", ");
			}
			if (i % 10 == 0) {
				writer.append("\n");
			}
		}
		writer.append("};\n");
		writer.append("}");
	}
}
