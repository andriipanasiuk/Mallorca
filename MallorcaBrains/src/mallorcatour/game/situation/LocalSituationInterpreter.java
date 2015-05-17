/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.game.situation;

import java.util.List;
import mallorcatour.interfaces.IInputInterpreter;
import mallorcatour.neuronetworkwrapper.BaseVector;
import mallorcatour.neuronetworkwrapper.VectorInterpreter;

/**
 *
 * @author Andrew
 */
public class LocalSituationInterpreter implements IInputInterpreter<LocalSituation> {

    private static final VectorInterpreter DEFAULT_VECTOR_INTERPRETER =
            new VectorInterpreter(true);

    public double[] createInput(LocalSituation situation) {
        List<Number> values = situation.getValues();
        return DEFAULT_VECTOR_INTERPRETER.createInput(new BaseVector(values));
    }
}
