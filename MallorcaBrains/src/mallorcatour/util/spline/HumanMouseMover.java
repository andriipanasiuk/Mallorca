/*
 * and open the template in the editor.
 */
package mallorcatour.util.spline;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;

import mallorcatour.tools.IRandomizer;
import mallorcatour.tools.ThreadUtils;
import mallorcatour.tools.UniformRandomizer;

/**
 *
 * @author Andrew
 */
public class HumanMouseMover {

    private double MouseSpeed = 2;
    private int LMouse_MissChance;
    private static Robot robot;
    private IRandomizer randomizer = new UniformRandomizer();

    static {
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            throw new RuntimeException(ex);
        }
    }

    private double hypot(double one, double two) {
        return Math.sqrt(one * one + two * two);
    }

    public void WindMouse(double xs, double ys, double xe, double ye, double gravity,
            double wind, double minWait, double maxWait, double maxStep, double targetArea) {

        double veloX = 0, veloY = 0, windX = 0, windY = 0, veloMag, dist, randomDist, step;
        int lastX, lastY;
        double sqrt2, sqrt3, sqrt5;

        sqrt2 = Math.sqrt(2);
        sqrt3 = Math.sqrt(3);
        sqrt5 = Math.sqrt(5);
        while (hypot(xs - xe, ys - ye) > 1) {

            dist = hypot(xs - xe, ys - ye);
            wind = Math.min(wind, dist);
            if (dist >= targetArea) {
                windX = windX / sqrt3 + (Math.random() * (Math.round(wind) * 2 + 1) - wind) / sqrt5;
                windY = windY / sqrt3 + (Math.random() * (Math.round(wind) * 2 + 1) - wind) / sqrt5;
            } else {
                windX = windX / sqrt2;
                windY = windY / sqrt2;
                if (maxStep < 3) {
                    maxStep = Math.random() * 3 + 3.0;
                } else {
                    maxStep = maxStep / sqrt5;
                }
            }
            veloX = veloX + windX;
            veloY = veloY + windY;
            veloX = veloX + gravity * (xe - xs) / dist;
            veloY = veloY + gravity * (ye - ys) / dist;
            if (hypot(veloX, veloY) > maxStep) {
                randomDist = maxStep / 2.0 + Math.random() * (Math.round(maxStep) / 2);
                veloMag = hypot(veloX, veloY);
                veloX = (veloX / veloMag) * randomDist;
                veloY = (veloY / veloMag) * randomDist;
            }
            lastX = (int) Math.round(xs);
            lastY = (int) Math.round(ys);
            xs = xs + veloX;
            ys = ys + veloY;
            if ((lastX != Math.round(xs)) || (lastY != Math.round(ys))) {
                moveMouse((int) Math.round(xs), (int) Math.round(ys));
            }
            step = hypot(xs - lastX, ys - lastY);
            ThreadUtils.sleep(Math.round((maxWait - minWait) * (step / maxStep) + minWait));
        }
        if (Math.round(xe) != Math.round(xs)
                || (Math.round(ye) != Math.round(ys))) {
            moveMouse((int) Math.round(xe), (int) Math.round(ye));
        }
    }

    private void moveMouse(int x, int y) {
        robot.mouseMove(x, y);
    }
    private boolean useLaptopMouse = true;

    public void MMouse(int x, int y, int rx, int ry) {
        int cx = 0, cy = 0;
        double randSpeed = 0;
        long seg, e, f, g, nx, ny, hypo;
        double a, b;
        boolean miss;
        if (useLaptopMouse) {
            miss = (Math.random() * LMouse_MissChance == 0);
            e = 0;
            a = x - cx;
            b = y - cy;
            hypo = Math.round(hypot(a, b));
            if (hypo == 0) {
                return;
            } else if (hypo >= 1 && hypo <= 225) {
                seg = 1;
            } else if (hypo >= 226 && hypo <= 600) {
                seg = randomizer.getRandom(0, 2) + 1;
            } else if (hypo >= 601 && hypo <= 1800) {
                seg = randomizer.getRandom(0, 3) + 2;
            } else {
                seg = 5;
            }
            f = Math.round(a / seg);
            g = Math.round(b / seg);
            do {
                ThreadUtils.sleep(30 + randomizer.getRandom(0, 50));
                randSpeed = (Math.random() * MouseSpeed / 2.0 + MouseSpeed) / 10.0;
                if (randSpeed == 0.0) {
                    randSpeed = 0.1;
                }
//              getMousePos(cx, cy);
                nx = (cx + (f * e)) + randomizer.getRandom(0, rx);
                ny = (cy + (g * e)) + randomizer.getRandom(0, ry);
                if (miss) {
                    nx = nx + randomizer.getRandom(rx, rx * 2);
                    ny = ny + randomizer.getRandom(ry, ry * 2);
                }
                WindMouse(cx, cy, nx, ny, 11.0, 8.0, 10.0 / randSpeed, 12.0 / randSpeed, 10.0 * randSpeed, 10.0 * randSpeed);
                e = e + 1;
            } while (e != seg);
            if (!new Rectangle(x, y, rx, ry).contains(new Point(cx, cy))) {
                ThreadUtils.sleep(30 + randomizer.getRandom(0, 30));
                WindMouse(cx, cy, (x + randomizer.getRandom(0, rx)),
                        (y + randomizer.getRandom(0, ry)),
                        11.0, 6.0, 10.0 / randSpeed, 15.0 / randSpeed, 10.0 * randSpeed, 10.0 * randSpeed);
            }
        } else {
            randSpeed = (randomizer.getRandom(0, MouseSpeed) / 2.0 + MouseSpeed) / 10.0;
            if (randSpeed == 0) {
                randSpeed = 0.1;
            }
            x = x + randomizer.getRandom(0, rx);
            y = y + randomizer.getRandom(0, ry);
            WindMouse(cx, cy, x, y, 9.0, 3.0, 10.0 / randSpeed,
                    15.0 / randSpeed, 10.0 * randSpeed, 10.0 * randSpeed);
        }
    }

    public static void main(String[] a) {
        ThreadUtils.sleep(2000);
        new HumanMouseMover().MMouse(10, 10, 20, 20);
    }
}
