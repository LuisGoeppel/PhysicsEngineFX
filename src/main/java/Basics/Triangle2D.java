package Basics;

import java.util.ArrayList;
import java.util.List;

public class Triangle2D extends Polygon2D {

    private Vec2D A, B, C;

    public Triangle2D(Vec2D a, Vec2D b, Vec2D c) {
        A = a;
        B = b;
        C = c;
    }

    public Vec2D getA() {
        return A;
    }

    public Vec2D getB() {
        return B;
    }

    public Vec2D getC() {
        return C;
    }

    public void setA(Vec2D a) {
        A = a;
    }

    public void setB(Vec2D b) {
        B = b;
    }

    public void setC(Vec2D c) {
        C = c;
    }

    @Override
    public List<Vec2D> getPoints() {
        List<Vec2D> points = new ArrayList<>();
        points.add(A);
        points.add(B);
        points.add(C);
        return points;
    }

    @Override
    public List<Line2D> getSides() {
        List<Line2D> sides = new ArrayList<>();
        sides.add(new Line2D(A, B));
        sides.add(new Line2D(B, C));
        sides.add(new Line2D(C, A));
        return sides;
    }

    @Override
    public void move(Vec2D dir) {
        A = A.add(dir);
        B = B.add(dir);
        C = C.add(dir);
    }

    @Override
    public boolean contains(Vec2D P) {
        Triangle2D PAB = new Triangle2D(P, A, B);
        Triangle2D PBC = new Triangle2D(P, B, C);
        Triangle2D PAC = new Triangle2D(P, A, C);

        return Utility.compareDoubles(Utility.getAreaTriangle(this), (Utility.getAreaTriangle(PAB)
                + Utility.getAreaTriangle(PBC) + Utility.getAreaTriangle(PAC)), 0.000001d);
    }

    @Override
    public MovableObjectType getObjectType() {
        return MovableObjectType.TRIANGLE;
    }

    @Override
    public double getBottom() {
        return Math.min(A.y, Math.min(B.y, C.y));
    }

    @Override
    public double getTop() {
        return Math.max(A.y, Math.max(B.y, C.y));
    }

    @Override
    public double getLeft() {
        return Math.min(A.x, Math.min(B.x, C.x));
    }

    @Override
    public double getRight() {
        return Math.max(A.x, Math.max(B.x, C.x));
    }

    @Override
    public Vec2D getCenter() {
        return new Vec2D(getRight() - getLeft(), getTop() - getBottom());
    }

    @Override
    public Collider getCopy() {
        return new Triangle2D(A, B, C);
    }

    @Override
    public String toString() {
        return "Triangle2D: [A = " + A + ", B = " + B + ", C = " + C + "]";
    }
}
