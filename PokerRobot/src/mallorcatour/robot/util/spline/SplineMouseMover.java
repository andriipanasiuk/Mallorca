/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot.util.spline;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mallorcatour.robot.util.RobotUtils;
import mallorcatour.tools.IRandomizer;
import mallorcatour.tools.Log;
import mallorcatour.tools.UniformRandomizer;
import model.SmoothSpline;
import utils.MathUtils;

/**
 *
 * @author Andrew
 */
public class SplineMouseMover implements IMouseMover {

    private final int countOfSplinePoints = 3;
    private final double MAX_DEVIATION_OF_CURSOR = 120;
    private final double KOEFF_FOR_DEVIATION = MAX_DEVIATION_OF_CURSOR / (480 * 480);
    //px/second
    private final double DISTANCE_FOR_MAX_SPEED = 960;
    private final double MAX_SPEED = 2750;
    private final double MAX_SPEED_DEVIATION = 250;
    //in this part of distance we get max speed
    private final double PART_FOR_MAX_SPEED = 0.57;
    private final double GROW_SPEED_POWER = 0.5;
    private final double DECREASE_SPEED_POWER = 4.75;
    private final IRandomizer randomizer = new UniformRandomizer();

    public void moveTo(Robot robot, Point point) {
        Point startPoint = MouseInfo.getPointerInfo().getLocation();
        double distance = MathUtils.dist(new PointAdapter(startPoint),
                new PointAdapter(point));
        if (Math.abs(startPoint.x - point.x) < 10) {
            Point middlePoint = getPointOnDistance(startPoint, point, 100,
                    distance / 2);
            moveTo(robot, middlePoint);
            moveTo(robot, point);
        } else {
            long start = System.currentTimeMillis();
            List<Point> splinePoints = getPointsForSpline(startPoint, point);
            sortByX(splinePoints);
            SmoothSpline spline = SmoothSpline.buildSpline(getXArray(splinePoints),
                    getYArray(splinePoints), 1);

            double maxSpeed = getMaxSpeedForDistance(distance);

            double localDistance = 0;
            double newDistance;
            double speed;
            double distanceInOneStep;
            int y;
            double timeToWait = 0;
            for (int x = startPoint.x + 1; (startPoint.x > point.x) ? x >= point.x : x <= point.x; x -= Math.signum(startPoint.x - point.x)) {
                y = (int) spline.getValue(x);
                distanceInOneStep = distance / (Math.abs(point.x - startPoint.x));
                newDistance = distanceInOneStep + localDistance;
                localDistance = newDistance;
                if (localDistance > distance) {
                    break;
                }
                speed = getSpeed(localDistance, distance, maxSpeed);
                if (timeToWait > 1 || x == point.x) {
                    robot.delay((int) timeToWait);
                    timeToWait -= Math.floor(timeToWait);
                    robot.mouseMove(x, y);
                } else {
                    timeToWait += distanceInOneStep / speed * 1000;
                }
            }
            robot.mouseMove(point.x, point.y);
            long time = (System.currentTimeMillis() - start);
            Log.d("SplineMouseMover. Distance: " + distance);
            Log.d("SplineMouseMover. Time: " + time + " ms");
            Log.d("SplineMouseMover. Average speed: " + distance / time + " px/ms");
        }
    }

    private void sortByX(List<Point> points) {
        Collections.sort(points, new Comparator<Point>() {

            public int compare(Point o1, Point o2) {
                return o1.x - o2.x;
            }
        });
    }

    private double getMaxSpeedForDistance(double distance) {
        double maxSpeed = MAX_SPEED + (randomizer.getRandom() * 2 - 1)
                * MAX_SPEED_DEVIATION;
        double koeff = maxSpeed / (Math.pow(DISTANCE_FOR_MAX_SPEED, GROW_SPEED_POWER));
        return koeff * Math.pow(distance, GROW_SPEED_POWER);
    }

    private double getSpeed(double distanceFromFirstPoint, double distance, double maxSpeed) {
        double result = -1;
        double koeffPlus = maxSpeed / Math.pow(PART_FOR_MAX_SPEED * distance,
                GROW_SPEED_POWER);
        double koeffMinus = maxSpeed / Math.pow((1 - PART_FOR_MAX_SPEED) * distance,
                DECREASE_SPEED_POWER);
        if (distanceFromFirstPoint < PART_FOR_MAX_SPEED * distance) {
            result = koeffPlus * Math.pow(distanceFromFirstPoint, GROW_SPEED_POWER);
            return result;
        } else {
            result = maxSpeed - koeffMinus * Math.pow(distanceFromFirstPoint
                    - PART_FOR_MAX_SPEED * distance, DECREASE_SPEED_POWER);
            return result;
        }
    }

    private double[] getXArray(List<Point> points) {
        double[] result = new double[points.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = points.get(i).x;
        }
        return result;
    }

    private double[] getYArray(List<Point> points) {
        double[] result = new double[points.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = points.get(i).y;
        }
        return result;
    }

    private List<Point> getPointsForSpline(Point startPoint, Point point) {
        List<Point> result = new ArrayList<Point>();
        result.add(startPoint);
        double globalDistance = MathUtils.dist(
                new PointAdapter(startPoint), new PointAdapter(point));
        double localDistance = globalDistance / (countOfSplinePoints + 1);

        for (int i = 0; i < countOfSplinePoints; i++) {
            result.add(getPointOnDistance(startPoint, point,
                    getRandomDistanceFromLine(globalDistance,
                    localDistance * (i + 1)), localDistance * (i + 1)));
        }
        result.add(point);
        return result;
    }

    private double getRandomDistanceFromLine(double lineLength, double distanceFromFirstPoint) {
        double minDistance = Math.min(distanceFromFirstPoint, lineLength
                - distanceFromFirstPoint);
        double maxDeviation = KOEFF_FOR_DEVIATION * minDistance * minDistance;
        return (randomizer.getRandom() * 2 - 1) * maxDeviation;
    }

    //Returns Point on defined distance from secondPoint
    //such as lines (returned point->secondPoint) and
    //(firstPoint->thirdPoint) are perpendicular
    private Point getPointOnDistance(Point firstPoint, Point secondPoint,
            Point thirdPoint, double distanceFromLine) {
        int resultY, resultX;
        double globalDeltaX = thirdPoint.x - firstPoint.x;
        double globalDeltaY = thirdPoint.y - firstPoint.y;
        if (globalDeltaX < 10) {
            resultX = (int) (secondPoint.x + distanceFromLine);
            resultY = secondPoint.y;
            return new Point(resultX, resultY);
        }
        resultY = (int) (Math.signum(distanceFromLine)
                * Math.sqrt(distanceFromLine * distanceFromLine
                / (1 + Math.pow(globalDeltaY / globalDeltaX, 2))) + secondPoint.y);
        resultX = (int) (-globalDeltaY / globalDeltaX * (resultY - secondPoint.y) + secondPoint.x);
        return new Point(resultX, resultY);
    }

    private Point getPointOnDistance(Point firstPoint,
            Point thirdPoint, double distanceFromLine, double distanceFromFirstPoint) {
        double distance = MathUtils.dist(new PointAdapter(firstPoint),
                new PointAdapter(thirdPoint));
        double globalDeltaX = thirdPoint.x - firstPoint.x;
        double globalDeltaY = thirdPoint.y - firstPoint.y;

        double localDeltaX = distanceFromFirstPoint / distance * globalDeltaX;
        double localDeltaY = distanceFromFirstPoint / distance * globalDeltaY;
        Point secondPoint = new Point(firstPoint.x + (int) localDeltaX,
                firstPoint.y + (int) localDeltaY);
        return getPointOnDistance(firstPoint, secondPoint, thirdPoint, distanceFromLine);
    }

    public static void main(String[] args){
        RobotUtils.moveMouse(500, 500, new SplineMouseMover());
    }
}
