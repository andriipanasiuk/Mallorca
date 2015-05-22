package mallorcatour.bot.interfaces;

import mallorcatour.core.spectrum.Spectrum;

public interface IVillainSpectrumHandler {
	public static final IVillainSpectrumHandler DEFAULT = new IVillainSpectrumHandler() {

		@Override
		public Spectrum getVillainSpectrum() {
			return Spectrum.random();
		}
	};

	Spectrum getVillainSpectrum();
}