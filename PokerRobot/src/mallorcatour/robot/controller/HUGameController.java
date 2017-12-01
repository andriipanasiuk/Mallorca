/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.controller;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import mallorcatour.core.player.interfaces.Player;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.robot.ExtPlayerInfo;
import mallorcatour.robot.hardwaremanager.KeyboardHookManager;
import mallorcatour.robot.interfaces.IGameController;
import mallorcatour.tools.FileUtils;
import mallorcatour.tools.Log;
import mp3player.Mp3Player;
import br.com.wagnerpaz.javahook.NativeKeyboardEvent;
import br.com.wagnerpaz.javahook.NativeKeyboardListener;

/**
 *
 * @author Andrew
 */
public class HUGameController implements IGameController {

    private static final String ALARM_WAV_PATH = "assets/sound/app-31.wav";
    private PokerStreet currentStreet;
    private HUGameInfo gameInfo;
    protected final Player player;
    //values must be reseted after new hand
    private List<Card> alreadyTakenCards;
    private Action heroPreviousAction, villainPreviousAction;
    @SuppressWarnings("unused")
	private long currentHandNumber = -1;
    private final String DEBUG_PATH;
    private final String heroName;

    public HUGameController(Player player, String heroName, String DEBUG_PATH) {
        this.player = player;
        this.DEBUG_PATH = DEBUG_PATH;
        this.heroName = heroName;
    }

    private ExtPlayerInfo getHero(List<ExtPlayerInfo> players) {
        for (ExtPlayerInfo playerInfo : players) {
            if (playerInfo.getName().equals(heroName)) {
                return playerInfo;
            }
        }
        throw new RuntimeException();
    }

    private ExtPlayerInfo getVillain(List<ExtPlayerInfo> players) {
        for (ExtPlayerInfo playerInfo : players) {
            if (!playerInfo.getName().equals(heroName)) {
                return playerInfo;
            }
        }
        throw new RuntimeException();
    }

    private double getBigBlind(List<ExtPlayerInfo> players) {
        for (ExtPlayerInfo playerInfo : players) {
            if (!playerInfo.isOnButton()) {
                return playerInfo.getBet();
            }
        }
        throw new RuntimeException();
    }

    private void processBoardCards(List<Card> boardCards, double pot) {
        if (boardCards.size() != alreadyTakenCards.size()) {
            for (Card card : boardCards) {
                if (!alreadyTakenCards.contains(card)) {
                    alreadyTakenCards.add(card);
                }
            }
            gameInfo.board = alreadyTakenCards;
            if (boardCards.size() == 3) {
                Log.f(DEBUG_PATH, "\nFLOP: " + boardCards + " Pot: " + pot + FileUtils.LINE_SEPARATOR);
                player.onStageEvent(PokerStreet.FLOP);
            } else if (boardCards.size() == 4) {
                Log.f(DEBUG_PATH, "\nTURN: " + boardCards + " Pot: " + pot + FileUtils.LINE_SEPARATOR);
                player.onStageEvent(PokerStreet.TURN);
            } else if (boardCards.size() == 5) {
                Log.f(DEBUG_PATH, "\nRIVER: " + boardCards + " Pot: " + pot + FileUtils.LINE_SEPARATOR);
                player.onStageEvent(PokerStreet.RIVER);
            }
        }
    }

    private PokerStreet calculateStreet(List<Card> boardCards) {
        int size = boardCards.size();
        switch (size) {
            case 0:
                return PokerStreet.PREFLOP;
            case 3:
                return PokerStreet.FLOP;
            case 4:
                return PokerStreet.TURN;
            case 5:
                return PokerStreet.RIVER;
            default:
                throw new RuntimeException("Illegal count of board cards: " + size);
        }
    }

    private double getEffectiveStack(List<ExtPlayerInfo> players) {
        double result = Double.POSITIVE_INFINITY;
        double playerAllStack;
        for (ExtPlayerInfo playerInfo : players) {
            playerAllStack = playerInfo.getBet() + playerInfo.getStack();
            if (playerAllStack < result) {
                result = playerAllStack;
            }
        }
        return result;
    }

	@Override
    public Action onMyAction(List<Card> boardCards, double pot, boolean villainSitOut) {
        sendVillainActions(boardCards, pot);
        Action heroAction;
        heroAction = player.getAction();
        if (villainSitOut) {
            double percent = 0.5;
            heroAction = Action.createRaiseAction(percent
                    * (gameInfo.getPotSize() + gameInfo.getAmountToCall()), percent);
        }
        //calculating changing of pot
        if (villainPreviousAction != null) {
            if (villainPreviousAction.isAggressive()) {
                if (heroAction.isAggressive()) {
                    gameInfo.pot += villainPreviousAction.getAmount();
                    double heroRaiseAmount = heroAction.getAmount();
                    if (heroRaiseAmount > gameInfo.bankrollAtRisk) {
                        heroRaiseAmount = gameInfo.bankrollAtRisk;
                    }
                    gameInfo.pot += heroRaiseAmount;
                    gameInfo.bankrollAtRisk -= heroRaiseAmount;
                    gameInfo.raisesOnStreet[currentStreet.intValue()]++;
                } else if (heroAction.isPassive()) {
                    gameInfo.pot += villainPreviousAction.getAmount();
                }
            } else if (villainPreviousAction.isPassive()) {
                if (heroAction.isAggressive()) {
                    double heroRaiseAmount = heroAction.getAmount();
                    if (heroRaiseAmount > gameInfo.bankrollAtRisk) {
                        heroRaiseAmount = gameInfo.bankrollAtRisk;
                    }
                    gameInfo.pot += heroRaiseAmount;
                    gameInfo.bankrollAtRisk -= heroRaiseAmount;
                    gameInfo.raisesOnStreet[currentStreet.intValue()]++;
                }
            }
        } else {
            if (heroAction.isAggressive()) {
                gameInfo.pot += gameInfo.bigBlind / 2;
                double heroRaiseAmount = heroAction.getAmount();
                if (heroRaiseAmount > gameInfo.bankrollAtRisk) {
                    heroRaiseAmount = gameInfo.bankrollAtRisk;
                }
                gameInfo.pot += heroRaiseAmount;
                gameInfo.bankrollAtRisk -= heroRaiseAmount;
                gameInfo.raisesOnStreet[currentStreet.intValue()]++;
            } else {
                gameInfo.pot += gameInfo.bigBlind / 2;
            }
        }
        heroPreviousAction = heroAction;
        return heroAction;
    }

    private void sendVillainActions(List<Card> boardCards, double pot) {
        double villainBet = gameInfo.villainInfo.getBet();
        double heroBet = gameInfo.heroInfo.getBet();
        boolean heroOnButton = gameInfo.heroInfo.isOnButton();
        PokerStreet newStreet = calculateStreet(boardCards);
        boolean streetChanged = false;
        //sending villain actions from previous street. If they are.
        if (newStreet != currentStreet) {
            if (heroPreviousAction == null) {
                System.err.println("Error has occured! HeroPreviousAction = null");
                System.err.println("Board cards: " + boardCards);
                System.err.println("Villain bet: " + villainBet);
                System.err.println("Hero bet: " + heroBet);
                System.err.println("Street: " + newStreet);
                System.err.println("Hero on button: " + heroOnButton);
                final mp3player.Mp3Player mp3Player = new Mp3Player(ALARM_WAV_PATH);
                mp3Player.play(true);
                KeyboardHookManager.addListener(new NativeKeyboardListener() {

                    public boolean keyPressed(NativeKeyboardEvent nke) {
                        if (nke.getKeyCode() == KeyEvent.VK_F2) {
                            mp3Player.stop();
                            return false;
                        }
                        return true;
                    }

                    public boolean keyReleased(NativeKeyboardEvent nke) {
                        if (nke.getKeyCode() == KeyEvent.VK_F2) {
                            return false;
                        }
                        return true;
                    }
                });
            }
            if (heroPreviousAction != null && heroPreviousAction.isAggressive()) {
                Log.f(DEBUG_PATH, gameInfo.villainInfo.getName()
                        + " calls " + heroPreviousAction.getAmount());
				villainPreviousAction = Action.callAction(heroPreviousAction.getAmount());
				player.onActed(villainPreviousAction, heroPreviousAction.getAmount(), gameInfo.villainInfo.getName());
				gameInfo.pot += heroPreviousAction.getAmount();
            } else if (currentStreet == PokerStreet.PREFLOP
                    && isMinPotOnPreflop(pot) && heroOnButton) {
                Log.f(DEBUG_PATH, gameInfo.villainInfo.getName()
                        + " checks");
                villainPreviousAction = Action.checkAction();
                player.onActed(villainPreviousAction, 0, gameInfo.villainInfo.getName());
            } else if (!heroOnButton && currentStreet != PokerStreet.PREFLOP
                    && heroPreviousAction.isCheck()) {
                Log.f(DEBUG_PATH, gameInfo.villainInfo.getName()
                        + " checks");
                villainPreviousAction = Action.checkAction();
                player.onActed(villainPreviousAction, 0,gameInfo.villainInfo.getName());
            }
            streetChanged = true;
            currentStreet = newStreet;
        }
        gameInfo.changeStreet(newStreet);

        processBoardCards(boardCards, gameInfo.pot);
        //sending current villain actions
		if (villainBet > heroBet && (!gameInfo.isPreFlop() || villainBet != gameInfo.bigBlind)) {
            Log.f(DEBUG_PATH, gameInfo.villainInfo.getName()
                    + (heroBet == 0 ? " bets " : " raises ") + (villainBet - heroBet));
            villainPreviousAction = Action.raiseAction(villainBet - heroBet);
            double villainToCall = 0;
            if (heroPreviousAction != null) {
                if (!streetChanged) {
                    if (heroPreviousAction.isAggressive()) {
                        villainToCall = heroPreviousAction.getAmount();
                    }
                } else {
                    villainToCall = 0;
                }
            } else {
                villainToCall = gameInfo.bigBlind / 2;
            }
            player.onActed(villainPreviousAction, villainToCall, gameInfo.villainInfo.getName());
            gameInfo.pot += villainToCall;
            gameInfo.raisesOnStreet[newStreet.intValue()]++;
            double villainRealBet = villainBet - heroBet;
            if (villainRealBet > gameInfo.bankrollAtRisk) {
                villainRealBet = gameInfo.bankrollAtRisk;
            }
            gameInfo.pot += villainRealBet;
            gameInfo.bankrollAtRisk -= villainRealBet;
        } else if (villainBet == heroBet) {
            if (!heroOnButton && gameInfo.isPreFlop()) {
                Log.f(DEBUG_PATH, gameInfo.villainInfo.getName()
                        + " calls " + gameInfo.bigBlind / 2);
                villainPreviousAction = Action.callAction(gameInfo.bigBlind / 2);
                player.onActed(villainPreviousAction, gameInfo.bigBlind / 2, gameInfo.villainInfo.getName());
                gameInfo.pot += gameInfo.bigBlind / 2;
            }
            if (heroOnButton && !gameInfo.isPreFlop()) {
                Log.f(DEBUG_PATH, gameInfo.villainInfo.getName()
                        + " checks");
                villainPreviousAction = Action.checkAction();
                player.onActed(villainPreviousAction, 0, gameInfo.villainInfo.getName());
            }
        }
    }

    private boolean isMinPotOnPreflop(double pot) {
        return Math.abs(pot - gameInfo.villainInfo.getBet() - gameInfo.bigBlind * 2)
                < 0.1 * gameInfo.bigBlind * 2;
    }

	@Override
	public void onNewHand(long handNumber, List<ExtPlayerInfo> players, Card holeCard1, Card holeCard2, List<Card> board,
			double pot, LimitType limitType) {
		villainPreviousAction = null;
		currentStreet = calculateStreet(board);
        alreadyTakenCards = new ArrayList<Card>();
        gameInfo = new HUGameInfo();
        player.onHandStarted(gameInfo);
        //player infos
        gameInfo.heroInfo = getHero(players);
        gameInfo.villainInfo = getVillain(players);

        currentHandNumber = handNumber;
        //game stage
        gameInfo.changeStreet(currentStreet);
        //big blind, pot, limit and effective stack
        gameInfo.bigBlind = getBigBlind(players);
        if (currentStreet == PokerStreet.PREFLOP) {
            heroPreviousAction = null;
            gameInfo.pot = gameInfo.bigBlind * 1.5;
        } else {
            heroPreviousAction = Action.checkAction();
            gameInfo.pot = pot;
        }
        double effectiveStack = getEffectiveStack(players);
        gameInfo.bankrollAtRisk = effectiveStack - gameInfo.bigBlind;
        gameInfo.limitType = limitType;
        gameInfo.raisesOnStreet[PokerStreet.PREFLOP.intValue()] = 1;
        //logging
        Log.f(DEBUG_PATH, "\n<--------------------->");
        Log.f(DEBUG_PATH, "Hand number: " + handNumber + FileUtils.LINE_SEPARATOR);
        String smallBlindLog = "", bigBlindLog = "";
        for (ExtPlayerInfo playerInfo : players) {
            String log;
            if (playerInfo.isOnButton()) {
                log = playerInfo.getName() + "* " + playerInfo.getStack()
                        + (playerInfo == gameInfo.heroInfo ? " " + holeCard1 + " " + holeCard2 : "");
                smallBlindLog = playerInfo.getName() + " posts small blind " + gameInfo.bigBlind / 2;
            } else {
                log = playerInfo.getName() + " " + playerInfo.getStack()
                        + (playerInfo == gameInfo.heroInfo ? " " + holeCard1 + " " + holeCard2 : "");
                bigBlindLog = playerInfo.getName() + " posts big blind " + gameInfo.bigBlind;
            }
            Log.f(DEBUG_PATH, log);
        }
        Log.f(DEBUG_PATH, "");
        Log.f(DEBUG_PATH, smallBlindLog);
        Log.f(DEBUG_PATH, bigBlindLog);

        player.onHoleCards(holeCard1, holeCard2);
        if (currentStreet == PokerStreet.PREFLOP) {
            player.onStageEvent(currentStreet);
        }
    }
}
