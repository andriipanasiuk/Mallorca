/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.pokeracademyport;

import com.biotools.meerkat.Action;
import com.biotools.meerkat.Card;
import com.biotools.meerkat.GameInfo;
import com.biotools.meerkat.Player;
import com.biotools.meerkat.util.Preferences;

import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.bot.modeller.VillainModel;
import mallorcatour.util.DateUtils;
import mallorcatour.util.Log;

/**
 *
 * @author Andrew
 */
public abstract class AbstractPABot implements Player {

    private PAVillainObserver villainObserver;
    private double toCall = 0;
    private HUGameInfo gi;
    private GameInfo paGameInfo;
    private int heroSeat;
    private String villainName;
    private String DEBUG_PATH = "default.txt";
    private VillainModel modeller;

    public AbstractPABot(String name) {
        DEBUG_PATH = name + "_debug_" + DateUtils.getDate(false) + ".txt";
        modeller = new VillainModel(LimitType.FIXED_LIMIT, DEBUG_PATH);
        villainObserver = new PAVillainObserver(modeller, DEBUG_PATH);
        Log.f(DEBUG_PATH, "=============================");
        Log.f(DEBUG_PATH, "Grandtorino player inited");
        Log.f(DEBUG_PATH, "Time: " + DateUtils.getDate(false));
        Log.f(DEBUG_PATH, "=============================");
    }

    protected VillainModel getVillainModeller() {
        return modeller;
    }

    protected IDecisionListener getDecisionListener() {
        return villainObserver;
    }

    protected String getDebugPath() {
        return DEBUG_PATH;
    }

    protected abstract IPlayer getRealBot();

    public void init(Preferences p) {
    }

    public void holeCards(Card card1, Card card2, int i) {
        this.heroSeat = i;
        gi.setHeroSeat(i);
        int seat = 0;
        while (true) {
            if (paGameInfo.inGame(seat) && seat != i) {
                break;
            }
            seat++;
        }
        this.villainName = paGameInfo.getPlayerName(seat);
        Log.f(DEBUG_PATH, "Count of raises: " + paGameInfo.getNumRaises());
        Log.f(DEBUG_PATH, "Hero seat: " + heroSeat);
        Log.f(DEBUG_PATH, "Villain seat: " + seat);
        Log.f(DEBUG_PATH, "Villain name: " + villainName);
        villainObserver.setVillainName(villainName);
        Log.f(DEBUG_PATH, "*** HOLE CARDS ****   " + card1 + " " + card2);
        getRealBot().onHandStarted(gi);
        getRealBot().onHoleCards(CardAdapter.createFromPACard(card1),
                CardAdapter.createFromPACard(card2),
                villainName);
    }

    public Action getAction() {
        Log.f(DEBUG_PATH, "Count of raises: " + paGameInfo.getNumRaises());
        mallorcatour.core.game.Action result = getRealBot().getAction();
        Log.f(DEBUG_PATH, "grandtorino acted: " + result);
        return ActionAdapter.createPAAction(result, toCall);
    }

    public void actionEvent(int i, Action action) {
        if (i != heroSeat) {
            toCall = action.getAmount();
            Log.f(DEBUG_PATH, villainName + " acted " + action);
            if (action.isBetOrRaise() || action.isCheckOrCall() || action.isFold()) {
                getRealBot().onVillainActed(ActionAdapter.createFromPAAction(action), action.getToCall());
            }
        }
    }

    public void stageEvent(int i) {
        toCall = 0;
        Log.f(DEBUG_PATH, "\nSTAGE EVENT: " + PokerStreet.valueOf(i));
        Log.f(DEBUG_PATH, "BOARD: " + gi.getBoard() + " POT: " + paGameInfo.getMainPotSize() + "\n");
        getRealBot().onStageEvent(PokerStreet.valueOf(i));
    }

    public void showdownEvent(int i, Card card1, Card card2) {
        if (i != heroSeat) {
            Log.f(DEBUG_PATH, "*** SHOWDOWN ***");
            Log.f(DEBUG_PATH, "Villain has " + card1 + " " + card2);
        }
    }

    public void gameStartEvent(GameInfo gi) {
        this.gi = new HUGameInfo(gi);
        this.paGameInfo = gi;
        Log.f(DEBUG_PATH, "================");
        Log.f(DEBUG_PATH, "GAME STARTED");
    }

    public void dealHoleCardsEvent() {
    }

    public void gameOverEvent() {
        Log.f(DEBUG_PATH, "GAME OVER");
        Log.f(DEBUG_PATH, "================\n");
        villainObserver.onHandPlayed(-1);
    }

    public void winEvent(int i, double d, String string) {
    }

    public void gameStateChanged() {
    }
}
