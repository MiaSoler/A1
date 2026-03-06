package com.mireia.steering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

public class Bullet {

    // Current position of the bullet in the game world
    public Vector2 position;

    // Velocity vector controlling bullet movement direction and speed
    public Vector2 velocity;

    // Bullet travel speed (units per second)
    public float speed = 400f;

    // Collision radius used for circular collision detection
    public float radius = 8f;

    // Determines if the bullet is still active in the game
    // If false → bullet will be removed from the bullet array
    public boolean active = true;

    // Visual length of the bullet line when drawn
    public float length = 12f;


    public Bullet(Vector2 startPosition, float rotation) {

        // Copy the spawn position (ship nose) so bullet starts there
        position = startPosition.cpy();

        // Create velocity vector pointing in the direction of the ship rotation
        // (0,1) represents upward direction before rotation is applied
        velocity = new Vector2(0, 1)
                .setAngleDeg(rotation)
                .scl(speed);
    }


    public void update(float dt) {

        // Update bullet position using velocity and frame delta time
        position.add(velocity.cpy().scl(dt));

        // Remove bullet if it goes outside screen boundaries
        // This prevents bullets from existing forever
        if (position.x < 0 || position.x > Gdx.graphics.getWidth()
                || position.y < 0 || position.y > Gdx.graphics.getHeight()) {

            active = false;
        }
    }
}