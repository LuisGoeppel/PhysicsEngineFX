package Basics;

import java.util.List;

public abstract class Polygon2D extends Movable{

    public abstract List<Vec2D> getPoints();

    public abstract List<Line2D> getSides();
}
