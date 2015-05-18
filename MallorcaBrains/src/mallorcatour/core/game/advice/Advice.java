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
import mallorcatour.interfaces.IOutputInterpreter;
import mallorcatour.interfaces.IRandomizer;
import mallorcatour.util.UniformRandomizer;

/**
 *
 * @author Andrew
 */
public final class Advice implements IVector, Serializable {

    private final static long serialVersionUID = 7789283258312255030L;
    private static final String FOLD = "Fold: ";
    private static final String PASSIVE = "Passive: ";
    private static final String AGGRESSIVE = "Aggressive: ";
    private final static IOutputInterpreter<Advice> DEFAULT_CREATOR = new AdviceCreator();
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
    public static Advice valueOf(String toString) {
        int from1 = toString.indexOf(FOLD) + FOLD.length();
        int to1 = toString.indexOf("%", from1);
        int from2 = toString.indexOf(PASSIVE) + PASSIVE.length();
        int to2 = toString.indexOf("%", from2);
        int from3 = toString.indexOf(AGGRESSIVE) + AGGRESSIVE.length();
        int to3 = toString.indexOf("%", from3);
        int fold, passive, aggressive;
        fold = Integer.parseInt(toString.substring(from1, to1));
        passive = Integer.parseInt(toString.substring(from2, to2));
        aggressive = Integer.parseInt(toString.substring(from3, to3));
        return new Advice(fold, passive, aggressive);
    }

    public static Advice create(double... values) {
        return DEFAULT_CREATOR.create(values);
    }

    public static Advice create(IOutputInterpreter<Advice> creator, double... values) {
        return creator.create(values);
    }
    private int foldPercent;
    private int passivePercent;
    private int aggressivePercent;

    Advice(int foldPercent,
            int passivePercent,
            int aggressivePercent) {
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

    public double getFold() {
        return (double) getFoldPercent() / 100;
    }

    public double getPassive() {
        return (double) getPassivePercent() / 100;
    }

    public double getAggressive() {
        return (double) getAggressivePercent() / 100;
    }

    public Action getAction() {
        int random = (int) (randomizer.getRandom() * 100);
        if (getFoldPercent() > random) {
            return Action.foldAction();
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
