/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.grandtorino.villainstat;

import java.util.ArrayList;
import java.util.List;
import mallorcatour.game.advice.Advice;
import mallorcatour.game.core.Action;
import mallorcatour.game.situation.IDecisionListener;
import mallorcatour.game.situation.LocalSituation;
import mallorcatour.neuronetworkwrapper.LEManager;
import mallorcatour.neuronetworkwrapper.PokerLearningExample;
import mallorcatour.util.Log;
import mallorcatour.util.Pair;
import mallorcatour.util.SerializatorUtils;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.MomentumBackpropagation;

/**
 *
 * @author Andrew
 */
public class PAVillainObserver implements IVillainObserver, IDecisionListener {

    private final static int ITERATION_COUNT = 15000;
    private final static int LEARN_PREFLOP_EVERY_HAND = 5;
    private VillainStatistics currentVillain;
    private final IVillainListener listener;
    private final String DEBUG;
    private String name;

    public PAVillainObserver(IVillainListener listener, String debug) {
        this.listener = listener;
        this.DEBUG = debug;
    }

    public void setVillainName(String name) {
        this.name = name;
        if (currentVillain == null || !currentVillain.getName().equals(name)) {
            Log.f(DEBUG, "Villain statistics created. Name: " + name);
            currentVillain = new VillainStatistics(name);
        }
        listener.onVillainKnown(true);
        listener.onVillainChange(currentVillain);
    }

    public void endSession() {
    }

    public void onHandPlayed(long handNumber) {
        currentVillain.addHandPlayed();
        if (currentVillain.getHandsCount() % LEARN_PREFLOP_EVERY_HAND == 0) {
            learnPreflopNN(currentVillain);
        }
        listener.onVillainChange(currentVillain);
        SerializatorUtils.save(name + ".stat", currentVillain);
    }

    public boolean isVillainKnown() {
        return true;
    }

    public VillainStatistics getCurrentVillain() {
        return currentVillain;
    }

    private void learnPreflopNN(VillainStatistics villainStatistics) {
        NeuralNetwork nn = new MultiLayerPerceptron(7, 10, 3);
        List<PokerLearningExample> examples = villainStatistics.getExamples();
        List<PokerLearningExample> preflopExamples = new ArrayList<PokerLearningExample>();
        for (PokerLearningExample example : examples) {
            if (example.getSituation().getStreet() == LocalSituation.PREFLOP) {
                preflopExamples.add(example);
            }
        }
        nn.learnInSameThread(LEManager.createTrainingSet(preflopExamples),
                new MomentumBackpropagation(ITERATION_COUNT / preflopExamples.size()));
        villainStatistics.setPreflopNeuralNetwork(nn);
        villainStatistics.setPreflopLearned(true);
    }

    private Advice createFromAction(Action action) {
        double[] values = new double[3];
        if (action.isFold()) {
            values[0] = 1;
        } else if (action.isPassive()) {
            values[1] = 1;
        } else if (action.isAggressive()) {
            values[2] = 1;
        } else {
            throw new RuntimeException();
        }
        return Advice.create(values);
    }

    public void onDecided(LocalSituation situation, Action action) {
        currentVillain.addPokerLearningExample(new PokerLearningExample(situation, createFromAction(action)));
        processStats(situation, action);
    }

    private void processStats(LocalSituation situation, Action action) {
        if (situation.getStreet() != LocalSituation.PREFLOP) {
            if (action.isAggressive()) {
                currentVillain.addAggressionFrequencyInfo(new Pair<Integer, Integer>(1, 1));
            } else {
                currentVillain.addAggressionFrequencyInfo(new Pair<Integer, Integer>(0, 1));
            }
//            Log.f(DEBUG, "PAVillainObserver. processStats() Aggr freq: " + currentVillain.getAggressionFrequency());
//            Log.f(DEBUG, "PAVillainObserver. Villain situation: " + situation);
//            Log.f(DEBUG, "PAVillainObserver. Villain action: " + action);
            if (situation.getPotOdds() != 0) {
                if (action.isFold()) {
                    currentVillain.addFoldInfo(new Pair<Integer, Integer>(1, 1));
//                    Log.f(DEBUG, "PAVillainObserver. Villain folded. Fold freq: " + currentVillain.getFoldFrequency());
                } else {
                    currentVillain.addFoldInfo(new Pair<Integer, Integer>(0, 1));
                }
            }
        }
    }
}