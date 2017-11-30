package mallorcatour.core.game.interfaces;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.PokerStreet;

public interface IObserver {

    /**
     * A new betting round has started. Not called on preflop.
     */
    void onStageEvent(PokerStreet street);

    /**
     * A new game has been started.
     *
     */
    void onActed(Action action, double toCall, String name);

    void onHandEnded();
}
