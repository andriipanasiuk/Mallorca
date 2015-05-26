package mallorcatour.bot.interfaces;

import mallorcatour.core.game.Card;
import mallorcatour.core.spectrum.Spectrum;

public interface ISpectrumHolder {
	public static final ISpectrumHolder DEFAULT = new ISpectrumHolder() {

		@Override
		public Spectrum getSpectrum() {
			return Spectrum.random();
		}

		@Override
		public void onCardsKnown(Card... cards) {
			// do nothing
		}
	};

	void onCardsKnown(Card... cards);

	Spectrum getSpectrum();
}