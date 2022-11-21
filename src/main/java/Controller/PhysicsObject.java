package Controller;

import Basics.Collider;
import javafx.scene.shape.Shape;

public class PhysicsObject {
    public Collider object;
    public Shape representation;
    public boolean hasGravity;
    public boolean isCollidable;

    public PhysicsObject(Collider object, Shape representation, boolean hasGravity, boolean isCollidable) {
        this.object = object;
        this.representation = representation;
        this.hasGravity = hasGravity;
        this.isCollidable = isCollidable;
    }
}
