/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.game.advice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import mallorcatour.core.game.Action;
import mallorcatour.core.vector.IVector;
import mallorcatour.interfaces.IAdviceInterpreter;
import mallorcatour.util.IRandomizer;
import mallorcatour.util.UniformRandomizer;

/**
 *
 * @author Andrew
 */
public final class Advice implements IAdvice, IVector, Serializable {

	private final static long serialVersionUID = 7789283258312255030L;
    private static final String FOLD = "Fold: ";
    private static final String PASSIVE = "Passive: ";
    private static final String AGGRESSIVE = "Aggressive: ";
    private final static IAdviceInterpreter DEFAULT_CREATOR = new AdviceCreator();
    private final transient IRandomizer randomizer = new UniformRandomizer();

    public List<Number> getValues() {
        List<Number> result = new ArrayList<Number>();
        result.add(getFold());
        result.add(getPassive());
        result.add(getAggressive());
        return result;
    }

    /**
     * @see toString() method of this class
     */
    public static Advice valueOf(String adviceStr) {
        int from1 = adviceStr.indexOf(FOLD) + FOLD.length();
        int to1 = adviceStr.indexOf("%", from1);
        int from2 = adviceStr.indexOf(PASSIVE) + PASSIVE.length();
        int to2 = adviceStr.indexOf("%", from2);
        int from3 = adviceStr.indexOf(AGGRESSIVE) + AGGRESSIVE.length();
        int to3 = adviceStr.indexOf("%", from3);
        int fold, passive, aggressive;
        fold = Integer.parseInt(adviceStr.substring(from1, to1));
        passive = Integer.parseInt(adviceStr.substring(from2, to2));
        aggressive = Integer.parseInt(adviceStr.substring(from3, to3));
        return new Advice(fold, passive, aggressive);
    }

    public static Advice create(double... values) {
        return DEFAULT_CREATOR.create(true, values);
    }

    public static Advice create(IAdviceInterpreter creator, boolean canRaise, double... values) {
        return creator.create(canRaise ,values);
    }
    private int foldPercent;
    private int passivePercent;
    private int aggressivePercent;

	public Advice(int foldPercent, int passivePercent,            int aggressivePercent) {
        if (foldPercent < 0 || passivePercent < 0 || aggressivePercent < 0) {
            throw new IllegalArgumentException("Percents must be form 0 to 100");
        }
        if (foldPercent + passivePercent + aggressivePercent != 100) {
            throw new IllegalArgumentException("Percents in summary must be 100");
        }
        this.foldPercent = foldPercent;
        this.passivePercent = passivePercent;
        this.aggressivePercent = aggressivePercent;
    }

    @Override
    public double getFold() {
        return (double) getFoldPercent() / 100;
    }

    @Override
    public double getPassive() {
        return (double) getPassivePercent() / 100;
    }

    @Override
    public double getAggressive() {
        return (double) getAggressivePercent() / 100;
    }

    @Override
    public Action getAction() {
        int random = (int) (randomizer.getRandom() * 100);
        if (getFoldPercent() > random) {
            return Action.fold();
        } else if (getPassivePercent() + getFoldPercent() > random) {
            return Action.passive();
        } else {
            return Action.aggressive();
        }
    }

    @Override
    public String toString() {
        return "[" + FOLD + getFoldPercent() + "%; "
                + PASSIVE + getPassivePercent() + "%; "
                + AGGRESSIVE + getAggressivePercent() + "%]";
    }

    /**
     * @return the foldPercent
     */
    public int getFoldPercent() {
        return foldPercent;
    }

    /**
     * @return the passivePercent
     */
    public int getPassivePercent() {
        return passivePercent;
    }

    /**
     * @return the aggressivePercent
     */
    public int getAggressivePercent() {
        return aggressivePercent;
    }
}
