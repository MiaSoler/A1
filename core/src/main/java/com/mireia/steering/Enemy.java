package com.mireia.steering;

import com.badlogic.gdx.math.Vector2;


public class Enemy {
    public Vector2 position;
    public Vector2 velocity;

    public float maxSpeed = 150f;
    public float maxForce = 200f;
    public float rotation;
    public boolean exploded = false;
    public boolean isLeader = false;

     // Formation
    public Vector2 formationOffset; // null if leader

    public Enemy(float posX, float posY) {
        position = new Vector2(posX, posY);
        velocity = new Vector2();
        formationOffset = null; 
    }

    public void update(float dt, Vector2 target, Vector2 separationForce) {

        if (exploded) return;

        float switchDistance = 100f;   // distance where we switch to ARRIVE
        float explosionRadius = 30f;    
        float distance = position.dst(target);

        Vector2 steering;

        if (distance > switchDistance) {
            steering = seek(target);       // FAR → SEEK
        } else {
            steering = arrive(target);     // CLOSE → ARRIVE
        }
     

        if (separationForce != null) {
            steering.add(separationForce);
        }

        velocity.add(steering.scl(dt));

        if (velocity.len() > maxSpeed)
            velocity.nor().scl(maxSpeed);

        position.add(velocity.cpy().scl(dt));

        if (velocity.len() > 0) {
            rotation = velocity.angleDeg() - 90f;
        }

        if (!exploded && position.dst(target) < 30f) {
            exploded = true;
        }

    }

    //far away full speed

    public Vector2 seek(Vector2 target) {
        Vector2 desired = target.cpy()
                .sub(position)
                .nor()
                .scl(maxSpeed);

        return computeSteering(desired);
    }


    public Vector2 arrive(Vector2 target) {

        float slowRadius = 120f;
    
        Vector2 desired = target.cpy().sub(position);
        float distance = desired.len();
    
        if (distance < 0.001f) {
            return new Vector2(); 
        }
    
        float speed = this.maxSpeed;
    
        if (distance < slowRadius) {
            speed = this.maxSpeed * (distance / slowRadius);
        }
    
        desired.nor().scl(speed);
    
        return computeSteering(desired);
    } 

    private Vector2 computeSteering(Vector2 desired) {

        Vector2 steering = desired.sub(velocity);
    
        if (steering.len() > maxForce) {
            steering.nor().scl(maxForce);
        }
    
        return steering;
    }
    

    
}
