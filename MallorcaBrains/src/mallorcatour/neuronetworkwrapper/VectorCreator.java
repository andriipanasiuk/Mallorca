/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.neuronetworkwrapper;

import mallorcatour.interfaces.IOutputInterpreter;
import mallorcatour.vector.IVector;

/**
 *
 * @author Andrew
 */
public class VectorCreator implements IOutputInterpreter<IVector> {

    public IVector create(double... output) {
        return new BaseVector(output);
    }
}
