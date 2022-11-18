package Controller;

import Basics.Movable;
import javafx.scene.Node;

public class PhysicsObject {
    public Movable object;
    public Node representation;
    public boolean hasGravity;
    public boolean isCollidable;

    public PhysicsObject(Movable object, Node representation, boolean hasGravity, boolean isCollidable) {
        this.object = object;
        this.representation = representation;
        this.hasGravity = hasGravity;
        this.isCollidable = isCollidable;
    }
}
