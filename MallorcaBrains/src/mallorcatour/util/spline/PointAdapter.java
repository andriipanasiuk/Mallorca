/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.util.spline;

import model.Point;

/**
 *
 * @author Andrew
 */
public class PointAdapter extends Point {

    public PointAdapter(java.awt.Point point) {
        super(point.x, point.y);
    }
}
