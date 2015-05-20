/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.frames;

import mallorcatour.bot.interfaces.ISpectrumListener;
import mallorcatour.core.spectrum.Spectrum;

/**
 *
 * @author Andrew
 */
public class ShowingSpectrumListener implements ISpectrumListener {

    private boolean show = true;

    public void setShow(boolean show) {
        this.show = show;
    }

    public void onSpectrumChanged(Spectrum spectrum, String log) {
        if (show) {
            new SpectrumFrame(spectrum, log).setVisible(true);
        }
    }
}
