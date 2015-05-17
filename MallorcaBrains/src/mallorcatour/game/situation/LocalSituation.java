/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.game.situation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import mallorcatour.core.bot.LimitType;
import mallorcatour.vector.IVector;
import mallorcatour.vector.VectorUtils;

/**
 *
 * @author Andrew
 */
public class LocalSituation implements IVector, Serializable {

    public static final double DEFAULT_POTENTIAL = 0.1;
    private final static long serialVersionUID = -762999739785198026L;
    private final int street;
    public static final int PREFLOP = 0;
    public static final int FLOP = 1;
    public static final int TURN = 2;
    public static final int RIVER = 3;
    private double strength = -1;
    private boolean isOnButton;
    private boolean canRaise;
    private double heroAggresionFrequency = -1;
    private double opponentAggresionFrequency = -1;
    private int heroAggresionActionCount = 0;


	private int villainAggresionActionCount = 0;
    private boolean wasIPreviousAggresive;
    private boolean wasOpponentPreviousAggresive;
    private double stackProportion = -1;
    /**
     * only for No Limit
     */
    private double potToStackOdds = -1;
    /**
     * only for Fixed Limit holdem on postflop
     */
    private double FLPotSize;
    private double potOdds = -1;
    private double positivePotential = DEFAULT_POTENTIAL;
    private double negativePotential = DEFAULT_POTENTIAL;
    private LimitType limitType;

    public int getHeroAggresionActionCount() {
    	return heroAggresionActionCount;
    }
    
    public void setHeroAggresionActionCount(int heroAggresionActionCount) {
    	this.heroAggresionActionCount = heroAggresionActionCount;
    }
    
    public int getVillainAggresionActionCount() {
    	return villainAggresionActionCount;
    }
    
    public void setVillainAggresionActionCount(int opponentAggresionActionCount) {
    	this.villainAggresionActionCount = opponentAggresionActionCount;
    }

    public int getStreet() {
        return street;
    }

    public LocalSituation(int street, LimitType gameType) {
        this.street = street;
        this.limitType = gameType;
    }

    /**
     * Constructor for creating LocalSituation has been read from file.
     * @param vector
     * @param street
     */
    public LocalSituation(IVector vector, int street) {
        this.street = street;
        int i = 0;
        List<Number> values = vector.getValues();
        strength = values.get(i++).doubleValue();
        isOnButton = values.get(i++).doubleValue() == 1;
        heroAggresionFrequency = values.get(i++).doubleValue();
        opponentAggresionFrequency = values.get(i++).doubleValue();
        heroAggresionActionCount = values.get(i++).intValue();
        villainAggresionActionCount = values.get(i++).intValue();
        wasIPreviousAggresive = values.get(i++).doubleValue() == 1;
        wasOpponentPreviousAggresive = values.get(i++).doubleValue() == 1;
        if (limitType == LimitType.NO_LIMIT) {
            potToStackOdds = values.get(i++).doubleValue();
        }
		potOdds = values.get(i++).doubleValue();
		positivePotential = values.get(i++).doubleValue();
		negativePotential = values.get(i++).doubleValue();
    }

    public LocalSituation(LocalSituation other) {
        this.limitType = other.limitType;
        this.street = other.street;
        this.strength = other.strength;
        this.isOnButton = other.isOnButton;
        this.wasIPreviousAggresive = other.wasIPreviousAggresive;
        this.heroAggresionFrequency = other.heroAggresionFrequency;
        this.wasOpponentPreviousAggresive = other.wasOpponentPreviousAggresive;
        this.opponentAggresionFrequency = other.opponentAggresionFrequency;
        this.villainAggresionActionCount = other.villainAggresionActionCount;
        this.heroAggresionActionCount = other.heroAggresionActionCount;
        this.potOdds = other.potOdds;
        this.potToStackOdds = other.potToStackOdds;
        this.FLPotSize = other.FLPotSize;
        this.stackProportion = other.stackProportion;
        this.positivePotential = other.positivePotential;
        this.negativePotential = other.negativePotential;
    }

    /**
     * Method for write LocalSituation as list of numbers.
     * Used for write it to file.
     */
    public List<Number> getValues() {
        List<Number> result = new ArrayList<Number>();
        result.add(getStrength());
        result.add(isHeroOnButton() ? 1 : 0);
        result.add(getHeroAggresionFrequency());
        result.add(getVillainAggresionFrequency());
        result.add(getHeroAggresionActionCount());
        result.add(getVillainAggresionActionCount());
        result.add(wasHeroPreviousAggresive() ? 1 : 0);
        result.add(wasVillainPreviousAggresive() ? 1 : 0);
        if (limitType == LimitType.FIXED_LIMIT && street != PREFLOP) {
            result.add(getFLPotSize());
        }
        if (limitType == LimitType.NO_LIMIT) {
            result.add(getPotToStackOdds());
        }
		result.add(getPotOdds());
		result.add(getPositivePotential());
		result.add(getNegativePotential());
		return result;
    }

    @Override
    public String toString() {
        return VectorUtils.toString(this);
    }

    public double getStrength() {
        return strength;
    }

    public void setStrength(double strength) {
        this.strength = strength;
    }

    public void setFLPotSize(double FLPotSize) {
        this.FLPotSize = FLPotSize;
    }

    public double getFLPotSize() {
        return FLPotSize;
    }

    public boolean canRaise() {
        return canRaise;
    }

    public void canRaise(boolean canRaise) {
        this.canRaise = canRaise;
    }

    public boolean isHeroOnButton() {
        return isOnButton;
    }

    public void isOnButton(boolean isOnButton) {
        this.isOnButton = isOnButton;
    }

    /**
     * @return the localAggresion
     */
    public double getHeroAggresionFrequency() {
        return heroAggresionFrequency;
    }

    /**
     * @param localAggresion the localAggresion to set
     */
    public void setLocalAggresion(double localAggresion) {
        this.heroAggresionFrequency = localAggresion;
    }

    /**
     * @return the localOpponentAggresion
     */
    public double getVillainAggresionFrequency() {
        return opponentAggresionFrequency;
    }

    /**
     * @param localOpponentAggresion the localOpponentAggresion to set
     */
    public void setLocalOpponentAggresion(double localOpponentAggresion) {
        this.opponentAggresionFrequency = localOpponentAggresion;
    }

    /**
     * @return the wasOpponentPreviousAggresive
     */
    public boolean wasVillainPreviousAggresive() {
        return wasOpponentPreviousAggresive;
    }

    /**
     * @param wasOpponentPreviousAggresive the wasOpponentPreviousAggresive to set
     */
    public void wasOpponentPreviousAggresive(boolean wasOpponentPreviousAggresive) {
        this.wasOpponentPreviousAggresive = wasOpponentPreviousAggresive;
    }

    /**
     * @return the wasIPreviousAggresive
     */
    public boolean wasHeroPreviousAggresive() {
        return wasIPreviousAggresive;
    }

    /**
     * @param wasIPreviousAggresive the wasIPreviousAggresive to set
     */
    public void wasHeroPreviousAggresive(boolean wasIPreviousAggresive) {
        this.wasIPreviousAggresive = wasIPreviousAggresive;
    }

    /**
     * @return the potOdds
     */
    public double getPotOdds() {
        return potOdds;
    }

    /**
     * @param potOdds the potOdds to set
     */
    public void setPotOdds(double potOdds) {
        this.potOdds = potOdds;
    }

    /**
     * @return the potToStackOdds
     */
    public double getPotToStackOdds() {
        return potToStackOdds;
    }

    /**
     * @param potToStackOdds the potToStackOdds to set
     */
    public void setPotToStackOdds(double potToStackOdds) {
        this.potToStackOdds = potToStackOdds;
    }

    /**
     * @return the positivePotential
     */
    public double getPositivePotential() {
        return positivePotential;
    }

    /**
     * @param positivePotential the positivePotential to set
     */
    public void setPositivePotential(double positivePotential) {
        this.positivePotential = positivePotential;
    }

    /**
     * @return the negativePotential
     */
    public double getNegativePotential() {
        return negativePotential;
    }

    /**
     * @param negativePotential the negativePotential to set
     */
    public void setNegativePotential(double negativePotential) {
        this.negativePotential = negativePotential;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LocalSituation)) {
            return false;
        }
        final LocalSituation other = (LocalSituation) obj;
        if (this.hashCode() != other.hashCode()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return VectorUtils.toString(this).hashCode();
    }

    /**
     * @return the stackProportion
     */
    public double getStackProportion() {
        return stackProportion;
    }

    /**
     * @param stackProportion the stackProportion to set
     */
    public void setStackProportion(double stackProportion) {
        this.stackProportion = stackProportion;
    }

    public LimitType getGameType() {
        return limitType;
    }
}
