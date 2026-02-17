package com.mireia.steering;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture shipTexture, asteroidTexture;
    private int shipWidth;
    private int shipHeight;
    private ShapeRenderer shapeRenderer;
    private Player player;  
    private Enemy enemyLeader;
    private Array<Enemy> followers;


    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        player = new Player(400, 300);

        batch = new SpriteBatch();
        shipTexture = new Texture("player.png");
        asteroidTexture = new Texture("enemy.png");

        shipWidth = shipTexture.getWidth();
        shipHeight = shipTexture.getHeight();
       
        enemyLeader = new Enemy(100, 100);
        followers = new Array<>();

        Enemy follower1 = new Enemy(180, 260);
        follower1.formationOffset = new Vector2(-40, -40);

        Enemy follower2 = new Enemy(220, 260);
        follower2.formationOffset = new Vector2(40, -40);

        followers.add(follower1);
        followers.add(follower2);
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        ScreenUtils.clear(0, 0, 0, 1);

        update(dt);
        draw();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shipTexture.dispose();
    }

    public void update(float dt) {
        player.update(dt);

        enemyLeader.update(dt, player.position, null);

        for (int i = 0; i < followers.size; i++) {
            Enemy follower = followers.get(i);
        
            Vector2 target = enemyLeader.position.cpy()
                                .add(follower.formationOffset);
        
            Vector2 separation = computeSeparation(follower, followers);
        
            follower.update(dt, target, separation);
        }

        if (enemyLeader.exploded) {
            player.takeDamage(20);
        }
        
    }

    public void draw() {
        batch.begin();

        drawPlayer();
        drawEnemy(enemyLeader);

        // Followers
        for (Enemy follower : followers) {
            drawEnemy(follower);
        }

        batch.end();
    }

    public void drawPlayer() {
        float scale = 0.25f;
        
        batch.draw(
            shipTexture,
            player.position.x - (shipWidth * scale) / 2f,
            player.position.y - (shipHeight * scale) / 2f,
            (shipWidth * scale) / 2f,   // origin X
            (shipHeight * scale) / 2f,  // origin Y
            shipWidth * scale,
            shipHeight * scale,
            1f, 1f,          // scale
            player.rotation,
            0, 0,
            shipWidth,
            shipHeight,
            false, false
        );
    }
    public void drawEnemy(Enemy e) {     
        float size = 40f; // fixed in-game size (recommended)

        batch.draw(
                asteroidTexture,
                e.position.x - size / 2f,
                e.position.y - size / 2f,
                size / 2f, size / 2f,  // rotation origin
                size, size,
                1f, 1f,
                e.rotation,
                0, 0,
                asteroidTexture.getWidth(),
                asteroidTexture.getHeight(),
                false, false
        );
    }

    private Vector2 computeSeparation(Enemy current, Array<Enemy> followers) {
        float desiredSeparation = 25f;
        Vector2 force = new Vector2();
    
        for (Enemy follower : followers) {
            if (follower == current) continue;
    
            float distance = current.position.dst(follower.position);
            if (distance > 0 && distance < desiredSeparation) {
                Vector2 difference = current.position.cpy()
                        .sub(follower.position)
                        .nor()
                        .scl(1f / distance);
                force.add(difference);
            }
        }
        return force;
    }
    
}
