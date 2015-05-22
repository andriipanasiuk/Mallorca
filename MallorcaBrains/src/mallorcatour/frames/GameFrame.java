/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GrandtorinoGameFrame.java
 *
 * Created on 02.12.2012, 13:44:00
 */
package mallorcatour.frames;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import mallorcatour.bot.interfaces.IDecisionListener;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.bot.math.NLMathBot;
import mallorcatour.bot.villainobserver.VillainStatistics;
import mallorcatour.core.equilator.PokerEquilatorBrecher;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.Deck;
import mallorcatour.core.game.Flop;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.bot.modeller.VillainModel;
import mallorcatour.bot.neural.NeuralBotFactory;
import mallorcatour.brains.IAdvisor;
import mallorcatour.interfaces.IRandomizer;
import mallorcatour.robot.ExtPlayerInfo;
import mallorcatour.robot.controller.HUGameControllerExt;
import mallorcatour.robot.controller.PokerPreferences;
import mallorcatour.util.DateUtils;
import mallorcatour.util.ExecutorUtils;
import mallorcatour.util.Log;
import mallorcatour.util.OnExceptionListener;
import mallorcatour.util.SerializatorUtils;
import mallorcatour.util.UniformRandomizer;
import mallorcatour.util.frames.FrameUtils;

/**
 * Frame for playing with bot.
 * @author Andrew
 */
public class GameFrame extends javax.swing.JFrame {

    private HUGameControllerExt controller;
    private long currentHandNumber = -1;
    private ExtPlayerInfo heroInfo, villainInfo;
    //temporary. Robot must recognize limit type from table
    private final LimitType limitType;
    private List<Card> nonUsedCards;
    private IRandomizer randomizer = new UniformRandomizer();
    private double pot, myBet, botBet, botStack = 6000, myStack = 6000;
    private PokerStreet currentStreet;
    private final static double BIG_BLIND = 10;
    private List<Card> boardCards;
    private boolean tradeOpened;
    private Card botCard1, botCard2, myCard1, myCard2;
    private boolean endOfHand;
    private final Object lock = new Object();
    private final ExecutorService executor;
    private final ShowingSpectrumListener spectrumListener;
    private boolean useGoButton;
    private VillainModel villainModeller;
//    private Flop predefinedFlop = new Flop(Card.valueOf("Td"), Card.valueOf("2s"), Card.valueOf("3s"));
//    private Card predefinedTurn = Card.valueOf("Qh");
//    private Card predefinedRiver = Card.valueOf("5h");
    private Flop predefinedFlop = null;
    private Card predefinedTurn = null;
    private Card predefinedRiver = null;
    private final String DEBUG_PATH;

    /** Creates new form GrandtorinoGameFrame */
    public GameFrame(LimitType limitType) {
        initComponents();
        spectrumListener = new ShowingSpectrumListener();
        DEBUG_PATH = PokerPreferences.DEBUG_PATTERN
                + DateUtils.getDate(false) + ".txt";
        villainModeller = new VillainModel(limitType, DEBUG_PATH);
		NeuralBotFactory factory = new NeuralBotFactory();
		IPlayer player = factory.createBot(IAdvisor.UNSUPPORTED, ISpectrumListener.EMPTY, IDecisionListener.EMPTY,
				"debug.txt");
		controller = new HUGameControllerExt(player, PokerPreferences.DEFAULT_HERO_NAME, DEBUG_PATH);
		executor = ExecutorUtils.newSingleThreadExecutor(OnExceptionListener.EMPTY);
		this.limitType = limitType;
		enableActionButtons(false);
		myDealerButton.setVisible(false);
		botDealerButton.setVisible(false);

        spectrumListener.setShow(showSpectrum);
        botCardsVisibleToggleButton.getModel().setPressed(true);
    }

    private void dealAction() {
        endOfHand = false;
        tradeOpened = false;
        botActionLabel.setText("");
        nonUsedCards = new ArrayList<Card>(Deck.getCards());
        currentHandNumber++;
        //street
        currentStreet = PokerStreet.PREFLOP;

//        HoleCards botCards = dealBotCards("4h", "Qc");
        HoleCards botCards = dealBotCards();
        botCardsField.setText(botCards.toString());
        myHoleCardsField.setText(dealMyCards().toString());

        //button
        boolean heroOnButton = currentHandNumber % 2 == 1;
        heroInfo = new ExtPlayerInfo(PokerPreferences.DEFAULT_HERO_NAME, heroOnButton);
        villainInfo = new ExtPlayerInfo(PokerPreferences.DEFAULT_VILLAIN_NAME, !heroOnButton);
        //
        botDealerButton.setVisible(false);
        myDealerButton.setVisible(false);
        botStack -= BIG_BLIND / 2;
        myStack -= BIG_BLIND / 2;

        if (heroInfo.isOnButton()) {
            botDealerButton.setVisible(true);
            myStack -= BIG_BLIND / 2;
        } else {
            myDealerButton.setVisible(true);
            botStack -= BIG_BLIND / 2;
        }
        //bets
        botBet = BIG_BLIND / 2 * (heroInfo.isOnButton() ? 1 : 2);
        myBet = BIG_BLIND / 2 * (villainInfo.isOnButton() ? 1 : 2);
        pot = botBet + myBet;
        heroInfo.bet = botBet;
        villainInfo.bet = myBet;

        //stacks
        heroInfo.stack = botStack;
        villainInfo.stack = myStack;

        boardCards = new ArrayList<Card>();
        updateUI();
        controller.onNewHand(currentHandNumber,
                Arrays.asList(new ExtPlayerInfo[]{heroInfo, villainInfo}),
                botCard1, botCard2, boardCards, pot, limitType);
        dealButton.setVisible(false);
        if (heroInfo.isOnButton()) {
            botAction();
        } else {
            myAction();
        }
    }

    private HoleCards dealBotCards() {
        botCard1 = nonUsedCards.remove(randomizer.getRandom(0, nonUsedCards.size()));
        botCard2 = nonUsedCards.remove(randomizer.getRandom(0, nonUsedCards.size()));
        return new HoleCards(botCard1, botCard2);
    }

    private HoleCards dealBotCards(String one, String two) {
        if (one.equals(two)) {
            throw new RuntimeException();
        }
        botCard1 = Card.valueOf(one);
        botCard2 = Card.valueOf(two);
        nonUsedCards.remove(botCard1);
        nonUsedCards.remove(botCard2);
        return new HoleCards(botCard1, botCard2);
    }

    private HoleCards dealMyCards() {
        myCard1 = nonUsedCards.remove(randomizer.getRandom(0, nonUsedCards.size()));
        myCard2 = nonUsedCards.remove(randomizer.getRandom(0, nonUsedCards.size()));
        return new HoleCards(myCard1, myCard2);
    }

    private void botAction() {
        if (useGoButton) {
            lock();
        }
        //recognizing bets
        heroInfo.bet = botBet;
        villainInfo.bet = myBet;

        Action action = controller.onMyAction(boardCards, pot);
        botActed(action);
        if (!endOfHand) {
            myAction();
        }
    }

    private void lock() {
        synchronized (lock) {
            try {
                goButton.setEnabled(true);
                lock.wait();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
        goButton.setEnabled(false);
    }

    private void changeStreet() {
        if (useGoButton) {
            lock();
        }
        goButton.setEnabled(false);
        tradeOpened = false;
        if (currentStreet == PokerStreet.RIVER) {
            List<Card> allBotCards = new ArrayList<Card>(boardCards);
            List<Card> allMyCards = new ArrayList<Card>(boardCards);
            allBotCards.add(botCard1);
            allBotCards.add(botCard2);
            allMyCards.add(myCard1);
            allMyCards.add(myCard2);
            long botCombination = PokerEquilatorBrecher.combination(Card.convertToIntBrecherArray(allBotCards));
            long myCombination = PokerEquilatorBrecher.combination(Card.convertToIntBrecherArray(allMyCards));
            String result = null;
            if (botCombination > myCombination) {
                result = "Hero";
            } else if (botCombination < myCombination) {
                result = "Villain";
            }
            endOfHand(result);
        } else if (currentStreet == PokerStreet.PREFLOP) {
            if (predefinedFlop != null) {
                dealFlop(predefinedFlop);
            } else {
                dealFlop();
            }
            currentStreet = PokerStreet.FLOP;
            myBet = 0;
            botBet = 0;
            updateUI();
            if (heroInfo.isOnButton()) {
                myAction();
            } else {
                botAction();
            }
        } else {
            if (currentStreet == PokerStreet.FLOP) {
                if (predefinedTurn != null) {
                    dealOneCard(predefinedTurn);
                } else {
                    dealOneCard();
                }
            } else {
                if (predefinedRiver != null) {
                    dealOneCard(predefinedRiver);
                } else {
                    dealOneCard();
                }
            }
            myBet = 0;
            botBet = 0;
            updateUI();

            if (currentStreet == PokerStreet.FLOP) {
                currentStreet = PokerStreet.TURN;
            } else if (currentStreet == PokerStreet.TURN) {
                currentStreet = PokerStreet.RIVER;
            }
            if (heroInfo.isOnButton()) {
                myAction();
            } else {
                botAction();
            }
        }
    }

    private void dealFlop() {
        Card flop1 = nonUsedCards.get(randomizer.getRandom(0, nonUsedCards.size()));
        nonUsedCards.remove(flop1);
        Card flop2 = nonUsedCards.get(randomizer.getRandom(0, nonUsedCards.size()));
        nonUsedCards.remove(flop2);
        Card flop3 = nonUsedCards.get(randomizer.getRandom(0, nonUsedCards.size()));
        nonUsedCards.remove(flop3);
        boardCards.add(flop1);
        boardCards.add(flop2);
        boardCards.add(flop3);
    }

    private void dealOneCard() {
        Card card = nonUsedCards.get(randomizer.getRandom(0, nonUsedCards.size()));
        nonUsedCards.remove(card);
        boardCards.add(card);

    }

    private void dealOneCard(String card) {
        Card c = Card.valueOf(card);
        nonUsedCards.remove(c);
        boardCards.add(c);
    }

    private void dealOneCard(Card c) {
        nonUsedCards.remove(c);
        boardCards.add(c);
    }

    private void dealFlop(String one, String two, String three) {
        Card flop1 = Card.valueOf(one);
        Card flop2 = Card.valueOf(two);
        Card flop3 = Card.valueOf(three);
        nonUsedCards.remove(flop1);
        nonUsedCards.remove(flop2);
        nonUsedCards.remove(flop3);
        boardCards.add(flop1);
        boardCards.add(flop2);
        boardCards.add(flop3);

    }

    private void dealFlop(Flop flop) {
        Card flop1 = flop.first;
        Card flop2 = flop.second;
        Card flop3 = flop.third;
        nonUsedCards.remove(flop1);
        nonUsedCards.remove(flop2);
        nonUsedCards.remove(flop3);
        boardCards.add(flop1);
        boardCards.add(flop2);
        boardCards.add(flop3);

    }

    private void myAction() {
        enableActionButtons(true);
        if (botBet > myBet) {
            passiveButton.setText("Call");
            aggressiveButton.setText("Raise");
        } else {
            passiveButton.setText("Check");
            aggressiveButton.setText("Bet");
        }
        double bet;
        if (currentStreet == PokerStreet.PREFLOP || currentStreet == PokerStreet.FLOP) {
            bet = BIG_BLIND;
        } else {
            bet = 2 * BIG_BLIND;
        }
        if (botBet == 4 * bet) {
            aggressiveButton.setEnabled(false);
        }
    }

    private void iActed(Action action) {
        enableActionButtons(false);
        if (action.isFold()) {
            double toCall = botBet - myBet;
            controller.onVillainActed(action, toCall);
            endOfHand("Hero");
        } else {
            double toCall = botBet - myBet;
            pot += toCall;
            myStack -= toCall;
            myBet = botBet;
            if (action.isAggressive()) {
                myStack -= action.getAmount();
                pot += action.getAmount();
                myBet += action.getAmount();
                tradeOpened = true;
            }
            updateUI();
            if (action.isPassive()) {
                if (tradeOpened) {
                    if (currentStreet == PokerStreet.RIVER) {
                        controller.onVillainActed(action, toCall);
                    }
                    changeStreet();
                } else {
                    tradeOpened = true;
                    botAction();
                }
            } else {
                botAction();
            }
        }
    }

    private void enableActionButtons(boolean enable) {
        foldButton.setEnabled(enable);
        passiveButton.setEnabled(enable);
        aggressiveButton.setEnabled(enable);
    }

    private void endOfHand(String winner) {
        dealButton.setVisible(true);
        botCardsField.setVisible(true);
        new Thread() {

            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                botCardsField.setVisible(botCardsVisibleToggleButton.getModel().isPressed());
            }
        }.start();
        enableActionButtons(false);
        Log.d("End of hand " + winner);
        if (winner == null) {
        } else if (winner.equals("Hero")) {
            botStack += pot;
        } else {
            myStack += pot;
        }
        pot = 0;
        myBet = 0;
        botBet = 0;
        updateUI();
        endOfHand = true;
    }

    private void updateUI() {
        botBetField.setText(botBet + "");
        myBetField.setText(myBet + "");
        potField.setText(pot + "");
        boardField.setText(boardCards.toString());
        myStackField.setText(myStack + "");
        botStackField.setText(botStack + "");
    }

    private void botActed(Action action) {
        botActionLabel.setText(action.toString());
        if (action.isFold()) {
            Log.f(DEBUG_PATH, PokerPreferences.DEFAULT_HERO_NAME + " folds");
            endOfHand("Villain");
        } else if (action.isCall()) {
            Log.f(DEBUG_PATH, PokerPreferences.DEFAULT_HERO_NAME + " calls");
            pot += action.getAmount();
            botBet += action.getAmount();
            botStack -= action.getAmount();
            updateUI();
            if (tradeOpened) {
                changeStreet();
            } else {
                tradeOpened = true;
            }
        } else if (action.isCheck()) {
            Log.f(DEBUG_PATH, PokerPreferences.DEFAULT_HERO_NAME + " checks");
            if (tradeOpened) {
                changeStreet();
            } else {
                tradeOpened = true;
            }
        } else if (action.isAggressive()) {
            Log.f(DEBUG_PATH, PokerPreferences.DEFAULT_HERO_NAME + " raises " + action.getAmount());
            double plusAmount = (myBet - botBet) + action.getAmount();
            botBet += plusAmount;
            pot += plusAmount;
            botStack -= plusAmount;
            if (!tradeOpened) {
                tradeOpened = true;
            }
        } else {
            Log.f(DEBUG_PATH,
                    "<--------------------Somethind wrong in doAction(). "
                    + PokerPreferences.DEFAULT_HERO_NAME + " folds.------------>");
            throw new RuntimeException();
        }
        if (!action.isFold()) {
            updateUI();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dealButton = new javax.swing.JButton();
        foldButton = new javax.swing.JButton();
        passiveButton = new javax.swing.JButton();
        aggressiveButton = new javax.swing.JButton();
        myHoleCardsField = new javax.swing.JTextField();
        botCardsField = new javax.swing.JTextField();
        botBetField = new javax.swing.JTextField();
        potField = new javax.swing.JTextField();
        myBetField = new javax.swing.JTextField();
        boardField = new javax.swing.JTextField();
        myDealerButton = new javax.swing.JButton();
        botDealerButton = new javax.swing.JButton();
        botStackField = new javax.swing.JTextField();
        myStackField = new javax.swing.JTextField();
        botActionLabel = new javax.swing.JLabel();
        goButton = new javax.swing.JButton();
        botCardsVisibleToggleButton = new javax.swing.JToggleButton();
        showSpectrumToggleButton = new javax.swing.JToggleButton();
        useGoButtonCheckBox = new javax.swing.JCheckBox();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        dealButton.setText("Deal");
        dealButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dealButtonActionPerformed(evt);
            }
        });

        foldButton.setText("Fold");
        foldButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                foldButtonActionPerformed(evt);
            }
        });

        passiveButton.setText("Check");
        passiveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passiveButtonActionPerformed(evt);
            }
        });

        aggressiveButton.setText("Bet");
        aggressiveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aggressiveButtonActionPerformed(evt);
            }
        });

        boardField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boardFieldActionPerformed(evt);
            }
        });

        myDealerButton.setText("D");

        botDealerButton.setText("D");

        goButton.setText("GO");
        goButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goButtonActionPerformed(evt);
            }
        });

        botCardsVisibleToggleButton.setText("Make unvisible");
        botCardsVisibleToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botCardsVisibleToggleButtonActionPerformed(evt);
            }
        });

        showSpectrumToggleButton.setText("Show spectrum");
        showSpectrumToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showSpectrumToggleButtonActionPerformed(evt);
            }
        });

        useGoButtonCheckBox.setText("Use GO button");
        useGoButtonCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                useGoButtonCheckBoxStateChanged(evt);
            }
        });

        jMenu1.setText("File");

        jMenuItem1.setText("Set villain...");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(potField, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(39, 39, 39)
                .addComponent(boardField, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 133, Short.MAX_VALUE)
                .addComponent(showSpectrumToggleButton)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(dealButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(myStackField, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(58, 58, 58)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(myDealerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(foldButton, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(passiveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(myBetField, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(botDealerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(37, 37, 37)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(botStackField)
                                    .addComponent(botBetField, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE))))))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGap(69, 69, 69)
                            .addComponent(botCardsField, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
                            .addComponent(botCardsVisibleToggleButton)
                            .addContainerGap())
                        .addGroup(layout.createSequentialGroup()
                            .addGap(10, 10, 10)
                            .addComponent(aggressiveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap(196, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(botActionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(62, 62, 62))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(goButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(useGoButtonCheckBox)
                            .addContainerGap()))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(72, 72, 72)
                        .addComponent(myHoleCardsField, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(botStackField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(botBetField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(botDealerButton)
                        .addComponent(botCardsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(botCardsVisibleToggleButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botActionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(potField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(boardField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 47, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(myBetField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(myDealerButton)
                            .addComponent(myHoleCardsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(foldButton)
                            .addComponent(aggressiveButton)
                            .addComponent(passiveButton)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(showSpectrumToggleButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(dealButton)
                            .addComponent(goButton)
                            .addComponent(useGoButtonCheckBox)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(myStackField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(19, 19, 19))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void dealButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dealButtonActionPerformed
        ExecutorUtils.newSingleThreadExecutor(
                OnExceptionListener.EMPTY).submit(new Runnable() {

            public void run() {
                dealAction();
            }
        });

    }//GEN-LAST:event_dealButtonActionPerformed
    private void foldButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_foldButtonActionPerformed
        iActed(Action.foldAction());
    }//GEN-LAST:event_foldButtonActionPerformed
    private void passiveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passiveButtonActionPerformed
        executor.submit(new Runnable() {

            public void run() {
                if (passiveButton.getText().equals("Check")) {
                    iActed(Action.checkAction());
                } else {
                    iActed(Action.callAction(botBet - myBet));
                }
            }
        });
    }//GEN-LAST:event_passiveButtonActionPerformed

    private void aggressiveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aggressiveButtonActionPerformed
        executor.submit(new Runnable() {

            public void run() {
                if (currentStreet == PokerStreet.PREFLOP || currentStreet == PokerStreet.FLOP) {
                    iActed(Action.createRaiseAction(BIG_BLIND, -1));
                } else {
                    iActed(Action.createRaiseAction(2 * BIG_BLIND, -1));
                }
            }
        });

    }//GEN-LAST:event_aggressiveButtonActionPerformed

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {

                EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        new GameFrame(LimitType.NO_LIMIT).setVisible(true);
                    }
                });
            }
        });
    }
    private void boardFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boardFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_boardFieldActionPerformed

    private void goButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goButtonActionPerformed
        synchronized (lock) {
            lock.notifyAll();
            goButton.setEnabled(false);
        }
    }//GEN-LAST:event_goButtonActionPerformed

    private void botCardsVisibleToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botCardsVisibleToggleButtonActionPerformed
        if (botCardsField.isVisible()) {
            botCardsField.setVisible(false);
            botCardsVisibleToggleButton.setText("Make visible");
        } else {
            botCardsField.setVisible(true);
            botCardsVisibleToggleButton.setText("Make unvisible");
        }
    }//GEN-LAST:event_botCardsVisibleToggleButtonActionPerformed
    private boolean showSpectrum = false;
    private void showSpectrumToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showSpectrumToggleButtonActionPerformed
        spectrumListener.setShow(!showSpectrum);
        showSpectrum = !showSpectrum;
    }//GEN-LAST:event_showSpectrumToggleButtonActionPerformed

    private void useGoButtonCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_useGoButtonCheckBoxStateChanged
        useGoButton = useGoButtonCheckBox.getModel().isSelected();
        goButton.setEnabled(useGoButton);
    }//GEN-LAST:event_useGoButtonCheckBoxStateChanged

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        String path = FrameUtils.openFileChooser(this, "./villains");
        if (path == null) {
            return;
        }
        villainModeller.onVillainKnown(true);
        villainModeller.onVillainChange(SerializatorUtils.load(path, VillainStatistics.class));
    }//GEN-LAST:event_jMenuItem1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aggressiveButton;
    private javax.swing.JTextField boardField;
    private javax.swing.JLabel botActionLabel;
    private javax.swing.JTextField botBetField;
    private javax.swing.JTextField botCardsField;
    private javax.swing.JToggleButton botCardsVisibleToggleButton;
    private javax.swing.JButton botDealerButton;
    private javax.swing.JTextField botStackField;
    private javax.swing.JButton dealButton;
    private javax.swing.JButton foldButton;
    private javax.swing.JButton goButton;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JTextField myBetField;
    private javax.swing.JButton myDealerButton;
    private javax.swing.JTextField myHoleCardsField;
    private javax.swing.JTextField myStackField;
    private javax.swing.JButton passiveButton;
    private javax.swing.JTextField potField;
    private javax.swing.JToggleButton showSpectrumToggleButton;
    private javax.swing.JCheckBox useGoButtonCheckBox;
    // End of variables declaration//GEN-END:variables
}
