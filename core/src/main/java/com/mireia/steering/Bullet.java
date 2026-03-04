package com.mireia.steering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

public class Bullet {

    public Vector2 position;
    public Vector2 velocity;
    public float speed = 400f;
    public float radius = 5f;
    public boolean active = true;
    public float length = 12f;

    public Bullet(Vector2 startPosition, float rotation) {
        position = startPosition.cpy();

        velocity = new Vector2(0, 1)
                .setAngleDeg(rotation)
                .scl(speed);
    }

    public void update(float dt) {
        position.add(velocity.cpy().scl(dt));

        // Remove if off screen
        if (position.x < 0 || position.x > Gdx.graphics.getWidth()
                || position.y < 0 || position.y > Gdx.graphics.getHeight()) {
            active = false;
        }
    }
}
