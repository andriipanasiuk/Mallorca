/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.brains.neural;

import java.util.List;

import mallorcatour.core.game.situation.LocalSituation;
import mallorcatour.core.vector.BaseVector;
import mallorcatour.core.vector.IInputInterpreter;
import mallorcatour.neural.core.VectorInterpreter;

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
