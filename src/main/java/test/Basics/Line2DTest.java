package Basics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Line2DTest {

    @Test
    public void testGetM() {
        Line2D line2D = new Line2D(420, 69);
        assertEquals(420, line2D.getM());
    }

    @Test
    public void testGetT() {
        Line2D line2D = new Line2D(420, 69);
        assertEquals(69, line2D.getT());
    }

    @Test
    public void testGetM2 () {
        Line2D line2D = new Line2D(new Vec2D(-2, -0.266), new Vec2D(69, 120.434));
        assertEquals(1.7, line2D.getM());
    }

    @Test
    public void testGetT2 () {
        Line2D line2D = new Line2D(new Vec2D(-2, -0.266), new Vec2D(69, 120.434));
        assertEquals(3.134, line2D.getT());
    }
}