package Basics;

import java.util.ArrayList;
import java.util.List;

public class Circle2D extends Collider {
    private Vec2D center;
    private double radius;

    //Additional Information
    private List<Vec2D> edgePoints;

    public Circle2D(Vec2D center, double radius) {
        this.center = center;
        this.radius = radius;

        edgePoints = new ArrayList<>();
    }

    @Override
    public Vec2D getCenter() {
        return center;
    }

    public void setCenter(Vec2D center) {
        this.center = center;
        edgePoints.clear();
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
        edgePoints.clear();
    }

    public List<Vec2D> getEdgePoints(int nPoints) {
        if (edgePoints.size() != nPoints) {
            edgePoints = calculateEdgePoints(nPoints);
        }
        return edgePoints;
    }

    @Override
    public void move(Vec2D dir) {
        center = center.add(dir);
        edgePoints.clear();
    }

    @Override
    public boolean contains(Vec2D point) {
        return point.sub(center).getLengthSquared() <= radius * radius;
    }

    @Override
    public MovableObjectType getObjectType() {
        return MovableObjectType.CIRCLE;
    }

    @Override
    public double getBottom() {
        return center.y - radius;
    }

    @Override
    public double getTop() {
        return center.y + radius;
    }

    @Override
    public double getLeft() {
        return center.x - radius;
    }

    @Override
    public double getRight() {
        return center.x + radius;
    }

    @Override
    public Collider getCopy() {
        return new Circle2D(center, radius);
    }

    @Override
    public String toString() {
        return "Circle2D: [center = " + center + ", radius = " + radius + "]";
    }

    private List<Vec2D> calculateEdgePoints(int nPoints) {
        List<Vec2D> edgePoints = new ArrayList<>();
        double degJump = 2 * Math.PI / (float) (nPoints);
        double currentDeg = 0;
        for (int i = 0; i < nPoints; i++) {
            Vec2D vec2D = new Vec2D(radius * Math.cos(currentDeg), radius * Math.sin(currentDeg));
            edgePoints.add(center.add(vec2D));
            currentDeg += degJump;
        }
        return edgePoints;
    }
}
