/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.bot.modeller;

import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.advice.Advice;
import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.core.spectrum.Spectrum;
import mallorcatour.core.vector.VectorInterpreter;
import mallorcatour.grandtorino.spectrum.SpectrumResources;
import org.neuroph.core.NeuralNetwork;

/**
 *
 * @author Andrew
 */
public class PreflopSpectrumModeller {

    public Advice getAdvice(final LocalSituation situation, HoleCards cards,
            NeuralNetwork preflopNN) {
        if (situation.getStreet() != LocalSituation.PREFLOP) {
            throw new IllegalArgumentException();
        }
        //preparing input
        double[] input = new VectorInterpreter(true).createInput(situation);
        input[0] = -1;

        preflopNN.setInput(input);
        preflopNN.calculate();
        double[] averageAction = preflopNN.getOutput();
        Advice advice = Advice.create(averageAction);
        double[] result = new double[3];

        Spectrum aggressiveSpectrum = getSpectrum(advice.getAggressivePercent());
        result[2] = aggressiveSpectrum.getWeight(cards);
        Spectrum passiveSpectrum = getSpectrum(advice.getAggressivePercent() + advice.getPassivePercent());
        double passive = passiveSpectrum.getWeight(cards);
        result[1] = passive - result[2];
        result[0] = 1 - passive;
        return Advice.create(result);
    }

    private Spectrum getSpectrum(int percent) {
        return SpectrumResources.getSpectrum(percent);
    }
}
