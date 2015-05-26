package mallorcatour.core.game.interfaces;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.situation.LocalSituation;


public interface IHeroObserver {
	public void onHeroActed(LocalSituation situation, Action action);
}