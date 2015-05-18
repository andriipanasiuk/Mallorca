/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.neuronetworkwrapper;

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
public class PokerLearningExample extends LearningExample implements Serializable {

    private final static long serialVersionUID = 1L;
    private LocalSituation situation;
    private Advice advice;
    private Hand hand;

    public PokerLearningExample() {
        super();
    }

    public PokerLearningExample(LocalSituation situation) {
        super(situation);
        this.situation = situation;
    }

    public PokerLearningExample(LocalSituation situation, Advice advice) {
        super(situation, advice);
        this.situation = situation;
        this.advice = advice;
    }

    /**
     * @return the situation
     */
    public LocalSituation getSituation() {
        return situation;
    }

    /**
     * @param situation the situation to set
     */
    public void setSituation(LocalSituation situation) {
        this.situation = situation;
        inputVector = situation;
    }

    /**
     * @return the advice
     */
    public Advice getAdvice() {
        return advice;
    }

    /**
     * @param advice the advice to set
     */
    public void setAdvice(Advice advice) {
        this.advice = advice;
        outputVector = advice;
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
        return new LearningExample(situation, advice).toString();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        inputVector = situation;
        outputVector = advice;
    }
}
