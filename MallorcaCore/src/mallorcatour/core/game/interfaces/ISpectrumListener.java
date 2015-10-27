/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.game.interfaces;

import mallorcatour.core.spectrum.Spectrum;

/**
 *
 * @author Andrew
 */
public interface ISpectrumListener {

    public static final ISpectrumListener EMPTY = new ISpectrumListener() {

        public void onSpectrumChanged(Spectrum spectrum, String log) {
            //do nothing
        }
    };

    public void onSpectrumChanged(Spectrum spectrum, String log);
}
