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
import mallorcatour.core.game.situation.LocalSituation;

/**
 *
 * @author Andrew
 */
public class PokerLearningExample implements ILearningExample<LocalSituation, Advice>, Serializable {

    private final static long serialVersionUID = 1L;
    private LocalSituation situation;
    private Advice advice;
    private Hand hand;

    public PokerLearningExample() {
        super();
    }

    public PokerLearningExample(LocalSituation situation) {
        this.situation = situation;
    }

    public PokerLearningExample(LocalSituation situation, Advice advice) {
        this.situation = situation;
        this.advice = advice;
    }

    /**
     * @return the situation
     */
    public LocalSituation getSituation() {
        return getInput();
    }

    /**
     * @param situation the situation to set
     */
    public void setSituation(LocalSituation situation) {
        this.situation = situation;
    }

    /**
     * @return the advice
     */
    public Advice getAdvice() {
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
	public LocalSituation getInput() {
		return situation;
	}

	@Override
	public Advice getOutput() {
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
