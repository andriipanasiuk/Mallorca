package mallorcatour.presenter;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.interfaces.GameContext;
import mallorcatour.core.player.interfaces.Player;

public interface GameViewContract {
    interface View {

        /**
         * Обновляет информацию о ставках, о стэке игроков,
         * о текущем размере банка, а также карты на столе.
         */
        void updateUI(GameContext gameInfo, Player playerUp, Player playerDown);

        void finishHand();

        void onActed(Action action, double toCall, String name, int index);

        void startHand(GameContext gameInfo, Player playerUp, Player playerDown);

        void onHoleCards(Card c1, Card c2);

        void beforeHumanAction(GameContext gameInfo);
    }

    interface Presenter {

        void start();
        void stop();

        void attachView(View view);

        void aggressiveButtonPressed();

        void dealButtonPressed();

        void passiveButtonPressed();

        void foldButtonPressed();
    }
}
