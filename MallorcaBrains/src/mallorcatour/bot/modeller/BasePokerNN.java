/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.modeller;

import java.util.ArrayList;
import java.util.List;

import mallorcatour.bot.interfaces.IPokerNN;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.advice.AdviceCreator;
import mallorcatour.core.game.advice.SmartAdviceCreator;
import mallorcatour.core.game.advice.SmartPostflopAdviceCreator;
import mallorcatour.core.game.advice.SmartRiverAdviceCreator;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.core.game.situation.LocalSituationDistance;
import mallorcatour.core.game.situation.LocalSituationInterpreter;
import mallorcatour.grandtorino.nn.core.AbstractNeurals;
import mallorcatour.neural.core.PokerExamples;
import mallorcatour.neural.core.PokerLearningExample;
import mallorcatour.neural.manager.LEManager;
import mallorcatour.util.Log;
import mallorcatour.util.SerializatorUtils;
import org.neuroph.core.NeuralNetwork;

/**
 *
 * Poker neural network that uses multi-layer perceptrons learned on hands from
 * one of PA bots. This NN does not use global parameters (see IGlobalSituation).
 * But before using of multi-layer perceptron this network looking for the same
 * situation from situation files. If there are few(count of situations and
 * the degree of similarity are defined in this class) same situations
 * result will be the average output of these situations. If there are not
 * it uses output of multi-layer perceptrons. As interpreter (see
 * IOutputInterpreter) of output vector and creating advice from it this class
 * uses SmartAdviceCreator for flop and turn and special advice creators for
 * preflop and river.
 *
 * @author Andrew
 */
public class BasePokerNN implements IPokerNN {

    private AbstractNeurals neurals;
    //neural networks
    private NeuralNetwork preflopNN, flopNN, turnNN, riverNN;
    //list of situations
    private List<PokerLearningExample> allSituations;
    private List<PokerLearningExample> preflopSituations, flopSituations,
            turnSituations, riverSituations;
    private int MIN_COUNT_OF_SIMILAR_SITUATIONS = 4;
    private final static double[] DEGREE_OF_SIMILARITY = new double[4];
    private final boolean useSituations;

    public BasePokerNN(AbstractNeurals nnStreaming, boolean useSituations) {
        this.neurals = nnStreaming;
        this.useSituations = useSituations;
        DEGREE_OF_SIMILARITY[0] = 0.05;
        DEGREE_OF_SIMILARITY[1] = 0.05;
        DEGREE_OF_SIMILARITY[2] = 0.1;
        DEGREE_OF_SIMILARITY[3] = 0.1;
        initNN();
        if (useSituations) {
            initSituations();
        }
    }

    public BasePokerNN(AbstractNeurals nnStreaming, boolean useSituations,
            int minCountOfSimilar) {
        this(nnStreaming, useSituations);
        this.MIN_COUNT_OF_SIMILAR_SITUATIONS = minCountOfSimilar;
    }

    private void initNN() {
        preflopNN = neurals.getPreflopNN();
        flopNN = neurals.getFlopNN();
        turnNN = neurals.getTurnNN();
        riverNN = neurals.getRiverNN();
        Log.d("BasePokerNN. Neural networks inited");
    }

    private void initSituations() {
        allSituations = SerializatorUtils.load(
                neurals.getSituationsStream(),
                PokerExamples.class).getExamples();
        preflopSituations = new ArrayList<PokerLearningExample>();
        flopSituations = new ArrayList<PokerLearningExample>();
        turnSituations = new ArrayList<PokerLearningExample>();
        riverSituations = new ArrayList<PokerLearningExample>();

        for (PokerLearningExample situation : allSituations) {
            int street = situation.getSituation().getStreet();
            if (street == LocalSituation.PREFLOP) {
                preflopSituations.add(situation);
            } else if (street == LocalSituation.FLOP) {
                flopSituations.add(situation);
            } else if (street == LocalSituation.TURN) {
                turnSituations.add(situation);
            } else if (street == LocalSituation.RIVER) {
                riverSituations.add(situation);
            }
        }
        Log.d("BasePokerNN. Situations inited");
    }

    public Advice getAdvice(LocalSituation situation, HoleCards cards) {
        List<PokerLearningExample> examplesForUse = null;
        NeuralNetwork nnForUse = null;
        AdviceCreator adviceCreator = null;
        boolean canRaise = situation.canRaise();
        int street = situation.getStreet();
        switch (street) {
            case 0:
                nnForUse = preflopNN;
                examplesForUse = preflopSituations;
                adviceCreator = new SmartAdviceCreator(canRaise);
                break;
            case 1:
                nnForUse = flopNN;
                examplesForUse = flopSituations;
                adviceCreator = new SmartPostflopAdviceCreator(canRaise);
                break;
            case 2:
                nnForUse = turnNN;
                examplesForUse = turnSituations;
                adviceCreator = new SmartPostflopAdviceCreator(canRaise);
                break;
            case 3:
                nnForUse = riverNN;
                examplesForUse = riverSituations;
                adviceCreator = new SmartRiverAdviceCreator(
                        situation.getPotOdds() == 0, canRaise);
                break;
            default:
                throw new IllegalArgumentException("Illegal street: " + street);
        }
        Advice result;
        if (useSituations) //if there are similar situations
        {
            long start = System.currentTimeMillis();
            result = processSimilarity(examplesForUse, situation, adviceCreator);
            Log.d("Search for similar situations longs "
                    + (System.currentTimeMillis() - start) + " ms");
            if (result != null) {
                return result;
            }
        }
        //if there are not use neural network
        nnForUse.setInput(
                new LocalSituationInterpreter().createInput(situation));
        nnForUse.calculate();
        double[] advice = nnForUse.getOutput();
        result = Advice.create(adviceCreator, advice);
        return result;
    }

    private Advice processSimilarity(List<PokerLearningExample> examplesForUse,
            LocalSituation current, AdviceCreator creator) {
        double similarityDegree = DEGREE_OF_SIMILARITY[current.getStreet()];
        List<PokerLearningExample> similarSituations =
                LEManager.getSimilarSituations(examplesForUse, current,
                similarityDegree, new LocalSituationDistance());
        if (similarSituations.size() >= MIN_COUNT_OF_SIMILAR_SITUATIONS) {
            return Advice.create(creator, LEManager.getAverageOutput(similarSituations));
        }
        return null;
    }
}
