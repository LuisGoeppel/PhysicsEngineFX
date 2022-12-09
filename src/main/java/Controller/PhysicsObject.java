package Controller;

import Basics.Collider;
import javafx.scene.shape.Shape;

@SuppressWarnings("ALL")
public class PhysicsObject {

    public int objectID;
    public Collider object;
    public Shape representation;
    public boolean hasGravity;
    public boolean isCollidable;
    public double bounciness;
    public boolean hasChanged;

    public PhysicsObject(Collider object, Shape representation, boolean hasGravity,
                         boolean isCollidable, double bounciness, int id) {
        this.object = object;
        this.representation = representation;
        this.hasGravity = hasGravity;
        this.isCollidable = isCollidable;
        this.bounciness = bounciness;
        this.objectID = id;

        this.hasChanged = true;
    }
}
