package com.mireia.steering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;


public class Enemy {
    public Vector2 position;
    public Vector2 velocity;

    public float maxSpeed = 150f;
    public float maxForce = 200f;
    private final float baseMaxSpeed = 150f;
    private final float baseMaxForce = 50f;
    private final float baseSeparationWeight = 1.5f;
    public float rotation;
    public boolean exploded = false;
    public boolean isLeader = false;
    public float separationWeight = 1.5f;

     // Formation
    public Vector2 formationOffset; // null if leader

    public Enemy(float posX, float posY, boolean isLeader) {
          // initial set up of each enemy
        position = new Vector2(posX, posY);
        velocity = new Vector2();
        formationOffset = null; 
        isLeader = this.isLeader;
    }

    public void update(float dt, Vector2 target, Vector2 separationForce, Vector2 playerPosition, float playerRadius) {

        if (exploded) return;
        // distance where we switch to arrive
        float switchDistance = 100f;  
        
        //explotion radius 
        float enemyRadius = 40f;
        float combinedRadius = enemyRadius + playerRadius;

        float distance = position.dst2(target);
        float halfSize = 20f;
        // try 1.5 – 3.0

        Vector2 steering;
        
        //target for enemyLeader is the spaceship
        //target for formation enemies is the enemyLeader

        if (distance > switchDistance) {
            // FAR → SEEK
            steering = seek(target);       
        } else {
            // CLOSE → ARRIVE
            steering = arrive(target);     
        }
     
        if (separationForce != null) {
            steering.add(separationForce.scl(separationWeight));
        }

        velocity.add(steering.cpy().scl(dt));

        if (velocity.len() > maxSpeed)
            velocity.nor().scl(maxSpeed);

        
        position.add(velocity.cpy().scl(dt));

        if (velocity.len() > 0) {
            rotation = velocity.angleDeg() - 90f;
        }

        // Explosion logic
        if ((this.isLeader && distance  < combinedRadius) || (!this.isLeader  && position.dst2(playerPosition) < combinedRadius)) {
            System.out.println("this.isLeader: " +this.isLeader);
            System.out.println("distance: " +distance);
            System.out.println("combinedRadius: " +combinedRadius);
            System.out.println("position.dst2(playerPosition): " +position.dst2(playerPosition));
            exploded = true;
        }

        float minY = halfSize;
        float maxX = Gdx.graphics.getWidth() - minY;
        float maxY = Gdx.graphics.getHeight() - Main.UI_HEIGHT - minY;

        position.x = Math.max(minY, Math.min(position.x, maxX));
        position.y = Math.max(minY, Math.min(position.y, maxY));
    }
    //manage seek behaviour
  
    public Vector2 seek(Vector2 target) {
        Vector2 desired = target.cpy()
                .sub(position)
                .nor()
                .scl(maxSpeed);

        return computeSteering(desired);
    }
//manage arrive behavoiur. when enemy is within slow radious

    public Vector2 arrive(Vector2 target) {

        float slowRadius = 120f;
        float speed = this.maxSpeed;
    
        Vector2 desired = target.cpy().sub(position);
        float distance = desired.len();
    
        if (distance < 0.001f) {
            return new Vector2(); 
        }
           
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
//increase speed and force based on level
    public void setDifficulty(int level) {
        this.maxSpeed = baseMaxSpeed + level * 10f;
        this.maxForce = baseMaxForce + level * 5f;
        this.separationWeight = baseSeparationWeight + level * 0.2f;
    }
    

    
}
