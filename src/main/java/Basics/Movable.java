package Basics;

public abstract class Movable {

    public abstract void move (Vec2D dir);
    public abstract boolean contains (Vec2D point);
    public abstract MovableObjectType getObjectType();

    public abstract double getBottom();
    public abstract double getTop();
    public abstract double getLeft();
    public abstract double getRight();
    public abstract Movable getCopy();

}
