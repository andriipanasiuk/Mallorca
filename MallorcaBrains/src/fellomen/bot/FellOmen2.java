/*This file is part of Fell Omen.

Fell Omen is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or
(at your option) any later version.

Fell Omen is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Foobar.  If not, see <http://www.gnu.org/licenses/>.*/
package fellomen.bot;

import java.util.List;

import mallorcatour.bot.actionpreprocessor.FLActionPreprocessor;
import mallorcatour.bot.interfaces.IPlayer;
import mallorcatour.core.equilator.PokerEquilatorBrecher;
import mallorcatour.core.equilator.StreetEquity;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.advice.AdviceCreator;
import mallorcatour.core.game.advice.SmartAdviceCreator;
import mallorcatour.core.game.advice.SmartPostflopAdviceCreator;
import mallorcatour.core.game.advice.SmartRiverAdviceCreator;
import mallorcatour.core.game.interfaces.IActionPreprocessor;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.util.Log;

//import com.ibot.HandPotential;
/** 
 * Ian Bot Impl
 * 
 * Description: {Insert Description here}
 * 
 * @author Ian Fellows
 */
public class FellOmen2 implements IPlayer {
    // constants used:

    private IPlayerGameInfo gameInfo;
    private Card holeCard1, holeCard2;
    private Card flop1, flop2, flop3, turn, river;
    private StreetEquity flopEquity, turnEquity;
    private double riverStrength;
    private boolean onButton;
    private final IActionPreprocessor preprocessor;
    private int flopBoardIndex;
    private int preflopHistory = -1, flopHistory = -1, turnHistory = -1;
    private final String DEBUG;

    public FellOmen2(String debug) {
        preprocessor = new FLActionPreprocessor();
        this.DEBUG = debug;
    }

    private Advice preFlopAction(boolean onButton, Card card1, Card card2) {
        String player = onButton ? "p2" : "p1";

        int preFlopIndex = FellOmenHelper.getPreflopIndex(card1, card2);
        Log.f(DEBUG, "Preflop index: " + preFlopIndex);

        // Generate the action table file name
        int actionNode = getActionNode(PokerStreet.PREFLOP, gameInfo.getNumRaises(), onButton);
        String actionTableFileName = player + "_strategy_" + actionNode + ".txt";
        Log.f(DEBUG, "Preflop file: " + actionTableFileName);

        double[][] actionDblArray = FellOmenHelper.getActionTable(0, actionTableFileName, DEBUG);

        // Now use this knowledge to determine what to do.
        double raiseProb = actionDblArray[2][preFlopIndex];
        double callProb = actionDblArray[1][preFlopIndex];
        double foldProb = actionDblArray[0][preFlopIndex];
        Log.f(DEBUG, "Probabilities: " + foldProb + " " + callProb + " " + raiseProb);

        AdviceCreator creator = new SmartAdviceCreator(true);
        Advice result = Advice.create(creator, foldProb, callProb, raiseProb);
        return result;
    }

    private Advice flopAction(boolean onButton, double strength, double positivePotential,
            double negativePotential, int flopBoardIndex, int preflopHistory) {
        String player = onButton ? "p2" : "p1";
        //calculate probability of winning against a random hand
        double rolloutHandStrength = strength * (1 - negativePotential)
                + (1 - strength) * positivePotential;

        //get index
        int flopIndex = FellOmenHelper.getFlopIndex(rolloutHandStrength, positivePotential);
        Log.f(DEBUG, "Rollout Hand Strength: " + rolloutHandStrength + " flop index: " + flopIndex);
        //find table
        int actionNode = getActionNode(PokerStreet.FLOP, gameInfo.getNumRaises(), onButton);
        String actionTableFileName = player + "_strategy" + preflopHistory + ""
                + flopBoardIndex + "_" + actionNode + ".txt";
        Log.f(DEBUG, "Flop file: " + actionTableFileName);

        double[][] actionDblArray = FellOmenHelper.getActionTable(1, actionTableFileName, DEBUG);

        // Now use this knowledge to determine what to do.
        double raiseProb = actionDblArray[2][flopIndex];
        double callProb = actionDblArray[1][flopIndex];
        double foldProb = actionDblArray[0][flopIndex];
        Log.f(DEBUG, "Probabilities: " + foldProb + " " + callProb + " " + raiseProb);


        AdviceCreator creator = new SmartPostflopAdviceCreator(true);
        Advice result = Advice.create(creator, foldProb, callProb, raiseProb);
        return result;
    }

    private Advice turnAction(boolean onButton, double strength,
            double positivePotential, double negativePotential, int flopBoardIndex,
            int preflopHistory, int flopHistory) {
        String player = onButton ? "p2" : "p1";
        //calculate probability of winning against a random hand
        double rolloutHandStrength = strength * (1 - negativePotential) + (1 - strength) * positivePotential;
        //get index
        int turnIndex = FellOmenHelper.getTurnIndex(rolloutHandStrength, positivePotential);
        Log.f(DEBUG, "rollout Hand Strength: " + rolloutHandStrength + " turn index: " + turnIndex);

        //find table
        int actionNode = getActionNode(PokerStreet.TURN, gameInfo.getNumRaises(), onButton);
        String actionTableFileName = player + "_strategy" + preflopHistory
                + flopBoardIndex + flopHistory + 0 + "_" + actionNode + ".txt";
        Log.f(DEBUG, "Turn file: " + actionTableFileName);

        double[][] actionDblArray = FellOmenHelper.getActionTable(2, actionTableFileName, DEBUG);

        // Now use this knowledge to determine what to do.
        double raiseProb = actionDblArray[2][turnIndex];
        double callProb = actionDblArray[1][turnIndex];
        double foldProb = actionDblArray[0][turnIndex];
        Log.f(DEBUG, "Probabilities: " + foldProb + " " + callProb + " " + raiseProb);

        AdviceCreator creator = new SmartPostflopAdviceCreator(true);
        Advice result = Advice.create(creator, foldProb, callProb, raiseProb);

        return result;
    }

    private Advice riverAction(boolean onButton, double strength, int flopBoardIndex,
            int preflopHistory, int flopHistory, int turnHistory) {
        String player = onButton ? "p2" : "p1";
        //calculate probability of winning against a random hand
        double rolloutHandStrength = strength;
        //get index
        int riverIndex = FellOmenHelper.getRiverIndex(rolloutHandStrength);
        Log.f(DEBUG, "Rollout Hand Strength: " + rolloutHandStrength + " river index: " + riverIndex);
        //find table
        int actionNode = getActionNode(PokerStreet.RIVER, gameInfo.getNumRaises(), onButton);
        String actionTableFileName = player + "_strategy" + preflopHistory + flopBoardIndex
                + flopHistory + 0 + turnHistory + 0 + "_" + actionNode + ".txt";
        Log.f(DEBUG, "River file: " + actionTableFileName);

        double[][] actionDblArray = FellOmenHelper.getActionTable(3, actionTableFileName, DEBUG);

        // Now use this knowledge to determine what to do.
        double raiseProb = actionDblArray[2][riverIndex];
        double callProb = actionDblArray[1][riverIndex];
        double foldProb = actionDblArray[0][riverIndex];

        Log.f(DEBUG, "Probabilities: " + foldProb + " " + callProb + " " + raiseProb);
        AdviceCreator creator = new SmartRiverAdviceCreator(
                gameInfo.getNumRaises() == 0, true);
        Advice result = Advice.create(creator, foldProb, callProb, raiseProb);
        return result;
    }

    private int getActionNode(PokerStreet street, int numRaises, boolean onButton) {
        if (street == PokerStreet.PREFLOP) {
            numRaises--;
            if (!onButton) {
                if (numRaises == 0) {
                    return 10;
                } else if (numRaises == 1) {
                    return 4;
                } else if (numRaises == 2) {
                    return 8;
                } else if (numRaises == 3) {
                    return 2;
                } else if (numRaises == 4) {
                    return 6;
                }
            } else {
                if (numRaises == 0) {
                    return 5;
                } else if (numRaises == 1) {
                    return 9;
                } else if (numRaises == 2) {
                    return 3;
                } else if (numRaises == 3) {
                    return 7;
                } else if (numRaises == 4) {
                    return 1;
                }
            }
        } else {
            //player 2
            if (onButton) {
                if (numRaises == 0) {
                    return 10;
                } else if (numRaises == 1) {
                    return 4;
                } else if (numRaises == 2) {
                    return 8;
                } else if (numRaises == 3) {
                    return 2;
                } else if (numRaises == 4) {
                    return 6;
                }
            } //player 1
            else {
                if (numRaises == 0) {
                    return 5;
                } else if (numRaises == 1) {
                    return 9;
                } else if (numRaises == 2) {
                    return 3;
                } else if (numRaises == 3) {
                    return 7;
                } else if (numRaises == 4) {
                    return 1;
                }
            }
        }
        throw new RuntimeException("Incorrect num of raises: " + numRaises);
    }

    private int nodeToTerminalNodeIndex(int node) {
        if (node == 1) {
            return 0;
        } else if (node == 2) {
            return 1;
        } else if (node == 3) {
            return 2;
        } else if (node == 4) {
            return 3;
        } else if (node == 6) {
            return 4;
        } else if (node == 7) {
            return 5;
        } else if (node == 8) {
            return 6;
        } else if (node == 9) {
            return 7;
        } else if (node == 10) {
            return 8;
        } else {
            throw new RuntimeException("Incorrect node: " + node);
        }
    }

    @Override
    public void onHandStarted(IPlayerGameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    public void onHoleCards(Card card1, Card card2, String villainName) {
        this.holeCard1 = card1;
        this.holeCard2 = card2;
        this.onButton = gameInfo.onButton();
    }

    private void checkPreflopHistory() {
        if (preflopHistory == -1) {
            preflopHistory = nodeToTerminalNodeIndex(
                    getActionNode(PokerStreet.PREFLOP, 1, false));
        }
    }

    private void checkFlopHistory() {
        if (flopHistory == -1) {
            flopHistory = nodeToTerminalNodeIndex(
                    getActionNode(PokerStreet.FLOP, 0, true));
        }
    }

    private void checkTurnHistory() {
        if (turnHistory == -1) {
            turnHistory = nodeToTerminalNodeIndex(
                    getActionNode(PokerStreet.TURN, 0, true));
        }
    }

    public Action getAction() {
        Log.f(DEBUG, "=========  Decision-making  =========");
        Advice advice;
        Action action;
        if (gameInfo.isVillainSitOut()) {
            Log.f(DEBUG, "Villain is sitting out");
            double percent = 0.5;
            action = Action.createRaiseAction(percent
                    * (gameInfo.getPotSize() + gameInfo.getHeroAmountToCall()), percent);
        } else {
            if (gameInfo.isPreFlop()) {
                advice = preFlopAction(onButton, holeCard1, holeCard2);
            } else if (gameInfo.isFlop()) {
                checkPreflopHistory();
                advice = flopAction(onButton, flopEquity.strength, flopEquity.positivePotential,
                        flopEquity.negativePotential, flopBoardIndex, preflopHistory);
            } else if (gameInfo.isTurn()) {
                checkPreflopHistory();
                checkFlopHistory();
                advice = turnAction(onButton, turnEquity.strength, turnEquity.positivePotential,
                        turnEquity.negativePotential, flopBoardIndex, preflopHistory, flopHistory);
            } else if (gameInfo.isRiver()) {
                checkPreflopHistory();
                checkFlopHistory();
                checkTurnHistory();
                advice = riverAction(onButton, riverStrength, flopBoardIndex,
                        preflopHistory, flopHistory, turnHistory);
            } else {
                throw new RuntimeException();
            }
            Log.f(DEBUG, "Advice: " + advice.toString());
            action = advice.getAction();
            action = preprocessor.preprocessAction(action, gameInfo);
        }
        Log.f(DEBUG, "Action: " + action.toString());
        Log.f(DEBUG, "===============  End  ==============");
        onPlayerActed(action, onButton);
        return action;
    }

    public void onStageEvent(PokerStreet street) {
        List<Card> board = gameInfo.getBoard();
        if (street == PokerStreet.FLOP) {
            flop1 = board.get(0);
            flop2 = board.get(1);
            flop3 = board.get(2);
            Log.f(DEBUG, "FLOP: " + flop1 + " " + flop2 + " " + flop3);
            flopBoardIndex = FellOmenHelper.getFlopBoardIndex(flop1, flop2, flop3);
            Log.f(DEBUG, "Flop board index: " + flopBoardIndex);
            flopEquity = PokerEquilatorBrecher.equityOnFlopFull(
                    holeCard1, holeCard2, flop1, flop2, flop3, true);
        } else if (street == PokerStreet.TURN) {
            flop1 = board.get(0);
            flop2 = board.get(1);
            flop3 = board.get(2);
            turn = board.get(3);
            flopBoardIndex = FellOmenHelper.getFlopBoardIndex(flop1, flop2, flop3);
            Log.f(DEBUG, "TURN: " + flop1 + " " + flop2 + " " + flop3 + " " + turn);
            turnEquity = PokerEquilatorBrecher.equityOnTurn(
                    holeCard1, holeCard2, flop1, flop2, flop3, turn);
        } else if (street == PokerStreet.RIVER) {
            flop1 = board.get(0);
            flop2 = board.get(1);
            flop3 = board.get(2);
            turn = board.get(3);
            river = board.get(4);
            flopBoardIndex = FellOmenHelper.getFlopBoardIndex(flop1, flop2, flop3);
            Log.f(DEBUG, "RIVER: " + flop1 + " " + flop2 + " " + flop3 + " " + turn + " " + river);
            riverStrength = PokerEquilatorBrecher.strengthOnRiver(
                    holeCard1, holeCard2, flop1, flop2, flop3, turn, river);
        }
    }

    private void onPlayerActed(Action action, boolean onButton) {
        int numRaises = gameInfo.getNumRaises();
        if (gameInfo.isPreFlop()) {
            if ((action.isCheck() || (action.isCall() && numRaises != 1))) {
                preflopHistory = nodeToTerminalNodeIndex(
                        getActionNode(PokerStreet.PREFLOP, numRaises, onButton));
                Log.f(DEBUG, "Preflop history calculated: " + preflopHistory);
            }
        } else if (gameInfo.isFlop()) {
            if ((action.isCheck() && onButton) || action.isCall()) {
                flopHistory = nodeToTerminalNodeIndex(
                        getActionNode(PokerStreet.FLOP, numRaises, onButton));
                Log.f(DEBUG, "Flop history calculated: " + flopHistory);
            }
        } else if (gameInfo.isTurn()) {
            if ((action.isCheck() && onButton) || action.isCall()) {
                turnHistory = nodeToTerminalNodeIndex(
                        getActionNode(PokerStreet.TURN, numRaises, onButton));
                Log.f(DEBUG, "Turn history calculated: " + turnHistory);
            }
        }
    }

    public void onVillainActed(Action action, double toCall) {
        onPlayerActed(action, !onButton);
    }

    private void testActionTableParsing() {
        double[][] dblArray = null;
        dblArray = FellOmenHelper.getActionTable(0, "p2_strategy_5.txt", DEBUG);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 50; j++) {
                Log.d("[" + i + "]::[" + j + "]::[" + dblArray[i][j] + "]");
            }
        }
    }

    public static void main(String[] arf) {
        new FellOmen2("debug.txt").testActionTableParsing();
    }

	@Override
	public void onHandEnded() {
		//do nothing
	}

	@Override
	public String getName() {
		return "FellOmen2";
	}
}
