/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.neural.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import mallorcatour.core.game.Hand;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.advice.AdviceHolder;
import mallorcatour.core.game.advice.IAdvice;
import mallorcatour.core.game.situation.HandState;
import mallorcatour.core.game.situation.HandStateHolder;

/**
 *
 * @author Andrew
 */
public class PokerLearningExample implements HandStateHolder, AdviceHolder, ILearningExample<HandState, IAdvice>, Serializable {

	private final static long serialVersionUID = 1L;
    private HandState situation;
    private IAdvice advice;
    private Hand hand;

    public PokerLearningExample() {
        super();
    }

    public PokerLearningExample(HandState situation) {
        this.situation = situation;
    }

    public PokerLearningExample(HandState situation, IAdvice advice) {
        this.situation = situation;
        this.advice = advice;
    }

    /**
     * @return the situation
     */
    @Override
    public HandState getSituation() {
        return getInput();
    }

    /**
     * @param situation the situation to set
     */
    public void setSituation(HandState situation) {
        this.situation = situation;
    }

    /**
     * @return the advice
     */
    @Override
    public IAdvice getAdvice() {
        return getOutput();
    }

    /**
     * @param advice the advice to set
     */
    public void setAdvice(Advice advice) {
        this.advice = advice;
    }

    /**
     * @return the hand
     */
    public Hand getHand() {
        return hand;
    }

    /**
     * @param hand the hand to set
     */
    public void setHand(Hand hand) {
        this.hand = hand;
    }

    @Override
    public String toString() {
        return LearningExample.toString(situation, advice);
    }

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		// TODO check correct work
		in.defaultReadObject();
	}

	@Override
	public HandState getInput() {
		return situation;
	}

	@Override
	public IAdvice getOutput() {
		return advice;
	}

	@Override
	public int getInputDimension() {
        return getInput().getValues().size();
    }

    @Override
	public int getOutputDimension() {
        return getOutput().getValues().size();
    }
}
