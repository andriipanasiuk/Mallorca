/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.neuronetworkwrapper;

import org.neuroph.core.learning.SupervisedTrainingElement;

/**
 *
 * @author Andrew
 */
public class SupervisedTrainingElementAdapter extends SupervisedTrainingElement {

    public SupervisedTrainingElementAdapter(LearningExample learningExample) {
        super(LEManager.createInputArray(learningExample.getInput()),
                LEManager.createInputArray(learningExample.getOutput()));
    }
}
