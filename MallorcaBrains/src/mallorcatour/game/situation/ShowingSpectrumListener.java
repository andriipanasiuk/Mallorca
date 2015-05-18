/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.game.situation;

import mallorcatour.frames.SpectrumFrame;
import mallorcatour.game.core.Spectrum;

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
