package mallorcatour.bot;

import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.situation.LocalSituation;

public interface IStudent {
	IStudent NONE = new IStudent() {
		@Override
		public void learn(LocalSituation situation, IAdvice advice) {
			//do nothing
		}
	};
	void learn(LocalSituation situation, IAdvice advice);
}