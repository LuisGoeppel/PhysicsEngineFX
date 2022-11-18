package Basics;

import Basics.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilityTest {

    @Test
    public void testIntersectionOfLines1() {
        Line2D A = new Line2D(6, -9);
        Line2D B = new Line2D(4, 20);
        Vec2D intersection = Utility.getIntersection(A, B);

        assertEquals(14.5, intersection.x);
        assertEquals(78, intersection.y);
    }

    @Test
    public void testInBetweenTrue() {
        double x = 7;
        double a = 8;
        double b = -2;
        assertTrue(Utility.isBetween(x, a, b));
    }

    @Test
    public void testInBetweenFalse() {
        double x = 7;
        double a = 3;
        double b = -2;
        assertFalse(Utility.isBetween(x, a, b));
    }

    @Test
    public void testInsideIntersectionTrue() {
        Line2D A = new Line2D(new Vec2D(-2, 14), new Vec2D(2, -2));
        Line2D B = new Line2D(new Vec2D(-5, -7), new Vec2D(5, 13));

        assertTrue(Utility.isIntersectionInsideBoundaries(A, B, true));
    }

    @Test
    public void testInsideIntersectionFalse1() {
        Line2D A = new Line2D(new Vec2D(-2, 14), new Vec2D(2, -2));
        Line2D B = new Line2D(new Vec2D(-5, -7), new Vec2D(-2, -1));

        assertFalse(Utility.isIntersectionInsideBoundaries(A, B, true));
    }

    @Test
    public void testInsideIntersectionFalse2() {
        Line2D A = new Line2D(new Vec2D(-2, 14), new Vec2D(0, 6));
        Line2D B = new Line2D(new Vec2D(-5, -7), new Vec2D(5, 13));

        assertFalse(Utility.isIntersectionInsideBoundaries(A, B, true));
    }

    @Test
    public void testBoxVsBoxCollisionTrue() {
        Box2D A = new Box2D(new Vec2D(1, 1), new Vec2D(4, 3));
        Box2D B = new Box2D(new Vec2D(3, 2), new Vec2D(10, 10));

        assertTrue(Utility.CollisionPolyVsPoly(A, B));
    }

    @Test
    public void testBoxVsBoxCollisionFalse() {
        Box2D A = new Box2D(new Vec2D(1, 1), new Vec2D(4, 3));
        Box2D B = new Box2D(new Vec2D(-5, 5), new Vec2D(10, 10));

        assertFalse(Utility.CollisionPolyVsPoly(A, B));
    }

    @Test
    public void testTriVsTriCollisionTrue1() {
        Vec2D A = new Vec2D(-0.06, 2.75);
        Vec2D B = new Vec2D(-10, -3);
        Vec2D C = new Vec2D(-1.3, 0.59);
        Vec2D D = new Vec2D(-11, 5);
        Vec2D E = new Vec2D(-5.68, 2.17);
        Vec2D F = new Vec2D(1.6, 2.43);

        Triangle2D T1 = new Triangle2D(A, B, C);
        Triangle2D T2 = new Triangle2D(D, E, F);

        assertTrue(Utility.CollisionPolyVsPoly(T1, T2));
    }

    @Test
    public void testTriVsTriCollisionTrue2() {
        Vec2D A = new Vec2D(-4.2, 6);
        Vec2D B = new Vec2D(-10, -3);
        Vec2D C = new Vec2D(-1.3, 0.59);
        Vec2D D = new Vec2D(-11, 5);
        Vec2D E = new Vec2D(-5.68, 2.17);
        Vec2D F = new Vec2D(1.6, 2.43);

        Triangle2D T1 = new Triangle2D(A, B, C);
        Triangle2D T2 = new Triangle2D(D, E, F);

        assertTrue(Utility.CollisionPolyVsPoly(T1, T2));
    }

    @Test
    public void testTriVsTriCollisionFalse() {
        Vec2D A = new Vec2D(-0.78, 1.95);
        Vec2D B = new Vec2D(-10, -3);
        Vec2D C = new Vec2D(-1.3, 0.59);
        Vec2D D = new Vec2D(-11, 5);
        Vec2D E = new Vec2D(-5.68, 2.17);
        Vec2D F = new Vec2D(1.6, 2.43);

        Triangle2D T1 = new Triangle2D(A, B, C);
        Triangle2D T2 = new Triangle2D(D, E, F);

        assertFalse(Utility.CollisionPolyVsPoly(T1, T2));
    }

    @Test
    public void testBoxVsCircleTrue1() {
        Box2D box = new Box2D(new Vec2D(-1, 1), new Vec2D(3, 4));
        Circle2D circle2D = new Circle2D(new Vec2D(4, -1), 3);

        assertTrue(Utility.CollisionBoxVsCircle(box, circle2D));
    }

    @Test
    public void testBoxVsCircleTrue2() {
        Box2D box = new Box2D(new Vec2D(-1, 1), new Vec2D(3, 4));
        Circle2D circle2D = new Circle2D(new Vec2D(0.75, 5.9), 2.2);

        assertTrue(Utility.CollisionBoxVsCircle(box, circle2D));
    }

    @Test
    public void testBoxVsCircleTrue3() {
        Box2D box = new Box2D(new Vec2D(-1, 1), new Vec2D(3, 4));
        Circle2D circle2D = new Circle2D(new Vec2D(-0.4, 2.8), 1);

        assertTrue(Utility.CollisionBoxVsCircle(box, circle2D));
    }

    @Test
    public void testBoxVsCircleFalse() {
        Box2D box = new Box2D(new Vec2D(-1, 1), new Vec2D(3, 4));
        Circle2D circle2D = new Circle2D(new Vec2D(-5.5, -1.47), 4.66);

        assertFalse(Utility.CollisionBoxVsCircle(box, circle2D));
    }

    @Test
    public void testIsRectTrue() {

        Vec2D A = new Vec2D(2, 1);
        Vec2D B = new Vec2D(6,5);
        Vec2D C = new Vec2D(8,3);
        Vec2D D = new Vec2D(4, -1);

        assertTrue(Utility.isRect(A, B, C, D));
    }

    @Test
    public void testRotationRectConstruction() {
        Vec2D A = new Vec2D(2, 1);
        Vec2D B = new Vec2D(6,5);
        Vec2D C = new Vec2D(8,3);
        Vec2D D = new Vec2D(4, -1);

        RotationBox2D rBox = new RotationBox2D(A, B, C, D);
        assertTrue(true);
    }
}