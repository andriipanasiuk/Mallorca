package mallorcatour.neural.generator;

import mallorcatour.tools.MyFileWriter;

import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;

public class NNWriter {

	public static void weightsToJavaClass(MyFileWriter writer, NeuralNetwork<?> nn, String name) {
		Layer[] layers = nn.getLayers();
		writer.append("public class " + name + "NeuralInfo implements INeuralInfo {\n");
		writer.append("private int[] layerSizeArray = new int[] ");
		writer.append("{");
		for (int i = 0; i < layers.length; i++) {
			Layer layer = layers[i];
			writer.append(String.valueOf(layer.getNeuronsCount()));
			if(i!= layers.length -1){
				writer.append(", ");
			}
		}
		writer.append("};\n");
		writer.append("private double[] weights = new double[] {");
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

		writer.append("public int[] getLayerSizes() {\n");
		writer.append("return layerSizeArray;\n");
		writer.append("}\n");

		writer.append("public double[] getWeights() {\n");
		writer.append("return weights;\n");
		writer.append("}\n");
		writer.append("}\n");
	}
}
