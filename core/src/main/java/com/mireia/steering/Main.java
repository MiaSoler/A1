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
import com.badlogic.gdx.utils.TimeUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    public static final float UI_HEIGHT = 60f;
    //render textures:images, text and shape
    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;

    private Texture shipTexture, asteroidTexture;
    private int shipWidth;
    private int shipHeight;
    private float scale = 0.25f;
    private float playerRadius;
 
    private Player player;  
    private Array<Bullet> bullets = new Array<>();

    private Enemy enemyLeader;
    private Array<Enemy> followers;

    //level
    private int level = 1;
    private float levelTimer = 0f;
    private float levelInterval = 15f; // seconds to level up

    private boolean gameOver = false;
    
    @Override
    //initialize the game with 1 player and 1 enemy
    //calling restartGame to initialize the array of enemy followers
    public void create() {        
        batch = new SpriteBatch();
        font = new BitmapFont();
        shapeRenderer = new ShapeRenderer();

        shipTexture = new Texture("player.png");
        asteroidTexture = new Texture("enemy.png");

        shipWidth = shipTexture.getWidth();
        shipHeight = shipTexture.getHeight();
        playerRadius = (shipWidth * scale);

        restartGame(); // initialize game objects
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        ScreenUtils.clear(0, 0, 0, 1);

        update(dt);
        draw();
        
    }
    //dispose to avoid memory leaks 
    @Override
    public void dispose() {
        batch.dispose();
        shipTexture.dispose();
        shapeRenderer.dispose();
        asteroidTexture.dispose();
    }

    public void update(float dt) {

        if (handleGameOver()) return;
    
        updateLevel(dt);
    
        updatePlayer(dt);
    
        handleShooting();
    
        updateBullets(dt);
    
        updateEnemyLeader(dt);
    
        updateFollowers(dt);
    
        handleLeaderExplosion();
    
        checkGameOver();
    }
    //ending state: player has no more life
    private boolean handleGameOver() {
        if (!gameOver) return false;
    
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) 
            restartGame();
    
        return true;
    }

    private void updateLevel(float dt) {
        levelTimer += dt;
    
        if (levelTimer >= levelInterval) {
            level++;
            levelTimer = 0f;
    
            enemyLeader.setDifficulty(level);
    
            for (Enemy follower : followers)
                follower.setDifficulty(level);
        }
    }

    private void updatePlayer(float dt) {
        player.update(dt);
    }

    private void handleShooting() {
        if (!Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
            return;
    
        Vector2 forward = new Vector2(0,1).setAngleDeg(player.rotation + 90f);
    
        float noseDistance = (shipHeight * scale) / 2f;
    
        Vector2 spawnPos = player.position.cpy()
                .add(forward.scl(noseDistance));
    
        bullets.add(new Bullet(spawnPos, player.rotation + 90f));
    }

    private void updateBullets(float dt) {

        for (int i = 0; i < bullets.size; i++) {
            Bullet bullet = bullets.get(i);
    
            bullet.update(dt);
    
            if (!bullet.active) {
                bullets.removeIndex(i);
                i--;
                continue;
            }
    
            checkBulletEnemyCollision(bullet);
        }
    }

    private void checkBulletEnemyCollision(Bullet bullet) {

        for (Enemy enemy : followers) {
            float combined = bullet.radius + enemy.radius;
    
            if (bullet.position.dst2(enemy.position) < combined * combined) {
                enemy.exploded = true;
                bullet.active = false;
                break;
            }
        }
    }

    private void updateEnemyLeader(float dt) {

        enemyLeader.update(dt, player.position, null, player.position, playerRadius);
    }

    private void updateFollowers(float dt) {

        for (int i = 0; i < followers.size; i++) {
    
            Enemy follower = followers.get(i);
    
            Vector2 futurePos = enemyLeader.position.cpy()
                    .add(enemyLeader.velocity.cpy().scl(0.3f));
            Vector2 target = futurePos.add(follower.formationOffset);
            Vector2 separation = computeSeparation(follower, followers);
    
            follower.update(dt, target, separation, player.position, playerRadius);
    
            if (follower.exploded) {
                player.takeDamage(20);
                followers.removeIndex(i);
    
                spawnFollower(level);
                i = i == 0 ? 0 : i--;
            }
        }
    }

    private void handleLeaderExplosion() {
        if (!enemyLeader.exploded) return;
    
        player.takeDamage(20);
    
        assignNewLeader();
    
        spawnFollower(level);
    }

    private void checkGameOver() {
        if (player.health <= 0)
            gameOver = true;
    }

    //draw the player, enemies and health bar
    public void draw() {
        batch.begin();
        
        drawPlayer();
        if (enemyLeader != null&& !enemyLeader.exploded)
            drawEnemy(enemyLeader);

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

        drawBullet();

        // 🔴 Draw red circle around leader
        if (enemyLeader != null && !enemyLeader.exploded) {

            float size = 40f;
            float pulse = 5f * MathUtils.sinDeg((TimeUtils.millis() % 360));
            float radius = size / 2f + 5f + pulse;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.RED);

            shapeRenderer.circle(
                    enemyLeader.position.x,
                    enemyLeader.position.y,
                    radius
            );

            shapeRenderer.end();
        }

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

    private void drawBullet() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        for (Bullet b : bullets) {

            Vector2 direction = b.velocity.cpy().nor().scl(b.length);

            float x1 = b.position.x;
            float y1 = b.position.y;

            float x2 = x1 - direction.x;
            float y2 = y1 - direction.y;

            // Outer glow
            Gdx.gl.glLineWidth(5f);
            shapeRenderer.setColor(1f, 0.6f, 0f, 0.4f); // soft orange
            shapeRenderer.line(x1, y1, x2, y2);

            // Middle layer
            Gdx.gl.glLineWidth(3f);
            shapeRenderer.setColor(Color.ORANGE);
            shapeRenderer.line(x1, y1, x2, y2);

            // Bright core
            Gdx.gl.glLineWidth(1f);
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.line(x1, y1, x2, y2);
        }

        shapeRenderer.end();
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

        // Draw level on left side
        font.draw(batch, "Level: " + level, 20, Gdx.graphics.getHeight() - 20);
        batch.end();
    }

    //this function make sure enemies keep a safe distance between them
    private Vector2 computeSeparation(Enemy current, Array<Enemy> followers) {
        float desiredSeparation = 100f;
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

    //initialize player, enemyleader and followers
    private void restartGame() {
        gameOver = false;
        level = 1;
        levelTimer = 0f;
    
        player = new Player(400, 300, shipWidth * scale, shipHeight * scale);
    
        enemyLeader = new Enemy(100, 100, true);
        followers = new Array<>();

        spawnFollower(2);
    }

    private void assignNewLeader() {    
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
    //assign each follower a new relative position 
    private void recalculateFormationOffsets() {
        float spacing = 60f;

        for (int i = 0; i < followers.size; i++) {

            float angle = i * 45f;

            float offsetX = MathUtils.cosDeg(angle) * spacing;
            float offsetY = MathUtils.sinDeg(angle) * spacing;

            followers.get(i).formationOffset.set(offsetX, offsetY);
        }
    }

    //create array of followers
    private void spawnFollower(int level) {

        int maxFollowers = level > 5 ? 5 : level;
    
        if (followers.size >= maxFollowers) return;
    
        float spacing = 90f;
        float angleStep = 360f / maxFollowers;

        float halfSize = 20f; // enemy radius (size/2)
        float uiHeight = UI_HEIGHT; // your top bar height
    
        for (int i = followers.size; i < maxFollowers; i++) {
    
            float angle = i * angleStep;
    
            float offsetX = MathUtils.cosDeg(angle) * spacing;
            float offsetY = MathUtils.sinDeg(angle) * spacing;
    
            Vector2 formationOffset = new Vector2(offsetX, offsetY);
    
            Vector2 spawnPos = enemyLeader.position.cpy().add(formationOffset);

            spawnPos.x = Math.max(halfSize,
                Math.min(spawnPos.x, Gdx.graphics.getWidth() - halfSize));

            spawnPos.y = Math.max(halfSize,
                Math.min(spawnPos.y, Gdx.graphics.getHeight() - uiHeight - halfSize));
    
            Enemy newFollower = new Enemy(spawnPos.x, spawnPos.y, false);
            newFollower.formationOffset = formationOffset;
    
            followers.add(newFollower);
        }
    }  
}

