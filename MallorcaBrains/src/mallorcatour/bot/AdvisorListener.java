package mallorcatour.bot;

import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.situation.LocalSituation;

/**
 * Interface for listening advice in every situation.
 * Something like AdviceListener. Consider rename this interface.
 * @author andriipanasiuk
 *
 */
public interface AdvisorListener {
	AdvisorListener NONE = new AdvisorListener() {
		@Override
		public void onAdvice(LocalSituation situation, IAdvice advice) {
			//do nothing
		}
	};
	void onAdvice(LocalSituation situation, IAdvice advice);
}