/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.vector;

import java.util.Comparator;
import java.util.List;
import mallorcatour.interfaces.IDistanceCalculator;
import mallorcatour.util.DoubleUtils;

/**
 *
 * @author Andrew
 */
public class VectorUtils {

    private static final IDistanceCalculator<IVector> DEFAULT_DISTANCE =
            new EuclidDistance();

    public static String toString(IVector vector) {
        return toString(vector, BaseVector.NUMBER_SEPARATOR);
    }

    public static String toString(IVector vector, String separator) {
        StringBuilder result = new StringBuilder();
        Double ZERO = Double.valueOf(0);
        Double ONE = Double.valueOf(1);

        for (Number value : vector.getValues()) {
            Double append = DoubleUtils.digitsAfterComma(value.doubleValue(), 3);
            if (Double.compare(ZERO, append) == 0) {
                result.append(0);
            } else if (Double.compare(ONE, append) == 0) {
                result.append(1);
            } else {
                result.append(append);
            }
            result.append(separator).append(" ");
        }
        result.delete(result.length() - 2, result.length());
        return result.toString();
    }

    public static Comparator<IVector> comparator(final int... columns) {
        return new Comparator<IVector>() {

            private int compareByColumn(IVector v1, IVector v2, int column) {
                double value1 = v1.getValues().get(column).doubleValue();
                double value2 = v2.getValues().get(column).doubleValue();

                if (Double.compare(value1, value2) > 0) {
                    return 1;
                } else if (Double.compare(value1, value2) < 0) {
                    return -1;
                } else {
                    return 0;
                }
            }

            public int compare(IVector vector1, IVector vector2) {
                int size = vector1.getValues().size();
                if (size != vector2.getValues().size()) {
                    throw new IllegalArgumentException("Vector dimensioms must "
                            + "be equals");
                }
                for (int column : columns) {
                    if (column >= size) {
                        throw new IllegalArgumentException("There is no such "
                                + "column in these vectors. Column: " + column);
                    }
                    int compare = compareByColumn(vector1, vector2, column);
                    if (compare > 0) {
                        return 1;
                    } else if (compare < 0) {
                        return -1;
                    }
                }
                return 0;
            }
        };
    }

    public static double getAverageMaxDistance(List<IVector> examples) {
        double result = 0;
        for (IVector vector1 : examples) {
            double max = Double.MIN_VALUE;
            for (IVector vector2 : examples) {
                double distance = getDistance(vector1, vector2);
                if (distance > max) {
                    max = distance;
                }
            }
            result += max;
        }
        //multiply on 2 cause of we have 2 included for cycles.
        result /= (2 * examples.size());
        return result;
    }

    public static double getDistance(IVector vector1, IVector vector2) {
        return DEFAULT_DISTANCE.getDistance(vector1, vector2);
    }

    public static double getDistance(IVector vector1, IVector vector2,
            IDistanceCalculator<IVector> distance) {
        return distance.getDistance(vector1, vector2);
    }
}
