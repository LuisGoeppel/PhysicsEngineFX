package Basics;

public class Circle2D extends Movable{
    private Vec2D center;
    private double radius;

    public Circle2D(Vec2D center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    @Override
    public Vec2D getCenter() {
        return center;
    }

    public void setCenter(Vec2D center) {
        this.center = center;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public Box2D getOuterBox() {
        return new Box2D(new Vec2D(center.x - radius, center.y - radius),
                new Vec2D(center.x + radius, center.y + radius));
    }

    @Override
    public void move(Vec2D dir) {
        center = center.add(dir);
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
    public Movable getCopy() {
        return new Circle2D(center, radius);
    }

    @Override
    public String  toString() {
        return "Circle2D: [center = " + center + ", radius = " + radius + "]";
    }
}
