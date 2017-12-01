/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.game.state;

import mallorcatour.core.game.interfaces.ISpectrumListener;
import mallorcatour.core.spectrum.Spectrum;
import mallorcatour.tools.DateUtils;
import mallorcatour.tools.SerializatorUtils;

/**
 *
 * @author Andrew
 */
public class SavingSpectrumListener implements ISpectrumListener {

    public void onSpectrumChanged(Spectrum spectrum, String log) {
        SerializatorUtils.save("spectrum_" + DateUtils.getDate(false) + ".spectr", spectrum);
    }
}
