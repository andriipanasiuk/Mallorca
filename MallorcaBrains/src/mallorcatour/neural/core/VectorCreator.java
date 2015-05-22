/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.neural.core;

import mallorcatour.core.vector.BaseVector;
import mallorcatour.core.vector.IVector;
import mallorcatour.interfaces.IOutputInterpreter;

/**
 *
 * @author Andrew
 */
public class VectorCreator implements IOutputInterpreter<IVector> {

    public IVector create(double... output) {
        return new BaseVector(output);
    }
}
