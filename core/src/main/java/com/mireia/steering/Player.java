package com.mireia.steering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;

public class Player {
    public Vector2 position;
    public Vector2 velocity;
    public float speed = 200f;
    public float rotation;

    public int health;
    public float width;
    public float height;
    public float radius;

    public Player(float x, float y, float width, float height, float radius) {
        //initial position
        position = new Vector2(x, y);
        velocity = new Vector2();
        rotation = 0f;
        
        health = 100;
        //player size
        this.width = width;
        this.height = height;
    }

    public void update(float dt) {
        //Player stay withing screen game
        float halfWidth = width / 2f;
        float halfHeight = height / 2f;

        float minX = halfWidth;
        float maxX = Gdx.graphics.getWidth() - halfWidth;

        float minY = halfHeight;
        float maxY = Gdx.graphics.getHeight() - Main.UI_HEIGHT - halfHeight;

        velocity.setZero();
 
        //user key input to make spaceship move
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            velocity.x += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            velocity.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
            velocity.y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.UP))
            velocity.y += 1;

        if (velocity.len() > 0) {
            velocity.nor().scl(speed);

            // Rotate ship in movement direction
            rotation = velocity.angleDeg() - 90f;
        }       
        //update position
        position.x = Math.max(minX, Math.min(position.x, maxX));
        position.y = Math.max(minY, Math.min(position.y, maxY));

        position.add(velocity.scl(dt));
    }
    // function to reduce player's health when  it is hit
    public void takeDamage(int amount) {
        health -= amount;
    
        if (health < 0) {
            health = 0;
        }
        //add gameover and option to restart the game
        System.out.println("Player health: " + health);
    }
    
}
