package mallorcatour.core.game.interfaces;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.PokerStreet;

/**
 * Интерфейс наблюдателя за иргой с точки зрения конкретного игрока.
 */
public interface IPlayerGameObserver extends IObserver {
    void onHandStarted(IPlayerGameInfo gameInfo);

    IPlayerGameObserver EMPTY = new IPlayerGameObserver() {
        @Override
        public void onHandStarted(IPlayerGameInfo gameInfo) {

        }

        @Override
        public void onStageEvent(PokerStreet street) {

        }

        @Override
        public void onActed(Action action, double toCall, String name) {

        }

        @Override
        public void onHandEnded() {

        }
    };
}
