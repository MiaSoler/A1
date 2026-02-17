package com.mireia.steering;

import com.badlogic.gdx.math.Vector2;


public class Enemy {
    public Vector2 position;
    public Vector2 velocity;

    public float maxSpeed = 150f;
    public float maxForce = 200f;
    public float rotation;
    public boolean exploded = false;

     // Formation
    public Vector2 formationOffset; // null if leader

    public Enemy(float posX, float posY) {
        position = new Vector2(posX, posY);
        velocity = new Vector2();
        formationOffset = null; 
    }

    public void update(float dt, Vector2 target, Vector2 separationForce) {
        Vector2 steering = seek(target);
        float explosionRadius = 30f;

        if (separationForce != null) {
            steering.add(separationForce);
        }

        // Limit steering
        if (steering.len() > maxForce)
            steering.nor().scl(maxForce);

        velocity.add(steering.scl(dt));

        if (velocity.len() > maxSpeed)
            velocity.nor().scl(maxSpeed);

        position.add(velocity.cpy().scl(dt));

        if (velocity.len() > 0) {
            rotation = velocity.angleDeg() - 90f;
        }

        if (position.dst(target) < explosionRadius) {
            exploded = true;
        }

    }
    //far away full speed

    private Vector2 seek(Vector2 target) {
        Vector2 desired = target.cpy()
                .sub(position)
                .nor()
                .scl(maxSpeed);

        return desired.sub(velocity);
    }

    //near target or very close -> slow down speed
    public Vector2 arrive(Vector2 target) {

        float slowRadius = 120f;
        float maxSpeed = 200f;
    
        Vector2 desired = target.cpy().sub(position);
        float distance = desired.len();
    
        if (distance < 0.001f) {
            return new Vector2(); 
        }
    
        float speed = maxSpeed;
    
        if (distance < slowRadius) {
            speed = maxSpeed * (distance / slowRadius);
        }
    
        desired.nor().scl(speed);
    
        Vector2 steering = desired.sub(velocity);
    
        return steering;
    }    
}
