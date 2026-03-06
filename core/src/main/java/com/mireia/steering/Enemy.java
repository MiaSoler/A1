package com.mireia.steering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

public class Enemy {

    // Current position of the enemy in the game world
    public Vector2 position;

    // Current velocity vector controlling enemy movement
    public Vector2 velocity;

    // Maximum movement speed of the enemy
    public float maxSpeed = 150f;

    // Maximum steering force (limits how fast the enemy can turn)
    public float maxForce = 200f;

    // Base values used to scale difficulty when levels increase
    private final float baseMaxSpeed = 150f;
    private final float baseMaxForce = 50f;
    private final float baseSeparationWeight = 1.5f;

    // Rotation of the asteroid sprite so it faces its movement direction
    public float rotation;

    // Indicates whether the enemy has exploded
    public boolean exploded = false;

    // True if destroyed by bullet instead of collision
    public boolean explodedBullet = false;

    // Determines if this enemy is the leader of the formation
    public boolean isLeader = false;

    // Weight applied to separation steering behaviour
    public float separationWeight = 1.5f;

    // Collision radius used for circular collision detection
    public float radius;

    // Offset relative to the leader used for formation positioning
    // Leader enemies have no offset (null)
    public Vector2 formationOffset;


    public Enemy(float posX, float posY, boolean isLeader, float radius) {

        // Initialize enemy position
        position = new Vector2(posX, posY);

        // Enemy initially not moving
        velocity = new Vector2();

        // Followers receive formation offsets later
        formationOffset = null;

        // Set whether this enemy is the leader
        this.isLeader = isLeader;

        this.radius = radius;
    }


    public void update(float dt, Vector2 target, Vector2 separationForce, Vector2 playerPosition, float playerRadius) {

        // Do nothing if enemy already exploded
        if (exploded) return;

        // Distance threshold where behaviour switches from seek to arrive
        float switchDistance = 100f;

        // Collision radius between enemy and player
        float combinedRadius = radius + playerRadius;

        // Squared version used for faster collision detection
        float combinedRadiusSquared = combinedRadius * combinedRadius;

        // Distance from enemy to target (leader or player)
        float distanceLeader = position.dst2(target);

        // Distance from enemy to player
        float distancePlayer = position.dst2(playerPosition);

        float halfSize = 20f;

        Vector2 steering;

        // Leader seeks the player
        // Followers seek the leader formation position
        if (distanceLeader > switchDistance) {

            // FAR → SEEK behavior
            steering = seek(target);

        } else {

            // CLOSE → ARRIVE behavior (slow down when near target)
            steering = arrive(target);
        }

        // Add separation force to avoid overlapping other enemies
        if (separationForce != null) {
            steering.add(separationForce.scl(separationWeight));
        }

        // Apply steering acceleration
        velocity.add(steering.cpy().scl(dt));

        // Limit velocity to maxSpeed
        if (velocity.len() > maxSpeed)
            velocity.nor().scl(maxSpeed);

        // Update enemy position
        position.add(velocity.cpy().scl(dt));

        // Rotate sprite to face movement direction
        if (velocity.len() > 0) {
            rotation = velocity.angleDeg() - 90f;
        }

        // Collision logic
        // Leader explodes when reaching the player
        // Followers explode when reaching the player as well
        if ((isLeader && distanceLeader < combinedRadiusSquared) ||
            (!isLeader && distancePlayer < combinedRadiusSquared)) {

            exploded = true;
        }

        // Keep enemies inside screen boundaries
        float minY = halfSize;
        float maxX = Gdx.graphics.getWidth() - minY;
        float maxY = Gdx.graphics.getHeight() - Main.UI_HEIGHT - minY;

        position.x = Math.max(minY, Math.min(position.x, maxX));
        position.y = Math.max(minY, Math.min(position.y, maxY));
    }


    // SEEK behavior
    // Enemy moves directly toward a target at full speed
    public Vector2 seek(Vector2 target) {

        Vector2 desired = target.cpy()
                .sub(position)
                .nor()
                .scl(maxSpeed);

        return computeSteering(desired);
    }


    // ARRIVE behavior
    // Enemy slows down when approaching the target
    public Vector2 arrive(Vector2 target) {

        float slowRadius = 120f;
        float speed = this.maxSpeed;

        Vector2 desired = target.cpy().sub(position);
        float distance = desired.len();

        if (distance < 0.001f) {
            return new Vector2();
        }

        // Reduce speed when inside the slow radius
        if (distance < slowRadius) {
            speed = this.maxSpeed * (distance / slowRadius);
        }

        desired.nor().scl(speed);

        return computeSteering(desired);
    }


    // Converts desired velocity into a steering force
    private Vector2 computeSteering(Vector2 desired) {

        // Steering = desired velocity − current velocity
        Vector2 steering = desired.sub(velocity);

        // Limit steering force
        if (steering.len() > maxForce) {
            steering.nor().scl(maxForce);
        }

        return steering;
    }


    // Increase enemy difficulty based on current level
    public void setDifficulty(int level) {

        // Increase speed
        this.maxSpeed = baseMaxSpeed + level * 10f;

        // Increase turning force
        this.maxForce = baseMaxForce + level * 5f;

        // Increase separation strength
        this.separationWeight = baseSeparationWeight + level * 0.2f;
    }
}