/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.game.advice;

import mallorcatour.interfaces.IOutputInterpreter;
import mallorcatour.util.Log;

/**
 * Default Advice creator.
 * @author Andrew
 */
public class AdviceCreator implements IOutputInterpreter<Advice> {

    public Advice create(double... output) {
        if (output.length != 3) {
            throw new IllegalArgumentException("Output dimension must equals 3");
        }
        double foldPercent = output[0];
        double passivePercent = output[1];
        double aggressivePercent = output[2];
        double sumD = foldPercent + passivePercent + aggressivePercent;
        foldPercent /= sumD;
        passivePercent /= sumD;
        aggressivePercent /= sumD;

        int foldCount = (int) (foldPercent * 100);
        int passiveCount = (int) (passivePercent * 100);
        int aggressiveCount = (int) (aggressivePercent * 100);
        int sum = foldCount + passiveCount + aggressiveCount;
        switch (sum - 100) {
            case 3:
                foldCount--;
                passiveCount--;
                aggressiveCount--;
                break;
            case 2:
                passiveCount--;
                aggressiveCount--;
                break;
            case 1:
                aggressiveCount--;
                break;
            case 0:
                break;
            case -1:
                aggressiveCount++;
                break;
            case -2:
                passiveCount++;
                aggressiveCount++;
                break;
            case -3:
                foldCount++;
                passiveCount++;
                aggressiveCount++;
                break;
            default:
                Log.d("SOMETHING WRONG in advice creator: "
                        + "fold: " + foldCount + " passive: " + passiveCount
                        + " aggressive: " + aggressiveCount);
                return new Advice(100, 0, 0);
        }
        return new Advice(foldCount, passiveCount, aggressiveCount);
    }
}
