package Controls;

import Basics.*;
import Controller.PhysicsObject;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class Gravity {

    private static class GravityObject {

        public int objectID;
        public Collider object;
        public Boolean applyGravity;
        public Boolean isCollidable;
        public double bounciness;
        public boolean upwardsForce;
        public int ticksSinceStart;
        public boolean movedToCollidingObject;
        public Vec2D nextMove;
        public GravityObject interactionPartner;

        public PhysicsObject matchingPhysicsObject;


        public GravityObject(PhysicsObject physicsObject) {

            this.object = physicsObject.object;
            this.applyGravity = physicsObject.hasGravity;
            this.isCollidable = physicsObject.isCollidable;
            this.bounciness = physicsObject.bounciness;
            this.objectID = physicsObject.objectID;
            this.matchingPhysicsObject = physicsObject;

            upwardsForce = false;
            ticksSinceStart = 0;
            movedToCollidingObject = false;
            nextMove = new Vec2D(0, 0);
            interactionPartner = null;
        }
    }

    private final List<GravityObject> gravityObjects;
    private final double GRAVITY_CONSTANT = 9.81;
    private final double maxRight = 754;
    private final double gameFPS;
    private int playerIndex;

    public Gravity(double gameFPS) {
        gravityObjects = new ArrayList<>();
        this.gameFPS = gameFPS;
        playerIndex = -1;
    }

    public void add(PhysicsObject o) {
        gravityObjects.add(new GravityObject(o));
    }

    public void setPlayer(Collider collider) {
        for (int i = 0; i < gravityObjects.size(); i++) {
            if (collider.equals(gravityObjects.get(i).object)) {
                playerIndex = i;
            }
        }
    }

    public void giveGravityAt(int index) {
        gravityObjects.get(index).applyGravity = true;
    }

    public void clear() {
        gravityObjects.clear();
        playerIndex = -1;
    }

    public void movePlayerLeft(double distance) {
        if (playerIndex != -1) {
            if (gravityObjects.get(playerIndex).object.getLeft() - distance > 0) {
                gravityObjects.get(playerIndex).nextMove.x = -distance;
            } else {
                gravityObjects.get(playerIndex).nextMove.x = - gravityObjects.get(playerIndex).object.getLeft();
            }
        }
    }

    public void movePlayerRight(double distance) {
        if (playerIndex != -1) {
            if (gravityObjects.get(playerIndex).object.getRight() + distance < maxRight) {
                gravityObjects.get(playerIndex).nextMove.x = distance;
            } else {
                gravityObjects.get(playerIndex).nextMove.x =
                        maxRight - gravityObjects.get(playerIndex).object.getRight();
            }
        }
    }

    public void playerJump(int jumpStrength) {
        if (playerIndex != -1) {
            gravityObjects.get(playerIndex).upwardsForce = true;
            gravityObjects.get(playerIndex).ticksSinceStart = jumpStrength;
        }
    }

    public void updateElements(List<PhysicsObject> physicObjects) {
        GravityObject o = null;
        if (playerIndex != -1) {
            o = gravityObjects.get(playerIndex);
            playerIndex = -1;
        }
        gravityObjects.clear();
        for (PhysicsObject physicObject : physicObjects) {
            add(physicObject);
        }
        if (o != null) {
            for (int i = 0; i < gravityObjects.size(); i++) {
                if (gravityObjects.get(i).object.equals(o.object)) {
                    playerIndex = i;
                }
            }
        }
    }

    public void tick() {
        for (int i = 0; i < gravityObjects.size(); i++) {

            GravityObject active = gravityObjects.get(i);
            int collisionIndex = -1;

            //Bounce
            if (gravityObjects.get(i).upwardsForce) {

                Collider copy = active.object.getCopy();
                copy.move(new Vec2D(active.nextMove.x, getDeltaS(active.ticksSinceStart) * active.bounciness));

                for (int j = 0; j < gravityObjects.size(); j++) {
                    if (i != j && active.isCollidable &&
                            gravityObjects.get(j).isCollidable &&
                            Utility.isColliding(gravityObjects.get(j).object, copy)) {

                        collisionIndex = j;
                        break;
                    }
                }

                handleBounce(active, collisionIndex);

                //Gravity
            } else if (gravityObjects.get(i).applyGravity) {

                if (gravityObjects.get(i).interactionPartner != null) {
                    handleObjectInteraction(active, active.interactionPartner);
                } else {
                    Collider copy = active.object.getCopy();
                    copy.move(new Vec2D(0, -1 * getDeltaS(active.ticksSinceStart)));

                    if (copy.getBottom() <= 0) {
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
        handleXMovement();
        moveObjects();
    }

    private void handleCollision(GravityObject active, int collisionIndex) {

        //Not colliding
        if (collisionIndex == -1) {
            active.nextMove.y = -1 * getDeltaS(active.ticksSinceStart);
            active.ticksSinceStart++;
            active.movedToCollidingObject = false;

            //Colliding with the bottom
        } else if (collisionIndex == Integer.MAX_VALUE){
            if (active.bounciness > 0 && active.ticksSinceStart > 10) {
                active.upwardsForce = true;
            } else {
                if (active.object.getBottom() > 0) {
                    active.nextMove.y = -1 * active.object.getBottom();
                    active.ticksSinceStart = 0;
                } else {
                    active.nextMove = new Vec2D(active.nextMove.x / 1.3, 0);
                }
                active.movedToCollidingObject = true;
            }

            //Collision with another object
        } else if (!active.movedToCollidingObject) {
            GravityObject collider = gravityObjects.get(collisionIndex);
            active.nextMove = new Vec2D(active.nextMove.x, 0);

            if (active.bounciness > 0 && active.ticksSinceStart > 10) {
                handleBounceX(active, collider);
                active.nextMove.y = getDeltaS(active.ticksSinceStart) * active.bounciness;
                active.upwardsForce = true;
                active.ticksSinceStart--;
            } else {
                moveToCollidingObject(active, collider, new Vec2D(0, -1),
                        getDeltaS(active.ticksSinceStart));
            }
        }
    }

    private void handleObjectInteraction(GravityObject active, GravityObject passive) {
        active.nextMove = new Vec2D(0, 0);

        if (active.object.getObjectType().equals(MovableObjectType.CIRCLE) &&
                passive.object.getObjectType().equals(MovableObjectType.ROTATION_BOX)) {

            double degCollider = ((RotationBox2D)(passive.object)).getRotationAngleRad();
            active.ticksSinceStart += 1;
            double degOut = Math.PI + degCollider;
            active.nextMove = new Vec2D(Math.cos(degOut) * getDeltaS((int)
                    (active.ticksSinceStart / 1.5)) * -Math.sin(degOut),
                    Math.sin(degOut) * getDeltaS((int)(active.ticksSinceStart / 1.5))
                    * -Math.sin(degOut));

        }
        if (active.nextMove.y == 0) {
            active.ticksSinceStart = 0;
        }
        Collider copy = active.object.getCopy();
        copy.move(new Vec2D(0, -1));
        if (!Utility.isColliding(copy, passive.object)) {
            active.interactionPartner = null;
            if (passive.object.getObjectType() == MovableObjectType.ROTATION_BOX) {
                active.ticksSinceStart = (int)(active.ticksSinceStart * Math.pow(
                        Math.sin(((RotationBox2D)(passive.object)).getRotationAngleRad()), 2));
            }
        }
    }

    private void handleBounce(GravityObject active, int collisionIndex) {

        //Not colliding
        if (collisionIndex == -1) {
            active.nextMove = new Vec2D(active.nextMove.x, getDeltaS(active.ticksSinceStart) * active.bounciness);
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
                collider.nextMove.y = getDeltaS(collider.ticksSinceStart) * collider.bounciness;
                collider.upwardsForce = true;
            }
        }
    }

    private void handleBounceX(GravityObject active, GravityObject collider) {
        if (active.object.getObjectType() == MovableObjectType.CIRCLE
                && collider.object.getObjectType() == MovableObjectType.ROTATION_BOX) {
            double xMovement = Math.sin(((RotationBox2D)(collider.object)).getRotationAngleRad() + Math.PI);
            active.nextMove.x = 2 * xMovement * active.bounciness;
            System.out.println(active.nextMove.x);
        }
    }

    private void moveToCollidingObject(GravityObject activeObject, GravityObject standingObject,
                                       Vec2D direction, double maxDeltaS) {
        if (maxDeltaS > 0.5) {
            direction = direction.getNormalizedVector();
            Collider activeCopy = activeObject.object.getCopy();

            int precision = 5;
            double current = maxDeltaS;
            boolean toCollider = true;

            for (int i = 0; i < precision; i++) {
                current = current / 2;

                if (toCollider) {
                    activeCopy.move(direction.mult(current));
                } else {
                    activeCopy.move(direction.mult(-current));
                }
                toCollider = !Utility.isColliding(activeCopy, standingObject.object);
            }
            if (Utility.isColliding(activeCopy, standingObject.object)) {
                activeCopy.move(direction.mult(-current));
            }

            activeObject.nextMove = activeCopy.getCenter().sub(activeObject.object.getCenter());

        } else {
            activeObject.nextMove = new Vec2D(activeObject.nextMove.x, 0);
        }
        activeObject.movedToCollidingObject = true;
        activeObject.interactionPartner = standingObject;
    }

    private void handleXMovement() {
        for (int i = 0; i < gravityObjects.size(); i++) {
            GravityObject gravityObject = gravityObjects.get(i);
            if (gravityObject.nextMove.x != 0) {
                Collider copy = gravityObject.object.getCopy();
                copy.move(gravityObject.nextMove);
                int collisionIndex = -1;
                if (copy.getLeft() < 0 || copy.getRight() > maxRight) {
                    collisionIndex = Integer.MAX_VALUE;
                } else {
                    for (int j = 0; j < gravityObjects.size(); j++) {
                        if (i != j && gravityObject.isCollidable &&
                                gravityObjects.get(j).isCollidable &&
                                Utility.isColliding(gravityObjects.get(j).object, copy)) {

                            collisionIndex = j;
                            break;
                        }
                    }
                }
                handleSidewardsCollision(gravityObject, collisionIndex);
            }
        }
    }

    private void handleSidewardsCollision(GravityObject activeObject, int collisionIndex) {
        if (collisionIndex == Integer.MAX_VALUE) {
            activeObject.nextMove.x *= -1;
        } else if (collisionIndex != -1) {
            moveToCollidingObject(activeObject, gravityObjects.get(collisionIndex), activeObject.nextMove,
                    activeObject.nextMove.getLength());
        }
    }

    private void moveObjects() {
        for (GravityObject gravityObject : gravityObjects) {
            if (gravityObject.nextMove.x != 0 || gravityObject.nextMove.y != 0) {
                gravityObject.object.move(gravityObject.nextMove);
                gravityObject.matchingPhysicsObject.hasChangedPosition = true;
            }
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
