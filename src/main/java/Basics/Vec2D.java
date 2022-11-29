package Basics;

public class Vec2D implements Comparable<Vec2D> {

    public double x;
    public double y;

    public Vec2D (double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2D add (Vec2D rhs) {
        return new Vec2D(x + rhs.x, y + rhs.y);
    }

    public Vec2D sub (Vec2D rhs) {
        return new Vec2D(x - rhs.x, y - rhs.y);
    }

    public Vec2D mult (double a) {
        return new Vec2D(x * a, y * a);
    }

    public double dotProduct (Vec2D rhs) {
        return x * rhs.x + y * rhs.y;
    }

    public double getLength() {
        return Math.sqrt(x * x + y * y);
    }

    public double getLengthSquared () {
        return x * x + y * y;
    }

    @SuppressWarnings("unused")
    public Vec2D getNormalizedVector () {
        double length = getLength();
        return new Vec2D(x / length, y / length);
    }

    @Override
    public boolean equals(Object rhs) {
        // If the object is compared with itself then return true
        if (rhs == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(rhs instanceof Vec2D)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        Vec2D vec = (Vec2D) rhs;

        return vec.x == this.x && vec.y == this.y;
    }

    @Override
    public int compareTo(Vec2D rhs) {
        if (x == rhs.x) {
            return Double.compare(y, rhs.y);
        } else if (x < rhs.x) {
            return -1;
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
