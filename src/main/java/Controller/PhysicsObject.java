package Controller;

import Basics.Movable;
import javafx.scene.Node;
import javafx.scene.shape.Shape;

public class PhysicsObject {
    public Movable object;
    public Shape representation;
    public boolean hasGravity;
    public boolean isCollidable;

    public PhysicsObject(Movable object, Shape representation, boolean hasGravity, boolean isCollidable) {
        this.object = object;
        this.representation = representation;
        this.hasGravity = hasGravity;
        this.isCollidable = isCollidable;
    }
}
