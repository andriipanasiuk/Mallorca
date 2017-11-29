/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.neural.bot;

import org.neuroph.core.NeuralNetwork;

import mallorcatour.advice.creator.SmartAdviceCreator;
import mallorcatour.advice.creator.SmartPostflopAdviceCreator;
import mallorcatour.advice.creator.SmartRiverAdviceCreator;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.advice.AdviceCreator;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.modeller.NeuralAdvisor;
import mallorcatour.stats.IPokerStats;
import mallorcatour.tools.Log;

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
public class NeuralAdvisorImpl implements NeuralAdvisor {

	private final IPokerNeurals neurals;
	private final IPokerStats stats;
	private final String name;
	private NeuralNetwork<?> preflopNN;
	private NeuralNetwork<?> flopNN;
	private NeuralNetwork<?> turnNN;
	private NeuralNetwork<?> riverNN;
	private AdviceCreator adviceCreator;

	public NeuralAdvisorImpl(IPokerNeurals neurals, IPokerStats stats, String name) {
		this(neurals, stats, name, null);
	}

	public NeuralAdvisorImpl(IPokerNeurals neurals, IPokerStats stats, String name, AdviceCreator adviceCreator) {
		this.neurals = neurals;
		this.stats = stats;
		this.name = name;
		this.adviceCreator = adviceCreator;
		initNN();
	}

	private void initNN() {
		preflopNN = neurals.getPreflop();
		flopNN = neurals.getFlop();
		turnNN = neurals.getTurn();
		riverNN = neurals.getRiver();
		Log.d("Neural network " + neurals.getName() + " has been inited");
	}

	@Override
	public IAdvice getAdvice(LocalSituation situation, HoleCards cards, IPlayerGameInfo gameInfo) {
		NeuralNetwork<?> neural;
		AdviceCreator adviceCreator;
		boolean canRaise = situation.canRaise();
		int street = situation.getStreet();
		switch (street) {
		case PokerStreet.PREFLOP_VALUE:
			neural = preflopNN;
			adviceCreator = new SmartAdviceCreator();
			break;
		case PokerStreet.FLOP_VALUE:
			neural = flopNN;
			adviceCreator = new SmartPostflopAdviceCreator();
			break;
		case PokerStreet.TURN_VALUE:
			neural = turnNN;
			adviceCreator = new SmartPostflopAdviceCreator();
			break;
		case PokerStreet.RIVER_VALUE:
			neural = riverNN;
			adviceCreator = new SmartRiverAdviceCreator(situation.getPotOdds() == 0);
			break;
		default:
			throw new IllegalArgumentException("Illegal street: " + street);
		}
		if (this.adviceCreator != null) {
			adviceCreator = this.adviceCreator;
		}
		neural.setInput(new LocalSituationInterpreter().createInput(situation));
		neural.calculate();
		double[] advice = neural.getOutput();
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
