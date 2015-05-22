package mallorcatour.brains.neural;

import java.io.InputStream;

import mallorcatour.core.game.interfaces.IPokerStats;

import org.neuroph.core.NeuralNetwork;

public abstract class AbstractNeurals implements ISituationData, IPokerStats, IPokerNeurals {
	private NeuralNetwork<?> preflopNN;
	private NeuralNetwork<?> flopNN;
	private NeuralNetwork<?> turnNN;
	private NeuralNetwork<?> riverNN;

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

	@Override
	public NeuralNetwork<?> getPreflop() {
		return this.preflopNN;
	}

	@Override
	public NeuralNetwork<?> getFlop() {
		return this.flopNN;
	}

	@Override
	public NeuralNetwork<?> getTurn() {
		return this.turnNN;
	}

	@Override
	public NeuralNetwork<?> getRiver() {
		return this.riverNN;
	}

	@Override
	public InputStream getSituationStream() {
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
