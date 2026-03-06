# Assignment1

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a template including simple application launchers and an `ApplicationAdapter` extension that draws libGDX logo.

# 1. Introduction
The game developed for this assignment is a simple 2D space survival game inspired by classic arcade games such as Space Invaders. The player controls a spaceship and must survive against waves of asteroids that move using steering behaviours. The asteroids attempt to approach the player while maintaining formation and avoiding collisions with each other. The spaceship can shot bullets to the asteroids.
The project was implemented using the LibGDX framework, which provides tools for rendering graphics, handling input, and managing the game loop.

# 2. Game Design and Logic
The game consists of three main types of entities:
Player -> spaceship
Enemy -> asteroids
Bullets -> fired by the player
The player controls a spaceship that can move in four directions and shoot bullets. Enemy asteroids move autonomously using steering behaviors to approach the player.
The enemies are organized using a leader–follower formation system. One asteroid acts as the leader and directly targets the player, while the other asteroids follow the leader while maintaining a formation and avoiding collisions with other asteroids.
The player has a health bar displayed at the top of the screen. When an asteroid collides with the spaceship, the player loses health. If the player's health reaches zero, the game ends and a game over message is displayed.
The difficulty increases over time through a level system, which increases enemy speed and behavior intensity.

# 3. Game Controls
The player interacts with the game using keyboard input.

The player must avoid asteroids while destroying them using bullets.

# 4. Game Mechanics
Player Movement
The spaceship can move freely within the boundaries of the screen using keyboard input. The player cannot move into the UI area where the health bar is displayed.
The spaceship rotates automatically to face the direction of movement.
Shooting System
The player can shoot bullets by pressing the space bar. Bullets are spawned at the front of the spaceship and travel in the direction the ship is facing.
Bullets destroy asteroids on collision.
Enemy Behaviour
Enemies attempt to move toward the player using steering behaviours. If an asteroid collides with the player, the player loses health and the asteroid is removed.
When the leader asteroid is destroyed, a new leader is selected from the followers array.
Health System
The player begins with 100 health points.Each collision with an asteroid reduces the player's health by 20.
If health reaches zero, the game enters a game over state.
Level System
The game includes a time-based level system.
Every 15 seconds:
The level increases
Enemy speed increases
Enemy steering force increases
Separation behavior becomes stronger
This gradually increases the difficulty as the player survives longer.

# 5. Steering Behaviors Implemented
The game demonstrates multiple steering behaviors as required by the assignment.
5.1 Seek Behavior
Seek behavior allows an agent to move directly toward a target at maximum speed.
In this game, the enemy leader uses seek behavior to pursue the player.
The desired velocity is calculated as:

desiredVelocity = normalize(target - position) * maxSpeed
steering = desiredVelocity - currentVelocity

This causes the enemy leader to actively chase the player's spaceship.

5.2 Arrive Behaviour
Arrive behavior allows the enemy to slow down when approaching a target.
When enemies get close to their target position, they switch from seek to arrive. This prevents overshooting and creates smoother movement.
The speed is reduced proportionally when the enemy enters a slow radius.

5.3 Separation Behaviour
Separation behavior prevents agents from colliding with each other.
Each enemy checks nearby enemies and generates a steering force that pushes it away from neighbors that are too close.
This ensures that asteroids maintain distance from each other and prevents overlapping.

# 6. Complex Steering Behavior
The complex steering behavior implemented in this game is a leader–follower formation system.
In this system:
One asteroid acts as the leader
Other asteroids act as followers
Followers maintain an offset relative to the leader
Followers calculate their target position using:
target = leaderPosition + formationOffset

They then apply:
Seek or arrive behavior to move toward the formation position
Separation behavior to avoid overlapping with other followers
This combination of behaviors produces coordinated group movement.

# 7. Projectile System
The player can shoot bullets that move forward in the direction of the spaceship's rotation.
Bullets use simple projectile physics:
position = position + velocity × deltaTime

Bullets are removed when they leave the screen or collide with an enemy.

# 8. Code Design and Structure
The program follows a modular object-oriented design.
The main classes are:

Separating the game logic into classes improves readability, maintainability, and modularity.
The update logic in the game loop is also modularized into multiple functions such as:
updatePlayer()
updateBullets()
updateFollowers()
handleShooting()
updateLevel()
This structure ensures the code remains organized and easy to understand.

# 9. How to Compile and Run the Game
Requirements
Java JDK
Gradle
LibGDX project structure
Running the game
From the project root directory, run:
./gradlew lwjgl3:run

This will launch the desktop version of the game.

# 10. Conclusion
This project successfully demonstrates the use of steering behaviors in a simple 2D game environment. The combination of seek, arrive, and separation behaviors allows enemy agents to move in a coordinated and believable way while interacting with the player.
The use of a leader–follower formation system demonstrates a complex coordinated movement behavior that combines multiple steering techniques.
The modular design of the program ensures the code is organized, readable, and maintainable.
Overall, the project fulfills the assignment requirements and demonstrates practical implementation of steering behaviors in game AI.
