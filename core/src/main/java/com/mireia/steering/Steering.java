package com.mireia.steering;

import com.badlogic.gdx.math.Vector2;

public class Steering {

    public static Vector2 seek(Vector2 position, Vector2 velocity, 
                                    Vector2 target, float maxSpeed) {
    
        Vector2 desired = target.sub(position)
                                    .nor()
                                    .scl(maxSpeed);

        return desired.sub(velocity);
    }

    public static Vector2 flee(Vector2 position, Vector2 velocity, 
                                    Vector2 target, float maxSpeed) {

        Vector2 desired = target.sub(position)
                                    .nor()
                                    .scl(maxSpeed);
                               
        return desired.sub(velocity);
    }
    
}
