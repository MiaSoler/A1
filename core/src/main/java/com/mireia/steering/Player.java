package com.mireia.steering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;

public class Player {

    // Current position of the spaceship in the game world
    public Vector2 position;

    // Velocity vector used to move the player
    public Vector2 velocity;

    // Maximum movement speed of the player
    public float speed = 200f;

    // Rotation of the spaceship (used when drawing the sprite)
    public float rotation;

    // Player health value (when it reaches 0 → game over)
    public int health;

    // Player sprite size (used for rendering and boundaries)
    public float width;
    public float height;

    // Collision radius used for circle collision detection
    public float radius;


    public Player(float x, float y, float width, float height, float radius) {

        // Initial position of the player spaceship
        position = new Vector2(x, y);

        // Velocity starts at zero (ship is not moving)
        velocity = new Vector2();

        // Initial rotation
        rotation = 0f;
        
        // Player starts with full health
        health = 100;

        // Store player sprite dimensions
        this.width = width;
        this.height = height;

        // Store collision radius
        this.radius = radius;
    }


    public void update(float dt) {

        // Calculate half dimensions to clamp player inside screen
        float halfWidth = width / 2f;
        float halfHeight = height / 2f;

        // Minimum and maximum X positions allowed
        float minX = halfWidth;
        float maxX = Gdx.graphics.getWidth() - halfWidth;

        // Minimum and maximum Y positions allowed
        float minY = halfHeight;

        // Prevent player from entering the UI area at the top
        float maxY = Gdx.graphics.getHeight() - Main.UI_HEIGHT - halfHeight;

        // Reset velocity every frame before applying input
        velocity.setZero();
 
        // Handle keyboard input to move the spaceship
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            velocity.x += 1;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            velocity.x -= 1;

        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
            velocity.y -= 1;

        if (Gdx.input.isKeyPressed(Input.Keys.UP))
            velocity.y += 1;


        // If the player is moving
        if (velocity.len() > 0) {

            // Normalize direction and apply movement speed
            velocity.nor().scl(speed);

            // Rotate the spaceship to face the movement direction
            rotation = velocity.angleDeg() - 90f;
        }       


        // Clamp the player inside the screen boundaries
        position.x = Math.max(minX, Math.min(position.x, maxX));
        position.y = Math.max(minY, Math.min(position.y, maxY));

        // Apply movement based on velocity and frame time
        position.add(velocity.scl(dt));
    }


    // Reduce player's health when hit by an enemy
    public void takeDamage(int amount) {

        // Subtract damage from health
        health -= amount;
    
        // Prevent health from going below zero
        if (health < 0) {
            health = 0;
        }

        // Debug message showing remaining health
        System.out.println("Player health: " + health);
    }
    
}