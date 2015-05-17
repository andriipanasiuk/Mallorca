/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.game.hand;

import mallorcatour.game.core.PokerStreet;
import mallorcatour.game.situation.LocalSituation;

/**
 *
 * @author Andrew
 */
public class BaseHandEquities implements HandEquities {

    private double preflopEquity;
    private double flopEquity;
    private double flopPositive;
    private double flopNegative;
    private double turnEquity;
    private double turnPositive;
    private double turnNegative;
    private double riverEquity;

    public BaseHandEquities(double preflopEquity,
            double flopEquity,
            double flopPositiveEquity,
            double flopNegativeEquity,
            double turnEquity,
            double turnPositiveEquity,
            double turnNegativeEquity,
            double riverEquity) {
        this.preflopEquity = preflopEquity;
        this.flopEquity = flopEquity;
        this.turnEquity = turnEquity;
        this.riverEquity = riverEquity;
        this.flopPositive = flopPositiveEquity;
        this.flopNegative = flopNegativeEquity;
        this.turnPositive = turnPositiveEquity;
        this.turnNegative = turnNegativeEquity;
    }

    private double returnWithCheck(double equity, int street) {
        if (equity != -1) {
            return equity;
        }
        throw new IllegalArgumentException("No street "
                + PokerStreet.valueOf(street) + " in this hand");
    }

    public double getPreflopEquity() {
        return returnWithCheck(preflopEquity, LocalSituation.PREFLOP);
    }

    public double getFlopEquity() {
        return returnWithCheck(flopEquity, LocalSituation.FLOP);
    }

    public double getTurnEquity() {
        return returnWithCheck(turnEquity, LocalSituation.TURN);
    }

    public double getRiverEquity() {
        return returnWithCheck(riverEquity, LocalSituation.RIVER);
    }

    public double getFlopPositivePotential() {
        return returnWithCheck(flopPositive, LocalSituation.FLOP);
    }

    public double getFlopNegativePotential() {
        return returnWithCheck(flopNegative, LocalSituation.FLOP);
    }

    public double getTurnPositivePotential() {
        return returnWithCheck(turnPositive, LocalSituation.TURN);
    }

    public double getTurnNegativePotential() {
        return returnWithCheck(turnNegative, LocalSituation.TURN);
    }
}
