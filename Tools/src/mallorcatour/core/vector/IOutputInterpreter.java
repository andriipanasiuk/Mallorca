/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mallorcatour.core.vector;


/**
 *
 * @author Andrew
 */
public interface IOutputInterpreter<T> {

	T create(double... output);
}
