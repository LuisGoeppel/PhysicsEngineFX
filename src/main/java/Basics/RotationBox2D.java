package Basics;

import java.util.ArrayList;
import java.util.List;

public class RotationBox2D extends Polygon2D {

    private Vec2D center;
    private double width, height;
    private double rotationAngle; //in radians

    //Additional Information;
    private List<Vec2D> points;
    private List<Line2D> sides;

    public RotationBox2D(Vec2D center, double width, double height, double rotationAngle) {
        this.center = center;
        this.width = width;
        this.height = height;
        this.rotationAngle = convertAngleToRad(rotationAngle);

        points = new ArrayList<>();
        sides = new ArrayList<>();
    }

    public RotationBox2D(Vec2D p1, Vec2D p2, Vec2D p3, Vec2D p4) {
        if (!Utility.isRect(p1, p2, p3, p4)) {
            throw new IllegalArgumentException("This ain't no Rect mate");
        }
        Line2D l1 = new Line2D(p1, p3);
        Line2D l2 = new Line2D(p2, p4);
        center = Utility.getIntersection(l1, l2);
        width = p1.sub(p2).getLength();
        height = p1.sub(p3).getLength();

        Vec2D rot = p2.sub(p1);
        Vec2D base = new Vec2D(1, 0);

        rotationAngle = Math.acos(rot.dotProduct(base) / (rot.getLength() * base.getLength()));

        points = new ArrayList<>();
        sides = new ArrayList<>();
    }

    @Override
    public Vec2D getCenter() {
        return center;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getRotationAngle() {
        return convertAngleToDeg(rotationAngle);
    }

    public void setCenter(Vec2D center) {
        this.center = center;
        clearAdditionalInfo();
    }

    public void setWidth(double width) {
        this.width = width;
        clearAdditionalInfo();
    }

    public void setHeight(double height) {
        this.height = height;
        clearAdditionalInfo();
    }

    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = convertAngleToRad(rotationAngle);
        clearAdditionalInfo();
    }

    @Override
    public List<Vec2D> getPoints() {
        if (points.isEmpty()) {
            points = calculatePoints();
        }
        return points;
    }

    @Override
    public List<Line2D> getSides() {
        if (sides.isEmpty()) {
            sides = calculateSides();
        }
        return sides;
    }

    @Override
    public void move(Vec2D dir) {
        center = center.add(dir);
        clearAdditionalInfo();
    }

    @Override
    public boolean contains(Vec2D point) {
        List<Line2D> sides = getSides();
        Line2D pointToTop = new Line2D(point, new Vec2D(point.x, -10));
        int nCrossedSides = 0;
        for (int i = 0; i < sides.size(); i++) {
            if (Utility.isIntersectionInsideBoundaries(pointToTop, sides.get(i), true)) {
                nCrossedSides++;
            }
        }
        return nCrossedSides % 2 != 0;
    }

    @Override
    public MovableObjectType getObjectType() {
        return MovableObjectType.ROTATION_BOX;
    }

    @Override
    public double getBottom() {
        List<Vec2D> points = getPoints();
        double minY = Integer.MAX_VALUE;
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).y < minY) {
                minY = points.get(i).y;
            }
        }
        return minY;
    }

    @Override
    public double getTop() {
        List<Vec2D> points = getPoints();
        double maxY = Integer.MIN_VALUE;
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).y > maxY) {
                maxY = points.get(i).y;
            }
        }
        return maxY;
    }

    @Override
    public double getLeft() {
        List<Vec2D> points = getPoints();
        double minX = Integer.MAX_VALUE;
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).x < minX) {
                minX = points.get(i).x;
            }
        }
        return minX;
    }

    @Override
    public double getRight() {
        List<Vec2D> points = getPoints();
        double maxX = Integer.MIN_VALUE;
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).x < maxX) {
                maxX = points.get(i).x;
            }
        }
        return maxX;
    }

    @Override
    public Collider getCopy() {
        return new RotationBox2D(center, width, height, convertAngleToDeg(rotationAngle));
    }

    @Override
    public String toString() {
        return "RotationBox2D: [center = " + center + ", width = " + width +
                ", height = " + height + ", rotationAngle = " + rotationAngle + "]";
    }


    private double convertAngleToRad (double deg) {
        return deg * (Math.PI / 180);
    }

    private double convertAngleToDeg (double rad) {
        return rad * (180 / Math.PI);
    }

    private List<Vec2D> calculatePoints() {
        List<Vec2D> corners = new ArrayList<>();

        Vec2D v1 = new Vec2D(Math.cos(rotationAngle), Math.sin(rotationAngle));
        Vec2D v2 = new Vec2D(-1 * v1.y, v1.x);

        v1 = v1.mult(width/2);
        v2 = v2.mult(height/2);

        corners.add(center.add(v1).add(v2));
        corners.add(center.add(v1).sub(v2));
        corners.add(center.sub(v1).sub(v2));
        corners.add(center.sub(v1).add(v2));

        return corners;
    }

    private List<Line2D> calculateSides() {
        List<Vec2D> corners = getPoints();
        List<Line2D> sides = new ArrayList<>();
        for (int i = 0; i < corners.size(); i++) {
            sides.add(new Line2D(corners.get(i), corners.get((i + 1) % corners.size())));
        }
        return sides;
    }

    private void clearAdditionalInfo() {
        points.clear();
        sides.clear();
    }
}
