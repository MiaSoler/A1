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

    public Player(float x, float y) {
        position = new Vector2(x, y);
        velocity = new Vector2();
        rotation = 0f;

        health = 100;
    }

    public void update(float dt) {
        velocity.setZero();
 
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

        position.add(velocity.scl(dt));
    }

    public void takeDamage(int amount) {
        health -= amount;
    
        if (health < 0) {
            health = 0;
        }
        //add gameover and option to restart the game
        System.out.println("Player health: " + health);
    }
    
}
