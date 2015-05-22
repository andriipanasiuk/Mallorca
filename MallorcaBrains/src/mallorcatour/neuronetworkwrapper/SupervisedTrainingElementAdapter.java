/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.neuronetworkwrapper;

import org.neuroph.core.data.DataSetRow;

/**
 *
 * @author Andrew
 */
public class SupervisedTrainingElementAdapter extends DataSetRow {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4694537850146601716L;

	public SupervisedTrainingElementAdapter(LearningExample learningExample) {
        super(LEManager.createInputArray(learningExample.getInput()),
                LEManager.createInputArray(learningExample.getOutput()));
    }
}
