/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.brains.neural;

import mallorcatour.brains.HavingStats;
import mallorcatour.brains.IAdvisor;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.advice.AdviceCreator;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.advice.SmartAdviceCreator;
import mallorcatour.core.game.advice.SmartPostflopAdviceCreator;
import mallorcatour.core.game.advice.SmartRiverAdviceCreator;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.game.interfaces.IPokerStats;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.core.game.situation.LocalSituationInterpreter;
import mallorcatour.tools.Log;

import org.neuroph.core.NeuralNetwork;

/**
 * 
 * Poker neural network that uses multi-layer perceptrons learned on hands from
 * one of PA bots. This NN does not use global parameters (see
 * IGlobalSituation). But before using of multi-layer perceptron this network
 * looking for the same situation from situation files. If there are few(count
 * of situations and the degree of similarity are defined in this class) same
 * situations result will be the average output of these situations. If there
 * are not it uses output of multi-layer perceptrons. As interpreter (see
 * IOutputInterpreter) of output vector and creating advice from it this class
 * uses SmartAdviceCreator for flop and turn and special advice creators for
 * preflop and river.
 * 
 * @author Andrew
 */
public class NeuralAdvisor implements IAdvisor {

	private final IPokerNeurals neurals;
	private final IPokerStats stats;
	private final String name;
	private NeuralNetwork<?> preflopNN, flopNN, turnNN, riverNN;
	private AdviceCreator adviceCreator;

	public NeuralAdvisor(IPokerNeurals nnStreaming, HavingStats stats, String name) {
		this(nnStreaming, stats, name, null);
	}

	public NeuralAdvisor(IPokerNeurals nnStreaming, HavingStats stats, String name, AdviceCreator adviceCreator) {
		this.neurals = nnStreaming;
		this.stats = stats.getStats();
		this.name = name;
		this.adviceCreator = adviceCreator;
		initNN();
	}

	private void initNN() {
		preflopNN = neurals.getPreflop();
		flopNN = neurals.getFlop();
		turnNN = neurals.getTurn();
		riverNN = neurals.getRiver();
		Log.d("BasePokerNN. Neural networks inited");
	}

	@Override
	public IAdvice getAdvice(LocalSituation situation, HoleCards cards, IPlayerGameInfo gameInfo) {
		NeuralNetwork<?> nnForUse = null;
		AdviceCreator adviceCreator = null;
		boolean canRaise = situation.canRaise();
		int street = situation.getStreet();
		switch (street) {
		case 0:
			nnForUse = preflopNN;
			adviceCreator = new SmartAdviceCreator();
			break;
		case 1:
			nnForUse = flopNN;
			adviceCreator = new SmartPostflopAdviceCreator();
			break;
		case 2:
			nnForUse = turnNN;
			adviceCreator = new SmartPostflopAdviceCreator();
			break;
		case 3:
			nnForUse = riverNN;
			adviceCreator = new SmartRiverAdviceCreator(situation.getPotOdds() == 0);
			break;
		default:
			throw new IllegalArgumentException("Illegal street: " + street);
		}
		if (this.adviceCreator != null) {
			adviceCreator = this.adviceCreator;
		}
		nnForUse.setInput(new LocalSituationInterpreter().createInput(situation));
		nnForUse.calculate();
		double[] advice = nnForUse.getOutput();
		Advice result = Advice.create(adviceCreator, canRaise, advice);
		return result;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public IPokerStats getStats() {
		return stats;
	}

}
