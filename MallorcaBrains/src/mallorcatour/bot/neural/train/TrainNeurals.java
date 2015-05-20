package mallorcatour.bot.neural.train;

import mallorcatour.grandtorino.nn.core.AbstractNeurals;

public class TrainNeurals extends AbstractNeurals{

	@Override
	public double getAggressionFactor() {
		return 3.0;
	}

	@Override
	public double getWtsd() {
		return 0.4;
	}

	@Override
	public double getAggressionFrequency() {
		return 0.5;
	}

	@Override
	public double getFoldFrequency() {
		return 0.2;
	}

	@Override
	protected String getSituationsPath() {
		return null;
	}

	@Override
	protected String getPreflopPath() {
		return null;
	}

	@Override
	protected String getFlopPath() {
		return null;
	}

	@Override
	protected String getTurnPath() {
		return null;
	}

	@Override
	protected String getRiverPath() {
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
