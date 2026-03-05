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
    // Height reserved at the top of the screen for UI elements (health bar, level text)
    public static final float UI_HEIGHT = 60f;
    
    // Renderers used by LibGDX to draw textures, fonts and shapes
    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;

    // Game textures (player spaceship and enemy asteroid)
    private Texture shipTexture, asteroidTexture;
    // Original size of the player texture
    private int shipWidth;
    private int shipHeight;
    // Scaling factor applied to the player sprite
    private float scale = 0.25f;
   
    // Player object
    private Player player;  
    // Array storing all bullets currently active in the game
    private Array<Bullet> bullets = new Array<>();

    // One enemy acts as leader of the formation
    private Enemy enemyLeader;
    // Followers that move relative to the leader
    private Array<Enemy> followers;

    // Game level
    private int level = 1;
    private float levelTimer = 0f;
    // Seconds to level up
    private float levelInterval = 15f; 
    
    // Boolean that determines if the game has ended
    private boolean gameOver = false;
    
        @Override
    // Initialize game resources
    public void create() {        

        // Create renderers
        batch = new SpriteBatch();
        font = new BitmapFont();
        shapeRenderer = new ShapeRenderer();

        // Load textures from assets folder
        shipTexture = new Texture("player.png");
        asteroidTexture = new Texture("enemy.png");

        // Save original texture size
        shipWidth = shipTexture.getWidth();
        shipHeight = shipTexture.getHeight();
            
        // Start the game
        restartGame();
    }

    @Override
    public void render() {

        // dt = time passed since last frame (used for smooth movement)
        float dt = Gdx.graphics.getDeltaTime();

        // Clear screen (black background)
        ScreenUtils.clear(0, 0, 0, 1);

        // Update game logic
        update(dt);

        // Draw all game elements
        draw();
    }
    // Dispose to avoid memory leaks 
    @Override
    public void dispose() {
        batch.dispose();
        shipTexture.dispose();
        shapeRenderer.dispose();
        asteroidTexture.dispose();
    }

    public void update(float dt) {
        // Stop updating if the game ended
        if (handleGameOver()) return;
    
        // Increase game level based on time survived
        updateLevel(dt);
    
        // Update player movement
        updatePlayer(dt);
    
        // Check if player fired a bullet
        handleShooting();
    
        // Update bullet positions and collisions
        updateBullets(dt);
    
        // Update enemy leader behaviour
        updateEnemyLeader(dt);
    
        // Update all follower enemies
        updateFollowers(dt);
    
        // Check if the leader exploded and replace it
        handleLeaderExplosion();
    
        // Check if player health reached zero
        checkGameOver();
    }
    // Ending state: player has no more life
    private boolean handleGameOver() {
        if (!gameOver) return false;
    
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) 
            restartGame();
    
        return true;
    }

    // Game levels keeps going up depending on time
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
    // Player shoots when SPACE key is pressed
    private void handleShooting() {

        if (!Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
            return;

        // Compute the forward direction of the spaceship
        Vector2 forward = new Vector2(0,1).setAngleDeg(player.rotation + 90f);

        // Distance from center of ship to the nose of the spaceship
        float noseDistance = player.height / 2f;

        // Spawn bullet at the front of the spaceship
        Vector2 spawnPos = player.position.cpy()
                .add(forward.scl(noseDistance));

        // Create bullet
        bullets.add(new Bullet(spawnPos, player.rotation + 90f));
    }

    private void updateBullets(float dt) {

        for (int i = 0; i < bullets.size; i++) {
            Bullet bullet = bullets.get(i);
    
            bullet.update(dt);
    
            if (!bullet.active) {
                bullets.removeIndex(i);
                i = i == 0 ? 0 : i--;
                continue;
            }
    
            checkBulletEnemyCollision(bullet);
        }
    }

    // Check if a bullet has hit any enemy
    private void checkBulletEnemyCollision(Bullet bullet) {

        for (Enemy enemy : followers) {

            // Combined radius for circular collision detection
            float combined = bullet.radius + (2 * enemy.radius);

            // Compare squared distances for performance
            if (bullet.position.dst2(enemy.position) < combined * combined) {

                enemy.exploded = true;      // mark enemy destroyed
                enemy.explodedBullet = true;// destroyed by bullet
                bullet.active = false;      // remove bullet

                break;
            }
        }
    }

    private void updateEnemyLeader(float dt) {

        enemyLeader.update(dt, player.position, null, player.position, player.radius);
    }
    //Update enemy followers behaviour and formation
    private void updateFollowers(float dt) {

        for (int i = 0; i < followers.size; i++) {
    
            Enemy follower = followers.get(i);
            //Predict leader future position for smoother formation following
            Vector2 futurePos = enemyLeader.position.cpy()
                    .add(enemyLeader.velocity.cpy().scl(0.3f));
            
            Vector2 target = futurePos.add(follower.formationOffset);
            Vector2 separation = computeSeparation(follower, followers);
    
            follower.update(dt, target, separation, player.position, player.radius);
    
            // Handle follower destruction
            if (follower.exploded) {
                // Damage player only if enemy collided with ship
                if (!follower.explodedBullet)
                    player.takeDamage(20);
                followers.removeIndex(i);
    
                spawnFollower(level);
                i = i == 0 ? 0 : i--;
            }
        }
    }
    // When enemyLeader explotes, new leader needs to be assigned
    private void handleLeaderExplosion() {
        if (!enemyLeader.exploded) return;
        if (!enemyLeader.explodedBullet)
            player.takeDamage(20);
    
        assignNewLeader();
        spawnFollower(level);
    }

    private void checkGameOver() {
        if (player.health <= 0)
            gameOver = true;
    }

    // Draw the player, enemies and health bar
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

        String gameOverText = "GAME OVER";
        String restartText = "Press R to Restart";
        
        layout.setText(font, gameOverText);
        
        float x = (Gdx.graphics.getWidth() - layout.width) / 2;
        float y = Gdx.graphics.getHeight() / 2;
        
        font.draw(batch, layout, x, y);
        
        // Restart text
        layout.setText(font, restartText);
        
        float x2 = (Gdx.graphics.getWidth() - layout.width) / 2;
        float y2 = y - 40;
        
        font.draw(batch, layout, x2, y2);

        batch.end();

        drawBullet();

        drawHealthBar();
    }

    private void drawPlayer() {   
        batch.draw(
            shipTexture,
            player.position.x - (player.width) / 2f,
            player.position.y - (player.height) / 2f,
            (player.width) / 2f,   // origin X
            (player.height) / 2f,  // origin Y
            player.width,
            player.height,
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

        for (Bullet bullet : bullets) {

            Vector2 direction = bullet.velocity.cpy().nor().scl(bullet.length);

            float x1 = bullet.position.x;
            float y1 = bullet.position.y;

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
    // Separation steering behaviour
    // Ensures enemies keep a safe distance from each other
    private Vector2 computeSeparation(Enemy current, Array<Enemy> followers) {

        float desiredSeparation = 100f;
        int count = 0;

        // Final steering force
        Vector2 force = new Vector2();

        for (Enemy follower : followers) {

            // Ignore self
            if (follower == current) continue;

            float distance = current.position.dst(follower.position);

            // If enemy is too close, push away
            if (distance > 0 && distance < desiredSeparation) {

                Vector2 difference = current.position.cpy()
                        .sub(follower.position)
                        .nor()
                        .scl(1f / distance);

                force.add(difference);
                count++;
            }
        }

        // Average force from neighbors
        if (count > 0) {
            force.scl(1f / count);
        }

        // Convert desired direction to steering force
        if (force.len() > 0) {

            force.nor().scl(current.maxSpeed);

            // Steering = desired velocity − current velocity
            force.sub(current.velocity);

            // Limit steering force
            if (force.len() > current.maxForce) {
                force.nor().scl(current.maxForce);
            }
        }
        return force;
    }

    // Initialize player, enemyleader and followers
    private void restartGame() {
        gameOver = false;
        level = 1;
        levelTimer = 0f;
    
        player = new Player(400, 300, shipWidth * scale, shipHeight * scale, (shipWidth * scale)/2);
    
        enemyLeader = new Enemy(100, 100, true);
        followers = new Array<>();

        bullets = new Array<>();

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
    // Assign each follower a new relative position 
    private void recalculateFormationOffsets() {
        float spacing = 60f;

        for (int i = 0; i < followers.size; i++) {

            float angle = i * 45f;

            float offsetX = MathUtils.cosDeg(angle) * spacing;
            float offsetY = MathUtils.sinDeg(angle) * spacing;

            followers.get(i).formationOffset.set(offsetX, offsetY);
        }
    }

    // Create array of followers
    private void spawnFollower(int level) {

        int maxFollowers = level > 5 ? 5 : level;
    
        if (followers.size >= maxFollowers) return;
    
        float spacing = 90f;
        // Evenly distribute followers in a circular formation
        float angleStep = 360f / maxFollowers;

        float halfSize = 20f; // enemy radius (size/2)
        float uiHeight = UI_HEIGHT; // your top bar height
        // Minimum safe distance from the player when spawning enemies
        float minPlayerDistance = 300f;
    
        for (int i = followers.size; i < maxFollowers; i++) {
    
            float angle = i * angleStep;
    
            float offsetX = MathUtils.cosDeg(angle) * spacing;
            float offsetY = MathUtils.sinDeg(angle) * spacing;
    
            Vector2 formationOffset = new Vector2(offsetX, offsetY);
    
            Vector2 spawnPos = enemyLeader.position.cpy().add(formationOffset);

            // Clamp spawn inside screen bounds
            spawnPos.x = Math.max(halfSize,
                Math.min(spawnPos.x, Gdx.graphics.getWidth() - halfSize));

            spawnPos.y = Math.max(halfSize,
                Math.min(spawnPos.y, Gdx.graphics.getHeight() - uiHeight - halfSize));

            // Prevent enemies from spawning too close to player
            if (spawnPos.dst(player.position) < minPlayerDistance) {

                // Push spawn position away from player
                Vector2 away = spawnPos.cpy()
                        .sub(player.position)
                        .nor()
                        .scl(minPlayerDistance);
            
                spawnPos.set(player.position.cpy().add(away));
            }
    
            Enemy newFollower = new Enemy(spawnPos.x, spawnPos.y, false);
            newFollower.formationOffset = formationOffset;
    
            followers.add(newFollower);
        }
    }  
}

