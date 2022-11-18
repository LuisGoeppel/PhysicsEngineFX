package Controls;

import Basics.Movable;
import Basics.Utility;
import Basics.Vec2D;
import Controller.PhysicsObject;

import java.util.ArrayList;
import java.util.List;

public class Gravity {

    private class GravityObject {
        public Movable object;
        public Boolean applyGravity;
        public Boolean isCollidable;
        public int ticksSinceStart;

        public GravityObject(Movable object, Boolean applyGravity, Boolean isCollidable, int ticksSinceStart) {
            this.object = object;
            this.applyGravity = applyGravity;
            this.ticksSinceStart = ticksSinceStart;
            this.isCollidable = isCollidable;
        }
    }

    private List<GravityObject> gravityObjects;
    private final double GRAVITY_CONSTANT = 0.1635; //60 FPS
    private double bottomYCord;

    public Gravity(List<Movable> objects, List<Boolean> applyGravity, List<Boolean> collidable, double bottomYCord) {
        if (objects.size() != applyGravity.size()) {
            throw new IllegalArgumentException("Sizes do not match!");
        }
        gravityObjects = new ArrayList<>();
        for (int i = 0; i < objects.size(); i++) {
            GravityObject gravityObject = new GravityObject(objects.get(i),
                    applyGravity.get(i), collidable.get(i), 0);
            gravityObjects.add(gravityObject);
        }
        this.bottomYCord = bottomYCord;
    }

    public Gravity(double bottomYCord) {
        gravityObjects = new ArrayList<>();
        this.bottomYCord = bottomYCord;
    }

    public void add(PhysicsObject o) {
        gravityObjects.add(new GravityObject(o.object, o.hasGravity, o.isCollidable, 0));
    }

    public void add(Movable m, boolean applyGravity, boolean isCollidable) {
        gravityObjects.add(new GravityObject(m, applyGravity, isCollidable,0));
    }

    public void clear() {
        gravityObjects.clear();
    }
    public void updateElements(List<PhysicsObject> physicObjects) {
        gravityObjects.clear();
        for (int i = 0; i < physicObjects.size(); i++) {
            add(physicObjects.get(i).object, physicObjects.get(i).hasGravity, physicObjects.get(i).isCollidable);
        }
    }

    private void increaseTicksAt(int index) {
        gravityObjects.get(index).ticksSinceStart = gravityObjects.get(index).ticksSinceStart + 1;
    }

    private void resetTicksAt(int index) {
        gravityObjects.get(index).ticksSinceStart = 0;
    }

    public void tick() {
        for (int i = 0; i < gravityObjects.size(); i++) {
            Movable active = gravityObjects.get(i).object;
            int collisionIndex = -1;

            if (gravityObjects.get(i).applyGravity) {

                boolean isColliding = false;
                Movable copy = active.getCopy();
                copy.move(new Vec2D(0, getDeltaS(gravityObjects.get(i).ticksSinceStart)));

                for (int j = 0; j < gravityObjects.size(); j++) {
                    if (i != j && gravityObjects.get(i).isCollidable &&
                            gravityObjects.get(j).isCollidable &&
                            Utility.isColliding(gravityObjects.get(j).object, copy)) {

                        isColliding = true;
                        collisionIndex = j;
                        break;
                    }
                }

                //Not colliding
                if (!isColliding && active.getTop() < bottomYCord) {
                    active.move(new Vec2D(0, getDeltaS(gravityObjects.get(i).ticksSinceStart)));
                    increaseTicksAt(i);

                //Colliding with the bottom
                } else if (active.getTop() >= bottomYCord){
                    if (active.getTop() != bottomYCord) {
                        active.move(new Vec2D(0, bottomYCord - active.getTop()));
                        resetTicksAt(i);
                    }

                //Colliding with another object
                } else {
                    resetTicksAt(i);
                    moveToCollidingObject(active, gravityObjects.get(collisionIndex).object,
                            getDeltaS(gravityObjects.get(i).ticksSinceStart));
                }
            }
        }
    }

    private void moveToCollidingObject(Movable activeObject, Movable standingObject, double maxDeltaS) {

        int precision = 5;
        double current = maxDeltaS;
        boolean nextDown = true;

        for (int i = 0; i < precision; i++) {
            current = current / 2;

            if (nextDown) {
                activeObject.move(new Vec2D(0, current));
            } else {
                activeObject.move(new Vec2D(0, -current));
            }

            if (Utility.isColliding(activeObject, standingObject)) {
                nextDown = false;
            } else {
                nextDown = true;
            }
        }
    }

    private double getDeltaS(int nTicks) {
        if (nTicks == 0) {
            return 0;
        }
        return 0.5 * GRAVITY_CONSTANT * (nTicks) * (nTicks)
                - 0.5 * GRAVITY_CONSTANT * ((nTicks - 1)) * ((nTicks - 1));
    }
}
