package Basics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Box2D extends Polygon2D {
    private Vec2D topRight;
    private Vec2D bottomLeft;

    public Box2D(Vec2D bottomLeft, Vec2D topRight) {
        this.topRight = topRight;
        this.bottomLeft = bottomLeft;
    }

    public Box2D (double left, double top, double right, double bottom) {
        topRight = new Vec2D(right, top);
        bottomLeft = new Vec2D(left, bottom);
    }

    public Box2D (Vec2D bottomLeft, double width, double height) {
        this.bottomLeft = bottomLeft;
        topRight = new Vec2D(bottomLeft.x + width, bottomLeft.y + height);
    }

    public Vec2D getTopLeft() {
        return new Vec2D(bottomLeft.x, topRight.y);
    }

    public Vec2D getTopRight() {
        return topRight;
    }

    public Vec2D getBottomLeft() {
        return bottomLeft;
    }

    @Override
    public List<Vec2D> getPoints() {
        return calculatePoints();
    }

    public Vec2D getBottomRight() {
        return new Vec2D(topRight.x, bottomLeft.y);
    }

    @Override
    public double getTop() {
        return topRight.y;
    }

    @Override
    public double getBottom() {
        return bottomLeft.y;
    }

    @Override
    public double getLeft() {
        return bottomLeft.x;
    }

    @Override
    public double getRight() {
        return topRight.x;
    }

    @Override
    public Collider getCopy() {
        return new Box2D(bottomLeft, topRight);
    }

    @Override
    public Vec2D getCenter() {
        return new Vec2D(bottomLeft.x + (topRight.x - bottomLeft.x) / 2, bottomLeft.y + (topRight.y - bottomLeft.y) / 2);
    }

    public void setCenter(Vec2D v) {
        double width = getWidth();
        double height = getHeight();
        bottomLeft = new Vec2D(v.x - width/2, v.y - height/2);
        topRight = new Vec2D(v.x + width/2, v.y + height/2);
    }

    public void setWidth(double width) {
        Vec2D center = getCenter();
        bottomLeft = new Vec2D(center.x - width/2, bottomLeft.y);
        topRight = new Vec2D(center.x + width/2, topRight.y);
    }

    public void setHeight(double height) {
        Vec2D center = getCenter();
        bottomLeft = new Vec2D(bottomLeft.x, center.y - height/2);
        topRight = new Vec2D(topRight.x, center.y + height/2);
    }

    public double getWidth() {
        return topRight.x - bottomLeft.x;
    }

    public double getHeight() {
        return topRight.y - bottomLeft.y;
    }

    @Override
    public List<Line2D> getSides() {
        return calculateSides();
    }

    @Override
    public void move(Vec2D dir) {
        topRight = topRight.add(dir);
        bottomLeft = bottomLeft.add(dir);
    }

    @Override
    public boolean contains(Vec2D point) {
        return bottomLeft.x <= point.x && point.x <= topRight.x &&
                bottomLeft.y <= point.y && point.y <= topRight.y;
    }

    @Override
    public MovableObjectType getObjectType() {
        return MovableObjectType.BOX;
    }

    @Override
    public String toString() {
        return "Box2D: [bottomLeft = " + bottomLeft + ", topRight = " + topRight + "]";
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) {
            return false;
        }
        if (rhs == this) {
            return true;
        }
        if (getClass() != rhs.getClass()) {
            return false;
        }
        Box2D obj = (Box2D) (rhs);
        return topRight.equals(obj.topRight) && bottomLeft.equals(obj.bottomLeft);
    }

    private List<Line2D> calculateSides() {
        List<Line2D> sides = new ArrayList<>();
        sides.add(new Line2D(getTopLeft(), topRight));
        sides.add(new Line2D(topRight, getBottomRight()));
        sides.add(new Line2D(getBottomRight(), bottomLeft));
        sides.add(new Line2D(bottomLeft, getTopLeft()));
        return sides;
    }

    private List<Vec2D> calculatePoints() {
        List<Vec2D> points = Arrays.asList(getBottomLeft(), getBottomRight(), getTopLeft(), getTopRight());
        return points;
    }
}
