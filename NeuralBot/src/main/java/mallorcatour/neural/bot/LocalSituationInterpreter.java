/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.neural.bot;

import java.util.List;

import mallorcatour.core.game.state.HandState;
import mallorcatour.core.vector.BaseVector;
import mallorcatour.core.vector.IInputInterpreter;
import mallorcatour.neural.core.VectorInterpreter;

/**
 *
 * @author Andrew
 */
public class LocalSituationInterpreter implements IInputInterpreter<HandState> {

    private static final VectorInterpreter DEFAULT_VECTOR_INTERPRETER =
            new VectorInterpreter(true);

    public double[] createInput(HandState situation) {
        List<Number> values = situation.getValues();
        return DEFAULT_VECTOR_INTERPRETER.createInput(new BaseVector(values));
    }
}
