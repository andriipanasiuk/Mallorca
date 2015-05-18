/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.game.situation;

import java.util.List;

import mallorcatour.core.vector.BaseVector;
import mallorcatour.core.vector.VectorInterpreter;
import mallorcatour.interfaces.IInputInterpreter;

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
