package Basics;

import java.util.ArrayList;
import java.util.List;

public class Utility {

    public static boolean isColliding(Collider lhs, Collider rhs) {
        switch (lhs.getObjectType()) {
            case BOX:
                switch (rhs.getObjectType()) {
                    case BOX:
                    case ROTATION_BOX:
                    case TRIANGLE:
                        return CollisionPolyVsPoly((Box2D) (lhs), (Polygon2D) (rhs));
                    case CIRCLE:
                        return CollisionBoxVsCircle((Box2D) (lhs), (Circle2D) (rhs));
                }
            case TRIANGLE:
                switch (rhs.getObjectType()) {
                    case BOX:
                    case ROTATION_BOX:
                    case TRIANGLE:
                        return CollisionPolyVsPoly((Triangle2D) (lhs), (Polygon2D) (rhs));
                    case CIRCLE:
                        return CollisionCircleVsPoly((Circle2D) (rhs), (Polygon2D) (lhs));
                }
            case CIRCLE:
                switch (rhs.getObjectType()) {
                    case BOX:
                        return CollisionBoxVsCircle((Box2D) (rhs), (Circle2D) (lhs));
                    case TRIANGLE:
                        return CollisionCircleVsPoly((Circle2D) (lhs), (Triangle2D) (rhs));
                    case CIRCLE:
                        return CollisionCircleVsCircle((Circle2D) (lhs), (Circle2D) (rhs));
                    case ROTATION_BOX:
                        return CollisionCircleVsPoly((Circle2D) (lhs), (Polygon2D) (rhs));
                }
            case ROTATION_BOX:
                switch (rhs.getObjectType()) {
                    case BOX:
                    case ROTATION_BOX:
                    case TRIANGLE:
                        return CollisionPolyVsPoly((RotationBox2D) (lhs), (Polygon2D) (rhs));
                    case CIRCLE:
                        return CollisionCircleVsPoly((Circle2D) (rhs), (Polygon2D) (lhs));
                }
            default:
                return false;
        }
    }

    public static boolean CollisionCircleVsPoly(Circle2D lhs, Polygon2D rhs) {

        //Temporary, not optimal
        if (rhs.contains(lhs.getCenter())) {
            return true;
        }
        for (Vec2D v : rhs.getPoints()) {
            if (lhs.contains(v)) {
                return true;
            }
        }
        for (Vec2D v : lhs.getEdgePoints(16)) {
            if (rhs.contains(v)) {
                return true;
            }
        }

        return false;
    }

    public static boolean CollisionPolyVsPoly(Polygon2D lhs, Polygon2D rhs) {
        for (Vec2D v : lhs.getPoints()) {
            if (rhs.contains(v)) {
                return true;
            }
        }
        for (Vec2D v : rhs.getPoints()) {
            if (lhs.contains(v)) {
                return true;
            }
        }
        return false;
    }

    public static boolean CollisionCircleVsCircle(Circle2D lhs, Circle2D rhs) {
        double distanceSqrt = lhs.getCenter().sub(rhs.getCenter()).getLengthSquared();
        return distanceSqrt <= (lhs.getRadius() + rhs.getRadius()) * (lhs.getRadius() + rhs.getRadius());
    }

    public static boolean CollisionBoxVsCircle(Box2D lhs, Circle2D rhs) {

        Line2D centerToCenter = new Line2D(lhs.getCenter(), rhs.getCenter());
        Vec2D closestPoint = null;
        for (Line2D l : lhs.getSides()) {
            if (isIntersectionInsideBoundaries(l, centerToCenter, true)) {
                closestPoint = getIntersection(l, centerToCenter);
                break;
            }
        }
        if (closestPoint == null) {
            return true;
        }
        return (closestPoint.sub(rhs.getCenter())).getLengthSquared() <= rhs.getRadius() * rhs.getRadius();
    }

    public static boolean areParallel (Line2D lhs, Line2D rhs) {
        return lhs.getM() == rhs.getM();
    }

    public static Vec2D getIntersection (Line2D lhs, Line2D rhs) {
        if (areParallel(lhs, rhs)) {
            return null;
        }
        if (lhs.getM() == Double.MAX_VALUE) {
            return new Vec2D(lhs.getFirst().x, lhs.getFirst().x * rhs.getM() + rhs.getT());
        }
        if (rhs.getM() == Double.MAX_VALUE) {
            return new Vec2D(rhs.getFirst().x, rhs.getFirst().x * lhs.getM() + lhs.getT());
        }
        double x = (lhs.getT() - rhs.getT()) / (rhs.getM() - lhs.getM());
        double y = x * lhs.getM() + lhs.getT();

        return new Vec2D(x, y);
    }

    public static boolean isIntersectionInsideBoundaries (Line2D lhs, Line2D rhs, boolean inclusive) {
        if (areParallel(lhs, rhs)) {
            return false;
        }
        if (!lhs.hasBoundaries() && !rhs.hasBoundaries()) {
            return true;
        }
        Vec2D intersection = getIntersection(lhs, rhs);

        if (inclusive) {
            return isBetween(intersection.x, lhs.getFirst().x, lhs.getSecond().x) &&
                    isBetween(intersection.y, lhs.getFirst().y, lhs.getSecond().y) &&
                    isBetween(intersection.x, rhs.getFirst().x, rhs.getSecond().x) &&
                    isBetween(intersection.y, rhs.getFirst().y, rhs.getSecond().y);
        } else {
            return isBetweenExclusive(intersection.x, lhs.getFirst().x, lhs.getSecond().x) &&
                    isBetweenExclusive(intersection.y, lhs.getFirst().y, lhs.getSecond().y) &&
                    isBetweenExclusive(intersection.x, rhs.getFirst().x, rhs.getSecond().x) &&
                    isBetweenExclusive(intersection.y, rhs.getFirst().y, rhs.getSecond().y);
        }
    }

    public static boolean isIntersectionInsideBoundaries (List<Line2D> lhs, List<Line2D> rhs, boolean inclusive) {
        for (Line2D l : lhs) {
            for (Line2D r : rhs) {
                if (isIntersectionInsideBoundaries(l, r, inclusive)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static double getAreaTriangle(Triangle2D triangle2D) {
        return Math.abs((triangle2D.getA().x * (triangle2D.getB().y - triangle2D.getC().y)
                + triangle2D.getB().x * (triangle2D.getC().y - triangle2D.getA().y)
                + triangle2D.getC().x * (triangle2D.getA().y - triangle2D.getB().y)) / 2);
    }

    public static boolean compareDoubles(double d1, double d2, double epsilon) {
        return Math.abs(d1 - d2) < epsilon;
    }

    public static boolean isBetween (double x, double a, double b) {
        if (b < a) {
            double temp = a;
            a = b;
            b = temp;
        }

        return a <= x && x <= b;
    }

    public static boolean isBetweenExclusive (double x, double a, double b) {
        if (b < a) {
            double temp = a;
            a = b;
            b = temp;
        }

        return a < x && x < b;
    }

    public static boolean isRect (Vec2D p1, Vec2D p2, Vec2D p3, Vec2D p4) {
        return p4.sub(p1).dotProduct(p2.sub(p1)) == 0 && p4.sub(p3).dotProduct(p2.sub(p3)) == 0;
    }

    public static double getAngle(Vec2D lhs, Vec2D rhs) {
        return Math.acos(lhs.dotProduct(rhs) / (lhs.getLength() * rhs.getLength()));
    }
}
