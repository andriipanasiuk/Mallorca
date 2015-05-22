/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.recognizer;

import java.awt.Dimension;
import java.awt.Point;

/**
 *
 * @author Andrew
 */
public interface ITableListener {

    public static final ITableListener EMPTY = new ITableListener() {

        public void onTableVisible(Point topLeftPoint, Dimension dimension) {
        }

        public void onTableInvisible() {
        }
    };

    void onTableVisible(Point topLeftPoint, Dimension dimension);

    void onTableInvisible();
}
