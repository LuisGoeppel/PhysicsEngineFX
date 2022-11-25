package Controls;

import Basics.Collider;
import Basics.Utility;
import Basics.Vec2D;
import Controller.PhysicsObject;

import java.util.ArrayList;
import java.util.List;

public class Gravity {

    private class GravityObject {
        public Collider object;
        public Boolean applyGravity;
        public Boolean isCollidable;
        public Boolean movedToCollidingObject;
        public double bounciness;
        public boolean upwardsForce;
        public int ticksSinceStart;

        public GravityObject(Collider object, Boolean applyGravity, Boolean isCollidable,
                             int ticksSinceStart, double bounciness) {

            if (bounciness >= 1) {
                throw new IllegalArgumentException("Yeah mate that's not gonna work");
            }
            this.object = object;
            this.applyGravity = applyGravity;
            this.ticksSinceStart = ticksSinceStart;
            this.isCollidable = isCollidable;
            this.bounciness = bounciness;
            movedToCollidingObject = false;
            upwardsForce = false;
        }
    }

    private List<GravityObject> gravityObjects;
    private final double GRAVITY_CONSTANT = 0.1635; //60 FPS
    private double bottomYCord;

    public Gravity(List<Collider> objects, List<Boolean> applyGravity,
                   List<Boolean> collidable, List<Double> bounciness, double bottomYCord) {

        if (objects.size() != applyGravity.size()) {
            throw new IllegalArgumentException("Sizes do not match!");
        }
        gravityObjects = new ArrayList<>();
        for (int i = 0; i < objects.size(); i++) {
            GravityObject gravityObject = new GravityObject(objects.get(i),
                    applyGravity.get(i), collidable.get(i), 0, bounciness.get(i));
            gravityObjects.add(gravityObject);
        }
        this.bottomYCord = bottomYCord;
    }

    public Gravity(double bottomYCord) {
        gravityObjects = new ArrayList<>();
        this.bottomYCord = bottomYCord;
    }

    public void add(PhysicsObject o) {
        gravityObjects.add(new GravityObject(o.object, o.hasGravity, o.isCollidable, 0, o.bounciness));
    }

    public void add(Collider m, boolean applyGravity, boolean isCollidable, double bounciness) {
        gravityObjects.add(new GravityObject(m, applyGravity, isCollidable,0, bounciness));
    }

    public void giveGravityAt(int index) {
        gravityObjects.get(index).applyGravity = true;
    }

    public void clear() {
        gravityObjects.clear();
    }

    public void updateElements(List<PhysicsObject> physicObjects) {
        gravityObjects.clear();
        for (int i = 0; i < physicObjects.size(); i++) {
            add(physicObjects.get(i).object, physicObjects.get(i).hasGravity,
                    physicObjects.get(i).isCollidable, physicObjects.get(i).bounciness);
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
            GravityObject active = gravityObjects.get(i);
            int collisionIndex = -1;
            boolean isColliding = false;

            if (gravityObjects.get(i).upwardsForce) {

                //Bounce Fun
                double currentUpwardsForce = getDeltaS(active.ticksSinceStart) * active.bounciness;
                active.object.move(new Vec2D(0, -1 * currentUpwardsForce));

                for (int j = 0; j < gravityObjects.size(); j++) {
                    if (i != j && active.isCollidable &&
                            gravityObjects.get(j).isCollidable &&
                            Utility.isColliding(gravityObjects.get(j).object, active.object)) {

                        isColliding = true;
                        collisionIndex = j;
                        break;
                    }
                }

                if (!isColliding) {
                    active.ticksSinceStart--;
                    if (active.ticksSinceStart <= 0) {
                        active.upwardsForce = false;
                    }
                } else {
                    active.upwardsForce = false;
                    active.ticksSinceStart *= (active.bounciness) * (active.bounciness);
                    if (gravityObjects.get(collisionIndex).bounciness > 0) {
                        GravityObject collider = gravityObjects.get(collisionIndex);
                        double colliderUpwardsForce = getDeltaS(collider.ticksSinceStart) * collider.bounciness;
                        collider.object.move(new Vec2D(0, -1 * colliderUpwardsForce));
                    }
                }

            } else if (gravityObjects.get(i).applyGravity) {

                Collider copy = active.object.getCopy();
                copy.move(new Vec2D(0, getDeltaS(active.ticksSinceStart)));

                for (int j = 0; j < gravityObjects.size(); j++) {
                    if (i != j && active.isCollidable &&
                            gravityObjects.get(j).isCollidable &&
                            Utility.isColliding(gravityObjects.get(j).object, copy)) {

                        isColliding = true;
                        collisionIndex = j;
                        break;
                    }
                }

                //Not colliding
                if (!isColliding && active.object.getTop() < bottomYCord) {
                    active.object.move(new Vec2D(0, getDeltaS(gravityObjects.get(i).ticksSinceStart)));
                    increaseTicksAt(i);
                    active.movedToCollidingObject = false;

                //Colliding with the bottom
                } else if (active.object.getTop() >= bottomYCord){
                    if (active.bounciness > 0 && active.ticksSinceStart > 10) {
                        active.upwardsForce = true;
                    } else if (active.object.getTop() != bottomYCord) {
                        active.object.move(new Vec2D(0, bottomYCord - active.object.getTop()));
                        resetTicksAt(i);
                    }

                //Colliding with another object
                } else if (!active.movedToCollidingObject
                        && gravityObjects.get(collisionIndex).ticksSinceStart == 0){

                    if (active.bounciness > 0 && active.ticksSinceStart > 10) {
                        active.upwardsForce = true;
                    } else if (active.object.getCenter().y < gravityObjects.get(collisionIndex).object.getCenter().y) {
                        moveToCollidingObject(active.object, gravityObjects.get(collisionIndex).object,
                                getDeltaS(active.ticksSinceStart));
                        resetTicksAt(i);
                        gravityObjects.get(i).movedToCollidingObject = true;
                    } else {
                        active.object.move(new Vec2D(0, getDeltaS(active.ticksSinceStart)));
                        increaseTicksAt(i);
                        active.movedToCollidingObject = false;
                    }
                }
            }
        }
    }

    private void moveToCollidingObject(Collider activeObject, Collider standingObject, double maxDeltaS) {

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
