package mallorcatour.grandtorino.nn.core;

import java.io.InputStream;

import mallorcatour.core.game.interfaces.IPokerStats;

import org.neuroph.core.NeuralNetwork;

public abstract class AbstractNeurals implements IPokerStats {
	private NeuralNetwork preflopNN;
	private NeuralNetwork flopNN;
	private NeuralNetwork turnNN;
	private NeuralNetwork riverNN;

	protected abstract String getSituationsPath();

	protected abstract String getPreflopPath();

	protected abstract String getFlopPath();

	protected abstract String getTurnPath();

	protected abstract String getRiverPath();

	public abstract String getName();

	protected AbstractNeurals() {
		init();
	}

	private void init() {
		this.preflopNN = NeuralNetwork.load(getPreflopStream());
		this.flopNN = NeuralNetwork.load(getFlopStream());
		this.turnNN = NeuralNetwork.load(getTurnStream());
		this.riverNN = NeuralNetwork.load(getRiverStream());
	}

	public NeuralNetwork getPreflopNN() {
		return this.preflopNN;
	}

	public NeuralNetwork getFlopNN() {
		return this.flopNN;
	}

	public NeuralNetwork getTurnNN() {
		return this.turnNN;
	}

	public NeuralNetwork getRiverNN() {
		return this.riverNN;
	}

	public InputStream getSituationsStream() {
		return getClass().getResourceAsStream(getSituationsPath());
	}

	private InputStream getPreflopStream() {
		return getClass().getResourceAsStream(getPreflopPath());
	}

	private InputStream getFlopStream() {
		return getClass().getResourceAsStream(getFlopPath());
	}

	private InputStream getTurnStream() {
		return getClass().getResourceAsStream(getTurnPath());
	}

	private InputStream getRiverStream() {
		return getClass().getResourceAsStream(getRiverPath());
	}
}
