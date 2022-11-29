package Controls;

import Basics.Collider;
import Basics.Utility;
import Basics.Vec2D;
import Controller.PhysicsObject;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class Gravity {

    private static class GravityObject {
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

    private final List<GravityObject> gravityObjects;
    private final double GRAVITY_CONSTANT = 9.81;
    private final double bottomYCord;
    private final double gameFPS;

    public Gravity(List<Collider> objects, List<Boolean> applyGravity, List<Boolean> collidable,
                   List<Double> bounciness, double bottomYCord, double gameFPS) {

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
        this.gameFPS = gameFPS;
    }

    public Gravity(double bottomYCord, double gameFPS) {
        gravityObjects = new ArrayList<>();
        this.bottomYCord = bottomYCord;
        this.gameFPS = gameFPS;
    }

    public void add(PhysicsObject o) {
        gravityObjects.add(new GravityObject(o.object, o.hasGravity, o.isCollidable, 0, o.bounciness));
    }

    public void add(Collider m, boolean applyGravity, boolean isCollidable, double bounciness) {
        gravityObjects.add(new GravityObject(m, applyGravity, isCollidable, 0, bounciness));
    }

    public void giveGravityAt(int index) {
        gravityObjects.get(index).applyGravity = true;
    }

    public void clear() {
        gravityObjects.clear();
    }

    public void updateElements(List<PhysicsObject> physicObjects) {
        gravityObjects.clear();
        for (PhysicsObject physicObject : physicObjects) {
            add(physicObject.object, physicObject.hasGravity,
                    physicObject.isCollidable, physicObject.bounciness);
        }
    }

    public void tick() {
        for (int i = 0; i < gravityObjects.size(); i++) {
            GravityObject active = gravityObjects.get(i);
            int collisionIndex = -1;

            if (gravityObjects.get(i).upwardsForce) {

                double currentUpwardsForce = getDeltaS(active.ticksSinceStart) * active.bounciness;
                active.object.move(new Vec2D(0, -1 * currentUpwardsForce));

                for (int j = 0; j < gravityObjects.size(); j++) {
                    if (i != j && active.isCollidable &&
                            gravityObjects.get(j).isCollidable &&
                            Utility.isColliding(gravityObjects.get(j).object, active.object)) {

                        collisionIndex = j;
                        break;
                    }
                }

                handleBounce(active, collisionIndex);

            } else if (gravityObjects.get(i).applyGravity) {

                Collider copy = active.object.getCopy();
                copy.move(new Vec2D(0, getDeltaS(active.ticksSinceStart)));

                if (copy.getTop() >= bottomYCord) {
                    collisionIndex = Integer.MAX_VALUE;
                } else {
                    for (int j = 0; j < gravityObjects.size(); j++) {
                        if (i != j && active.isCollidable &&
                                gravityObjects.get(j).isCollidable &&
                                Utility.isColliding(gravityObjects.get(j).object, copy)) {

                            collisionIndex = j;
                            break;
                        }
                    }
                }

                handleCollision(active, collisionIndex);
            }
        }
    }

    private void handleCollision(GravityObject active, int collisionIndex) {

        //Not colliding
        if (collisionIndex == -1 && active.object.getTop() < bottomYCord) {
            active.object.move(new Vec2D(0, getDeltaS(active.ticksSinceStart)));
            active.ticksSinceStart++;
            active.movedToCollidingObject = false;

            //Colliding with the bottom
        } else if (collisionIndex == Integer.MAX_VALUE){
            if (active.bounciness > 0 && active.ticksSinceStart > 10) {
                active.upwardsForce = true;
            } else if (active.object.getTop() != bottomYCord) {
                active.object.move(new Vec2D(0, bottomYCord - active.object.getTop()));
                active.ticksSinceStart = 0;
            }

            //Colliding with another object
        } else if (!active.movedToCollidingObject
                && gravityObjects.get(collisionIndex).ticksSinceStart == 0){

            if (active.bounciness > 0 && active.ticksSinceStart > 10) {
                active.upwardsForce = true;
            } else if (active.object.getCenter().y < gravityObjects.get(collisionIndex).object.getCenter().y) {
                moveToCollidingObject(active.object, gravityObjects.get(collisionIndex).object,
                        getDeltaS(active.ticksSinceStart));
                active.ticksSinceStart = 0;
                active.movedToCollidingObject = true;
            } else {
                active.object.move(new Vec2D(0, getDeltaS(active.ticksSinceStart)));
                active.ticksSinceStart = 0;
                active.movedToCollidingObject = false;
            }
        }
    }

    private void handleBounce(GravityObject active, int collisionIndex) {

        //Not colliding
        if (collisionIndex == -1) {
            active.ticksSinceStart--;
            if (active.ticksSinceStart <= 0) {
                active.upwardsForce = false;
            }

        //Colliding with another object
        } else {
            active.upwardsForce = false;
            active.ticksSinceStart *= (active.bounciness) * (active.bounciness);
            if (gravityObjects.get(collisionIndex).bounciness > 0) {
                GravityObject collider = gravityObjects.get(collisionIndex);
                double colliderUpwardsForce = getDeltaS(collider.ticksSinceStart) * collider.bounciness;
                collider.object.move(new Vec2D(0, -1 * colliderUpwardsForce));
                collider.upwardsForce = true;
            }
        }
    }

    private void moveToCollidingObject(Collider activeObject,
                                       Collider standingObject, double maxDeltaS) {

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

            nextDown = !Utility.isColliding(activeObject, standingObject);
        }
    }

    private double getDeltaS(int nTicks) {
        if (nTicks == 0) {
            return 0;
        }

        return 0.5 * (GRAVITY_CONSTANT / gameFPS) * (nTicks) * (nTicks)
                - 0.5 * (GRAVITY_CONSTANT / gameFPS) * ((nTicks - 1)) * ((nTicks - 1));
    }
}
