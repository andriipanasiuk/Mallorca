package mallorcatour.core.game.interfaces;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.PokerStreet;

/**
 * Интерфейс наблюдателя за иргой с точки зрения конкретного игрока.
 */
public interface IPlayerGameObserver extends IGameObserver {

    IPlayerGameObserver EMPTY = new IPlayerGameObserver() {
        @Override
        public void onHandStarted(GameContext gameInfo) {

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
