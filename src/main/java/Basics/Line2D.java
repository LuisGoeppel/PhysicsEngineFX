package Basics;

public class Line2D {

    private Vec2D first;
    private Vec2D second;
    private boolean hasBoundaries;

    public Line2D(Vec2D first, Vec2D second) {
        if (first.compareTo(second) < 0) {
            this.first = first;
            this.second = second;
        } else if (first.compareTo(second) > 0) {
            this.first = second;
            this.second = first;
        } else {
            throw new IllegalArgumentException("The two Points" +
                    " to declare a Line cannot be the same!");
        }
        hasBoundaries = true;
    }

    public Line2D(Vec2D first, Vec2D second, boolean boundaries) {
        if (first.compareTo(second) < 0) {
            this.first = first;
            this.second = second;
        } else if (first.compareTo(second) > 0) {
            this.first = second;
            this.second = first;
        } else {
            throw new IllegalArgumentException("The two Points" +
                    " to declare a Line cannot be the same!");
        }
        hasBoundaries = boundaries;
    }

    public Line2D(double m, double t) {
        first = new Vec2D(0, t);
        second = new Vec2D(1, m + t);
        hasBoundaries = false;
    }

    public boolean hasBoundaries() {
        return hasBoundaries;
    }

    public Vec2D getFirst() {
        return first;
    }

    public Vec2D getSecond() {
        return second;
    }

    public double getM () {
        if (first.x == second.x) {
            return Double.MAX_VALUE;
        }
        return (first.y - second.y) / (first.x - second.x);
    }

    public double getT () {
        if (first.x == 0) {
            return first.y;
        }
        if (first.y == second.y) {
            return first.y;
        }
        return first.y - (first.x * getM());
    }

    @Override
    public String toString() {
        return "[" + first.toString() + ", " + second.toString() + "]";
    }
}
