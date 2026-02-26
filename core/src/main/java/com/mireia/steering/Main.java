package com.mireia.steering;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    public static final float UI_HEIGHT = 60f;
    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;

    private Texture shipTexture, asteroidTexture;
    private int shipWidth;
    private int shipHeight;
    private float scale = 0.25f;
 
    private Player player;  
    private Enemy enemyLeader;
    private Array<Enemy> followers;

    private boolean gameOver = false;
    
    @Override
    public void create() {        
        batch = new SpriteBatch();
        font = new BitmapFont();
        shapeRenderer = new ShapeRenderer();

        shipTexture = new Texture("player.png");
        asteroidTexture = new Texture("enemy.png");

        shipWidth = shipTexture.getWidth();
        shipHeight = shipTexture.getHeight();

        restartGame(); // initialize game objects
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        ScreenUtils.clear(0, 0, 0, 1);

        if (enemyLeader != null){
            update(dt);
            draw();
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        shipTexture.dispose();
        shapeRenderer.dispose();
        asteroidTexture.dispose();
    }

    public void update(float dt) {

        if (gameOver) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                restartGame();
            }
                return;   // stop all logic 
        }

        player.update(dt);

        enemyLeader.update(dt, player.position, null, player.position);

        for (int i = 0; i < followers.size; i++) {
            Enemy follower = followers.get(i);

            //calculate the future position of the enemy leader
            Vector2 futurePos = enemyLeader.position.cpy()
                .add(enemyLeader.velocity.cpy().scl(0.3f));
        
            Vector2 target = futurePos.add(follower.formationOffset);
        
            Vector2 separation = computeSeparation(follower, followers);
        
            follower.update(dt, target, separation, player.position);

            if (follower.exploded) {
                player.takeDamage(20);
                followers.removeIndex(i);
               // spawnFollower(1);
                System.out.println("follower exploded: " + i);
                i--;
            }
        }

        if (enemyLeader.exploded) {
            player.takeDamage(20);
            assignNewLeader();
           // spawnFollower(MathUtils.random(1, 3));
        }


        if (player.health <= 0) {
            gameOver = true;
        }
    }

    public void draw() {
        batch.begin();
        
        drawPlayer();
        if (enemyLeader != null&& !enemyLeader.exploded)
            drawEnemy(enemyLeader);

        // Followers
        for (Enemy follower : followers) {
            if (!follower.exploded) {
                drawEnemy(follower);
            }
        }

        if (gameOver) {
            font.draw(batch, "GAME OVER", 350, 320);
            font.draw(batch, "Press R to Restart", 330, 280);
        }

        batch.end();

        drawHealthBar();
    }

    private void drawPlayer() {
                
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
    private void drawEnemy(Enemy e) {     
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

    private void drawHealthBar() {

        float barWidth = 200;
        float barHeight = 20;
        float margin = 20;
    
        float x = Gdx.graphics.getWidth() - barWidth - margin;
        float y = Gdx.graphics.getHeight() - barHeight - margin;
    
        float healthPercent = (float) player.health / 100;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Background (gray)
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(x, y, barWidth, barHeight);

        // Health (red)
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(x, y, barWidth * healthPercent, barHeight);

        shapeRenderer.end();

        // Draw text
        batch.begin();
        font.draw(batch, "Player", x, y + barHeight + 15);
        batch.end();
    }

    private Vector2 computeSeparation(Enemy current, Array<Enemy> followers) {
        float desiredSeparation = 40f;
        int count = 0;
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
                count++;
            }
        }
        // Average the force
        if (count > 0) {
            force.scl(1f / count);
        }

        // Convert to proper steering force
        if (force.len() > 0) {
            force.nor().scl(current.maxSpeed);
            force.sub(current.velocity);

            // Limit force
            if (force.len() > current.maxForce) {
                force.nor().scl(current.maxForce);
            }
        }

        return force;
    }

    private void restartGame() {
        gameOver = false;
    
        player = new Player(400, 300, shipWidth * scale, shipHeight * scale);
    
        enemyLeader = new Enemy(100, 100, true);
        followers = new Array<>();

        spawnFollower(2);
    }

    private void assignNewLeader() {

        if (followers.size == 0) {
            enemyLeader = null;
            return;
        }
    
        Enemy closest = followers.first();
        float minDist = closest.position.dst(player.position);
    
        for (int i = 1; i < followers.size; i++) {
            Enemy e = followers.get(i);
            float dist = e.position.dst(player.position);
    
            if (dist < minDist) {
                minDist = dist;
                closest = e;
            }
        }
    
        followers.removeValue(closest, true);
        enemyLeader = closest;
        closest.isLeader = true;

        recalculateFormationOffsets();
    }

    private void recalculateFormationOffsets() {

        float spacing = 60f;

        for (int i = 0; i < followers.size; i++) {

            float angle = i * 45f;

            float offsetX = MathUtils.cosDeg(angle) * spacing;
            float offsetY = MathUtils.sinDeg(angle) * spacing;

            followers.get(i).formationOffset.set(offsetX, offsetY);
        }
    }

    private void spawnFollower(int amount) {

        int maxFollowers = 5;

        if (followers.size < maxFollowers) {
            for (int i = 0; i<amount; i++) { 
                // Spawn near the leader
                Vector2 spawnPos = enemyLeader.position.cpy()
                    .add(MathUtils.random(-100, 100), 
                        MathUtils.random(-100, 100));
    
                Enemy newFollower = new Enemy(spawnPos.x, spawnPos.y, false);
                newFollower.formationOffset = new Vector2(-40, -40);
    
                followers.add(newFollower);
    
                recalculateFormationOffsets();
            }
        }
    }
    
}

