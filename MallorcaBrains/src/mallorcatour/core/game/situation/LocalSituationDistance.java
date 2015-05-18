/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.game.situation;

import mallorcatour.core.vector.EuclidDistance;
import mallorcatour.core.vector.IVector;
import mallorcatour.interfaces.IDistanceCalculator;

/**
 *
 * @author Andrew
 */
public class LocalSituationDistance implements IDistanceCalculator<LocalSituation> {

    private final double MAX_POTENTIAL = 0.4;
    private static final IDistanceCalculator<IVector> DEFAULT_VECTOR_DISTANCE =
            new EuclidDistance();

    public double getDistance(LocalSituation one, LocalSituation other) {
        if (one.getStreet() != other.getStreet()) {
            throw new IllegalArgumentException("There are situations from "
                    + "different stages.");
        }
        int street = one.getStreet();
        if (street == LocalSituation.FLOP || street == LocalSituation.TURN) {
            LocalSituation local1 = new LocalSituation(one);
            LocalSituation local2 = new LocalSituation(other);
            double pPo11 = local1.getPositivePotential();
            double pPo12 = local2.getPositivePotential();
            double nPo11 = local1.getNegativePotential();
            double nPo12 = local2.getNegativePotential();

            if (pPo11 != 1) {
                local1.setPositivePotential(pPo11 * (1d / MAX_POTENTIAL));
            }
            if (pPo12 != 1) {
                local2.setPositivePotential(pPo12 * (1d / MAX_POTENTIAL));
            }
            if (nPo11 != 1) {
                local1.setNegativePotential(nPo11 * (1d / MAX_POTENTIAL));
            }
            if (nPo12 != 1) {
                local2.setNegativePotential(nPo12 * (1d / MAX_POTENTIAL));
            }
            return DEFAULT_VECTOR_DISTANCE.getDistance(local1, local2);
        } else {
            return DEFAULT_VECTOR_DISTANCE.getDistance(one, other);
        }

    }
}
