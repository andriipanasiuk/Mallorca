package mallorcatour.presenter;

import mallorcatour.bot.C;
import mallorcatour.bot.ObservingPlayer;
import mallorcatour.bot.math.NLMathBotFactory;
import mallorcatour.bot.random.RandomBot;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.AdvisorListener;
import mallorcatour.core.game.engine.GameEngine;
import mallorcatour.core.game.interfaces.GameContext;
import mallorcatour.core.game.interfaces.IGameObserver;
import mallorcatour.core.player.interfaces.Player;
import mallorcatour.equilator.PokerEquilatorBrecher;
import mallorcatour.presenter.GameViewContract.Presenter;
import mallorcatour.presenter.GameViewContract.View;
import mallorcatour.tools.DateUtils;
import mallorcatour.tools.Log;

public class GameViewPresenter implements Presenter, IGameObserver {

    private View view;

    private String DEBUG_PATH;
    private GameEngine engine;
    private Action lastMove;
    private Player playerUp;
    private Player playerDown;

    private GameContext gameInfo;

    @Override
    public void start() {
        Log.WRITE_TO_CONSOLE = true;
        DEBUG_PATH = C.Preferences.LOG_PATH + DateUtils.getDate(false) + ".txt";
        NLMathBotFactory factory = new NLMathBotFactory(new RandomBot("random", DEBUG_PATH));
        playerUp = factory.createBot(AdvisorListener.NONE,
                AdvisorListener.NONE, "Grantorino Up", DEBUG_PATH);
        playerDown = new HumanPlayer("Andrew", DEBUG_PATH);
        engine = new GameEngine(playerDown, playerUp, this, new PokerEquilatorBrecher(), DEBUG_PATH);
    }

    @Override
    public void stop() {
        this.view = null;
    }

    @Override
    public void attachView(View view) {
        this.view = view;
    }

    @Override
    public void foldButtonPressed() {
        lastMove = Action.fold();
    }

    @Override
    public void passiveButtonPressed() {
        if (gameInfo.getAmountToCall(playerDown.getName()) > 0) {
            lastMove = Action.callAction(gameInfo.getAmountToCall(playerDown.getName()));
        } else {
            lastMove = Action.checkAction();
        }
    }

    @Override
    public void aggressiveButtonPressed() {
        lastMove = Action.createRaiseAction(gameInfo.getAmountToCall("Andrew"), gameInfo.getPotSize(),
                gameInfo.getBankRollAtRisk());
    }

    @Override
    public void dealButtonPressed() {
        engine.playGame();
        Log.f(DEBUG_PATH, "==========  New tournament  ==========");
    }

    @Override
    public void onHandStarted(GameContext gameInfo) {
        this.gameInfo = gameInfo;
        view.startHand(gameInfo, playerUp, playerDown);
        view.updateUI(gameInfo, playerUp, playerDown);
    }

    @Override
    public void onStageEvent(PokerStreet street) {
        view.updateUI(gameInfo, playerUp, playerDown);
    }

    @Override
    public void onActed(Action action, double toCall, String name) {
        view.updateUI(gameInfo, playerUp, playerDown);
        int index;
        if (name.equals(playerUp.getName())) {
            index = 0;
        } else {
            index = 1;
        }
        view.onActed(action, toCall, name, index);
    }

    @Override
    public void onHandEnded() {
        view.finishHand();
        view.updateUI(gameInfo, playerUp, playerDown);
    }

    public class HumanPlayer extends ObservingPlayer implements Player {

        public HumanPlayer(String name, String debug) {
            super(name, debug);
        }

        @Override
        public void onHoleCards(Card c1, Card c2) {
            super.onHoleCards(c1, c2);
            view.onHoleCards(c1, c2);
        }

        @Override
        public Action getAction() {
            view.beforeHumanAction(gameInfo);
            return lastMove;
        }

        @Override
        public String getName() {
            return name;
        }

    }
}
