/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.grandtorino.controller;

import br.com.wagnerpaz.javahook.NativeKeyboardEvent;
import br.com.wagnerpaz.javahook.NativeKeyboardListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import mallorcatour.game.core.Card;
import mallorcatour.game.core.Action;
import mallorcatour.game.core.PokerStreet;
import mallorcatour.core.bot.IGameController;
import mallorcatour.core.bot.IPlayer;
import mallorcatour.core.bot.LimitType;
import mallorcatour.grandtorino.robot.PlayerInfo;
import mallorcatour.hookmanager.KeyboardHookManager;
import mallorcatour.util.Log;
import mp3player.Mp3Player;

/**
 *
 * @author Andrew
 */
public class HUGameController implements IGameController {

    private static final String ALARM_WAV_PATH = "assets/sound/app-31.wav";
    private PokerStreet currentStreet;
    private HUGameInfo gameInfo;
    protected final IPlayer player;
    //values must be reseted after new hand
    private List<Card> alreadyTakenCards;
    private Action heroPreviousAction, villainPreviousAction;
    private long currentHandNumber = -1;
    private final String DEBUG_PATH;
    private final String heroName;

    public HUGameController(IPlayer player, String heroName, String DEBUG_PATH) {
        this.player = player;
        this.DEBUG_PATH = DEBUG_PATH;
        this.heroName = heroName;
    }

    private PlayerInfo getHero(List<PlayerInfo> players) {
        for (PlayerInfo playerInfo : players) {
            if (playerInfo.getName().equals(heroName)) {
                return playerInfo;
            }
        }
        throw new RuntimeException();
    }

    private PlayerInfo getVillain(List<PlayerInfo> players) {
        for (PlayerInfo playerInfo : players) {
            if (!playerInfo.getName().equals(heroName)) {
                return playerInfo;
            }
        }
        throw new RuntimeException();
    }

    private double getBigBlind(List<PlayerInfo> players) {
        for (PlayerInfo playerInfo : players) {
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
            gameInfo.boardCards = alreadyTakenCards;
            if (boardCards.size() == 3) {
                Log.f(DEBUG_PATH, "\nFLOP: " + boardCards + " Pot: " + pot + "\n");
                player.onStageEvent(PokerStreet.FLOP);
            } else if (boardCards.size() == 4) {
                Log.f(DEBUG_PATH, "\nTURN: " + boardCards + " Pot: " + pot + "\n");
                player.onStageEvent(PokerStreet.TURN);
            } else if (boardCards.size() == 5) {
                Log.f(DEBUG_PATH, "\nRIVER: " + boardCards + " Pot: " + pot + "\n");
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

    private double getEffectiveStack(List<PlayerInfo> players) {
        double result = Double.POSITIVE_INFINITY;
        double playerAllStack;
        for (PlayerInfo playerInfo : players) {
            playerAllStack = playerInfo.getBet() + playerInfo.getStack();
            if (playerAllStack < result) {
                result = playerAllStack;
            }
        }
        return result;
    }

    public Action onMyAction(List<Card> boardCards, double pot) {
        sendVillainActions(boardCards, pot);
        Action heroAction;
        heroAction = player.getAction();
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
                player.onVillainActed(villainPreviousAction, heroPreviousAction.getAmount());
                gameInfo.pot += heroPreviousAction.getAmount();
            } else if (currentStreet == PokerStreet.PREFLOP
                    && isMinPotOnPreflop(pot) && heroOnButton) {
                Log.f(DEBUG_PATH, gameInfo.villainInfo.getName()
                        + " checks");
                villainPreviousAction = Action.checkAction();
                player.onVillainActed(villainPreviousAction, 0);
            } else if (!heroOnButton && currentStreet != PokerStreet.PREFLOP
                    && heroPreviousAction.isCheck()) {
                Log.f(DEBUG_PATH, gameInfo.villainInfo.getName()
                        + " checks");
                villainPreviousAction = Action.checkAction();
                player.onVillainActed(villainPreviousAction, 0);
            }
            streetChanged = true;
            currentStreet = newStreet;
        }
        //processing changing the street
        gameInfo.street = newStreet;

        processBoardCards(boardCards, gameInfo.pot);
        //sending current villain actions
        if (villainBet > heroBet && (gameInfo.street != PokerStreet.PREFLOP
                || villainBet != gameInfo.bigBlind)) {
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
            player.onVillainActed(villainPreviousAction, villainToCall);
            gameInfo.pot += villainToCall;
            gameInfo.raisesOnStreet[newStreet.intValue()]++;
            double villainRealBet = villainBet - heroBet;
            if (villainRealBet > gameInfo.bankrollAtRisk) {
                villainRealBet = gameInfo.bankrollAtRisk;
            }
            gameInfo.pot += villainRealBet;
            gameInfo.bankrollAtRisk -= villainRealBet;
        } else if (villainBet == heroBet) {
            if (!heroOnButton && gameInfo.street == PokerStreet.PREFLOP) {
                Log.f(DEBUG_PATH, gameInfo.villainInfo.getName()
                        + " calls " + gameInfo.bigBlind / 2);
                villainPreviousAction = Action.callAction(gameInfo.bigBlind / 2);
                player.onVillainActed(villainPreviousAction, gameInfo.bigBlind / 2);
                gameInfo.pot += gameInfo.bigBlind / 2;
            }
            if (heroOnButton && gameInfo.street != PokerStreet.PREFLOP) {
                Log.f(DEBUG_PATH, gameInfo.villainInfo.getName()
                        + " checks");
                villainPreviousAction = Action.checkAction();
                player.onVillainActed(villainPreviousAction, 0);
            }
        }
    }

    private boolean isMinPotOnPreflop(double pot) {
        return Math.abs(pot - gameInfo.villainInfo.getBet() - gameInfo.bigBlind * 2)
                < 0.1 * gameInfo.bigBlind * 2;
    }

    public void onNewHand(long handNumber, List<PlayerInfo> players,
            Card holeCard1, Card holeCard2, List<Card> board, double pot,
            LimitType limitType) {
        villainPreviousAction = null;
        currentStreet = calculateStreet(board);
        alreadyTakenCards = new ArrayList<Card>();
        gameInfo = new HUGameInfo();
        player.onHandStarted(gameInfo, handNumber);
        //player infos
        gameInfo.heroInfo = getHero(players);
        gameInfo.villainInfo = getVillain(players);

        currentHandNumber = handNumber;
        //game stage
        gameInfo.street = currentStreet;
        //big blind, pot, limit and effective stack
        gameInfo.bigBlind = getBigBlind(players);
        if (currentStreet == PokerStreet.PREFLOP) {
            heroPreviousAction = null;
            gameInfo.pot = gameInfo.bigBlind * 1.5;
        } else {
            heroPreviousAction = Action.checkAction();
            gameInfo.pot = pot;
        }
        gameInfo.effectiveStack = getEffectiveStack(players);
        gameInfo.bankrollAtRisk = gameInfo.effectiveStack - gameInfo.bigBlind;
        gameInfo.limitType = limitType;
        gameInfo.raisesOnStreet[PokerStreet.PREFLOP.intValue()] = 1;
        //logging
        Log.f(DEBUG_PATH, "\n<--------------------->");
        Log.f(DEBUG_PATH, "Hand number: " + handNumber + "\n");
        String smallBlindLog = "", bigBlindLog = "";
        for (PlayerInfo playerInfo : players) {
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

        player.onHoleCards(holeCard1, holeCard2, heroName,
                gameInfo.villainInfo.getName());
        if (currentStreet == PokerStreet.PREFLOP) {
            player.onStageEvent(currentStreet);
        }
    }
}
