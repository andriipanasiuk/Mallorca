/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.game.situation;

import mallorcatour.core.spectrum.Spectrum;
import mallorcatour.frames.SpectrumFrame;

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
