package Controller;

import Basics.Collider;
import javafx.scene.shape.Shape;

@SuppressWarnings("ALL")
public class PhysicsObject {
    public Collider object;
    public Shape representation;
    public boolean hasGravity;
    public boolean isCollidable;
    public double bounciness;

    public PhysicsObject(Collider object, Shape representation, boolean hasGravity,
                         boolean isCollidable, double bounciness) {
        this.object = object;
        this.representation = representation;
        this.hasGravity = hasGravity;
        this.isCollidable = isCollidable;
        this.bounciness = bounciness;
    }
}
