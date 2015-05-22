/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.villainobserver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import mallorcatour.core.game.interfaces.IPokerStats;
import mallorcatour.neural.core.PokerLearningExample;
import mallorcatour.util.Pair;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.nnet.MultiLayerPerceptron;

/**
 *
 * @author Andrew
 */
public class VillainStatistics implements Serializable, IPokerStats {

    private static final long serialVersionUID = 1L;
    private String name;
    private int handsCount;
    private NeuralNetwork<?> preflopNN;
    private boolean isPreflopLearned;
    private List<PokerLearningExample> situations;
    // pair of (aggressive action) / (aggressive actions + call actions)
    private Pair<Integer, Integer> aggressionFactor;
    // pair of (aggressive action) / (all actions)
    private Pair<Integer, Integer> aggressionFrequency;
    // pair of (wtsd) / (all hands seen flop)
    private Pair<Integer, Integer> wtsd;
    // pair of (fold actions) / (all actions where hero can fold)
    private Pair<Integer, Integer> foldFrequency;

    public VillainStatistics(String name) {
        this.name = name;
        preflopNN = new MultiLayerPerceptron(7, 10, 3);
        situations = new ArrayList<PokerLearningExample>();
        aggressionFactor = new Pair<Integer, Integer>(0, 0);
        aggressionFrequency = new Pair<Integer, Integer>(0, 0);
        foldFrequency = new Pair<Integer, Integer>(0, 0);
        wtsd = new Pair<Integer, Integer>(0, 0);
        isPreflopLearned = false;
    }

    public NeuralNetwork<?> getPrefloNeuralNetwork() {
        return preflopNN;
    }

    public void addHandPlayed() {
        handsCount++;
    }

    public int getHandsCount() {
        return handsCount;
    }

    public void setPreflopNeuralNetwork(NeuralNetwork<?> nn) {
        this.preflopNN = nn;
    }

    public void setPreflopLearned(boolean learned) {
        this.isPreflopLearned = learned;
    }

    public boolean isPreflopLearned() {
        return isPreflopLearned;
    }

    public void addPokerLearningExample(PokerLearningExample example) {
        situations.add(example);
    }

    public void addPokerLearningExamples(List<PokerLearningExample> examples) {
        situations.addAll(examples);
    }

    public List<PokerLearningExample> getExamples() {
        return situations;
    }

    public void addAggressionInfo(Pair<Integer, Integer> aggression) {
        this.aggressionFactor.first += aggression.first;
        this.aggressionFactor.second += aggression.second;
    }

    public void addWtsdInfo(Pair<Integer, Integer> wtsd) {
        this.wtsd.first += wtsd.first;
        this.wtsd.second += wtsd.second;
    }

    public void addAggressionFrequencyInfo(Pair<Integer, Integer> aggressionFreq) {
        this.aggressionFrequency.first += aggressionFreq.first;
        this.aggressionFrequency.second += aggressionFreq.second;
    }

    public void addFoldInfo(Pair<Integer, Integer> fold) {
        this.foldFrequency.first += fold.first;
        this.foldFrequency.second += fold.second;
    }

    public double getAggressionFactor() {
        return (double) aggressionFactor.first / (aggressionFactor.second - aggressionFactor.first);
    }

    public double getWtsd() {
        return (double) wtsd.first / wtsd.second;
    }

    public double getFoldFrequency() {
        return (double) foldFrequency.first / foldFrequency.second;
    }

    public double getAggressionFrequency() {
        return (double) aggressionFrequency.first / aggressionFrequency.second;
    }

    public String getName() {
        return name;
    }
}
