package mallorcatour.bot.interfaces;

import mallorcatour.core.spectrum.Spectrum;

public interface ISpectrumHolder {
	public static final ISpectrumHolder DEFAULT = new ISpectrumHolder() {

		@Override
		public Spectrum getSpectrum() {
			return Spectrum.random();
		}

	};

	Spectrum getSpectrum();
}