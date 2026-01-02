package dev.main;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GameLogic {
    
    private GameState state;
    private float cameraLerpSpeed = 5f;
    
    public GameLogic(GameState state) {
        this.state = state;
    } 
    
    public void update(float delta) {
        state.incrementGameTime(delta);
        
        Entity player = state.getPlayer();
        Position playerPos = player.getComponent(Position.class);
        
        for (Entity entity : state.getEntities()) {
            EntityType entityType = entity.getType();
            
            // Update based on entity type
            if (entityType == EntityType.PLAYER) {
                updatePlayer(entity, delta);
            } else if (entityType == EntityType.MONSTER) {
                updateMonster(entity, playerPos, delta);
            }
            
            // Update sprite animations (all entities)
            Sprite sprite = entity.getComponent(Sprite.class);
            if (sprite != null) {
                sprite.update(delta);
            }
            
            // Update target indicator
            TargetIndicator indicator = entity.getComponent(TargetIndicator.class);
            if (indicator != null) {
                indicator.update(delta);
            }
        }
        
        // Remove dead entities
        state.removeMarkedEntities();
        
        updateCamera(delta);
    }
    
    private void updatePlayer(Entity player, float delta) {
        Movement movement = player.getComponent(Movement.class);
        Position position = player.getComponent(Position.class);
        Sprite sprite = player.getComponent(Sprite.class);
        Stats stats = player.getComponent(Stats.class);
        Path path = player.getComponent(Path.class);
        TargetIndicator indicator = player.getComponent(TargetIndicator.class);
        
        if (movement == null || position == null || sprite == null || stats == null) return;
        
        // Follow path
        if (path != null && path.isFollowing) {
            followPath(player, path, movement, position, delta);
        }
        
        if (movement.isMoving) {
            // Handle stamina
            if (movement.isRunning) {
                boolean hasStamina = stats.consumeStamina(movement.staminaCostPerSecond * delta);
                if (!hasStamina) {
                    movement.stopRunning();
                }
            }
            
            moveTowardsTarget(player, movement, position, delta);
            
            String moveAnim = movement.isRunning 
                ? getRunAnimationForDirection(movement.direction)
                : getWalkAnimationForDirection(movement.direction);
            sprite.setAnimation(moveAnim);
            
        } else {
            if (indicator != null) {
                indicator.clear();
            }
            
            sprite.setAnimation(getIdleAnimationForDirection(movement.lastDirection));
            stats.regenerateStamina(delta);
        }
        
        if (movement != null && movement.isMoving && !movement.isRunning) {
            stats.regenerateStamina(delta);
        }
    }
    private void updateMonster(Entity monster, Position playerPos, float delta) {
        AI ai = monster.getComponent(AI.class);
        Position position = monster.getComponent(Position.class);
        Movement movement = monster.getComponent(Movement.class);
        Sprite sprite = monster.getComponent(Sprite.class);
        Stats stats = monster.getComponent(Stats.class);
        Path path = monster.getComponent(Path.class);
        Dead dead = monster.getComponent(Dead.class);
        
        if (position == null || ai == null) return;
        
        // Update dead state
        if (dead != null) {
            dead.update(delta);
            if (dead.shouldRemove()) {
                state.markForRemoval(monster);
            }
            return;  // Dead monsters don't do anything
        }
        
        // Check if dead
        if (stats != null && stats.hp <= 0) {
            handleMonsterDeath(monster, sprite);
            return;
        }
        
        // Update AI
        ai.update(delta);
        
        // AI State Machine
        switch (ai.currentState) {
            case IDLE:
                handleIdleState(monster, ai, movement, sprite, playerPos, delta);
                break;
                
            case ROAMING:
                handleRoamingState(monster, ai, movement, position, path, sprite, playerPos, delta);
                break;
                
            case CHASING:
                handleChasingState(monster, ai, movement, position, path, sprite, playerPos, delta);
                break;
                
            case RETURNING:
                handleReturningState(monster, ai, movement, position, path, sprite, delta);
                break;
                
            case ATTACKING:
                handleAttackingState(monster, ai, movement, sprite, playerPos, stats, delta);
                break;
        }
        
        // Move if has target
        if (movement != null && movement.isMoving) {
            moveTowardsTarget(monster, movement, position, delta);
        }
        
        // Follow path
        if (path != null && path.isFollowing) {
            followPath(monster, path, movement, position, delta);
        }
    }
    
    private void handleIdleState(Entity monster, AI ai, Movement movement, Sprite sprite, Position playerPos, float delta) {
        // Detect player
        if (ai.behaviorType.equals("aggressive") && playerPos != null) {
            Position monsterPos = monster.getComponent(Position.class);
            if (canDetectPlayer(monsterPos, playerPos, ai.detectionRange)) {
                ai.currentState = AI.State.CHASING;
                ai.target = state.getPlayer();
                return;
            }
        }
        
        // Transition to roaming after timer
        if (ai.roamTimer >= ai.roamInterval) {
            ai.roamTimer = 0;
            ai.roamInterval = ThreadLocalRandom.current().nextFloat(3f, 6f);
            ai.currentState = AI.State.ROAMING;
        }
        
        // Play idle animation
        if (sprite != null && movement != null) {
            sprite.setAnimation(getIdleAnimationForDirection(movement.lastDirection));
        }
    }
    
    private void handleRoamingState(Entity monster, AI ai, Movement movement, Position position, Path path, Sprite sprite, Position playerPos, float delta) {
        // Detect player
        if (ai.behaviorType.equals("aggressive") && playerPos != null) {
            if (canDetectPlayer(position, playerPos, ai.detectionRange)) {
                ai.currentState = AI.State.CHASING;
                ai.target = state.getPlayer();
                if (path != null) path.clear();
                return;
            }
        }
        
        // Pick random point within roam radius
        if (movement != null && !movement.isMoving) {
            float angle = (float)(ThreadLocalRandom.current().nextDouble() * Math.PI * 2);
            float distance = ThreadLocalRandom.current().nextFloat(0.5f, 1f) * ai.roamRadius;
            
            float targetX = ai.homeX + (float)Math.cos(angle) * distance;
            float targetY = ai.homeY + (float)Math.sin(angle) * distance;
            
            // Pathfind to roam target
            int startTileX = (int)(position.x / TileMap.TILE_SIZE);
            int startTileY = (int)(position.y / TileMap.TILE_SIZE);
            int goalTileX = (int)(targetX / TileMap.TILE_SIZE);
            int goalTileY = (int)(targetY / TileMap.TILE_SIZE);
            
            List<int[]> foundPath = state.getPathfinder().findPath(startTileX, startTileY, goalTileX, goalTileY);
            
            if (foundPath != null && path != null) {
                path.setPath(foundPath);
                movement.isRunning = false;
            } else {
                ai.currentState = AI.State.IDLE;
            }
        }
        
        // Reached destination, go back to idle
        if (movement != null && !movement.isMoving && (path == null || !path.isFollowing)) {
            ai.currentState = AI.State.IDLE;
            ai.roamTimer = 0;
        }
        
        // Animation
        if (sprite != null && movement != null) {
            if (movement.isMoving) {
                sprite.setAnimation(getWalkAnimationForDirection(movement.direction));
            } else {
                sprite.setAnimation(getIdleAnimationForDirection(movement.lastDirection));
            }
        }
    }
    
    private void handleChasingState(Entity monster, AI ai, Movement movement, Position position, Path path, Sprite sprite, Position playerPos, float delta) {
        if (playerPos == null || movement == null || position == null) {
            ai.currentState = AI.State.RETURNING;
            return;
        }
        
        // Check if too far from home
        float distFromHome = distance(position.x, position.y, ai.homeX, ai.homeY);
        if (distFromHome > ai.returnThreshold) {
            ai.currentState = AI.State.RETURNING;
            ai.target = null;
            if (path != null) path.clear();
            return;
        }
        
        // Check if player still in detection range
        float distToPlayer = distance(position.x, position.y, playerPos.x, playerPos.y);
        if (distToPlayer > ai.detectionRange * TileMap.TILE_SIZE * 1.5f) {  // 1.5x leash
            ai.currentState = AI.State.RETURNING;
            ai.target = null;
            if (path != null) path.clear();
            return;
        }
        
        // Check if in attack range
        if (distToPlayer <= ai.attackRange) {
            ai.currentState = AI.State.ATTACKING;
            if (movement != null) movement.stopMoving();
            if (path != null) path.clear();
            return;
        }
        
        // Chase player - update path periodically
        if (!movement.isMoving || (path != null && !path.isFollowing)) {
            int startTileX = (int)(position.x / TileMap.TILE_SIZE);
            int startTileY = (int)(position.y / TileMap.TILE_SIZE);
            int goalTileX = (int)(playerPos.x / TileMap.TILE_SIZE);
            int goalTileY = (int)(playerPos.y / TileMap.TILE_SIZE);
            
            List<int[]> foundPath = state.getPathfinder().findPath(startTileX, startTileY, goalTileX, goalTileY);
            
            if (foundPath != null && path != null) {
                path.setPath(foundPath);
                movement.isRunning = true;  // Run when chasing
            }
        }
        
        // Animation
        if (sprite != null && movement.isMoving) {
            sprite.setAnimation(getRunAnimationForDirection(movement.direction));
        }
    }
    
    private void handleReturningState(Entity monster, AI ai, Movement movement, Position position, Path path, Sprite sprite, float delta) {
        if (movement == null || position == null) return;
        
        // Check if back home
        float distFromHome = distance(position.x, position.y, ai.homeX, ai.homeY);
        if (distFromHome < 32f) {  // Within half tile
            ai.currentState = AI.State.IDLE;
            if (movement != null) movement.stopMoving();
            if (path != null) path.clear();
            return;
        }
        
        // Path back home
        if (!movement.isMoving || (path != null && !path.isFollowing)) {
            int startTileX = (int)(position.x / TileMap.TILE_SIZE);
            int startTileY = (int)(position.y / TileMap.TILE_SIZE);
            int goalTileX = (int)(ai.homeX / TileMap.TILE_SIZE);
            int goalTileY = (int)(ai.homeY / TileMap.TILE_SIZE);
            
            List<int[]> foundPath = state.getPathfinder().findPath(startTileX, startTileY, goalTileX, goalTileY);
            
            if (foundPath != null && path != null) {
                path.setPath(foundPath);
                movement.isRunning = false;
            }
        }
        
        // Animation
        if (sprite != null && movement != null) {
            if (movement.isMoving) {
                sprite.setAnimation(getWalkAnimationForDirection(movement.direction));
            } else {
                sprite.setAnimation(getIdleAnimationForDirection(movement.lastDirection));
            }
        }
    }
    
    private void handleAttackingState(Entity monster, AI ai, Movement movement, Sprite sprite, Position playerPos, Stats stats, float delta) {
        Position monsterPos = monster.getComponent(Position.class);
        
        if (playerPos == null || monsterPos == null) {
            ai.currentState = AI.State.IDLE;
            return;
        }
        
        float distToPlayer = distance(monsterPos.x, monsterPos.y, playerPos.x, playerPos.y);
        
        // Player moved away
        if (distToPlayer > ai.attackRange * 1.5f) {
            ai.currentState = AI.State.CHASING;
            return;
        }
        
        // Attack if cooldown ready
        if (ai.canAttack()) {
            tryAttack(monster, state.getPlayer());
            ai.resetAttackCooldown();
            
            // Play attack animation (for now use idle, add attack animation later)
            if (sprite != null && movement != null) {
                sprite.setAnimation(getIdleAnimationForDirection(movement.lastDirection));
                // TODO: sprite.setAnimation(getAttackAnimationForDirection(movement.lastDirection));
            }
        }
    }
    
    private void handleMonsterDeath(Entity monster, Sprite sprite) {
        System.out.println(monster.getName() + " has died!");
        
        // Add dead component
        monster.addComponent(new Dead(5f));  // Corpse lasts 5 seconds
        
        // Stop movement
        Movement movement = monster.getComponent(Movement.class);
        if (movement != null) {
            movement.stopMoving();
        }
        
        Path path = monster.getComponent(Path.class);
        if (path != null) {
            path.clear();
        }
        
        // Change to dead sprite/animation
        if (sprite != null) {
            // TODO: sprite.setAnimation("dead");
            // For now, just stop animating
        }
    }
    /**
     * Detect player using distance check (can add dot product later for FOV)
     */
    private boolean canDetectPlayer(Position monsterPos, Position playerPos, float detectionTiles) {
        float detectionDistance = detectionTiles * TileMap.TILE_SIZE;
        float dist = distance(monsterPos.x, monsterPos.y, playerPos.x, playerPos.y);
        return dist <= detectionDistance;
    }
    
    private float distance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float)Math.sqrt(dx * dx + dy * dy);
    }
    
    public void tryAttack(Entity attacker, Entity target) {
        Stats attackerStats = attacker.getComponent(Stats.class);
        Stats targetStats = target.getComponent(Stats.class);
        
        if (attackerStats != null && targetStats != null) {
            int damage = Math.max(1, (int)(attackerStats.attack - targetStats.defense));
            targetStats.hp -= damage;
            
            System.out.println(attacker.getName() + " attacks " + target.getName() + " for " + damage + " damage!");
            
            if (targetStats.hp <= 0) {
                targetStats.hp = 0;
            }
        }
    }
    /**
     * Follow the current path waypoint by waypoint
     */
    private void followPath(Entity entity, Path path, Movement movement, Position position, float delta ) {//TODO: no delta in the argument!
        int[] waypoint = path.getCurrentWaypoint();
        
        if (waypoint == null) {
            path.clear();
            movement.stopMoving();
            return;
        }
        
        // Convert tile coordinates to world coordinates (center of tile)
        float waypointWorldX = waypoint[0] * TileMap.TILE_SIZE + TileMap.TILE_SIZE / 2f;
        float waypointWorldY = waypoint[1] * TileMap.TILE_SIZE + TileMap.TILE_SIZE / 2f;
        
        // Check if we're close to the waypoint
        float dx = waypointWorldX - position.x;
        float dy = waypointWorldY - position.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (distance < 5f) {
            // Reached waypoint, move to next
            path.advanceWaypoint();
            
            if (!path.isFollowing) {
                // Reached final destination
                movement.stopMoving();
            } else {
                // Set target to next waypoint
                int[] nextWaypoint = path.getCurrentWaypoint();
                if (nextWaypoint != null) {
                    float nextX = nextWaypoint[0] * TileMap.TILE_SIZE + TileMap.TILE_SIZE / 2f;
                    float nextY = nextWaypoint[1] * TileMap.TILE_SIZE + TileMap.TILE_SIZE / 2f;
                    movement.setTarget(nextX, nextY, movement.isRunning);
                }
            }
        } else {
            // Still moving to current waypoint
            if (!movement.isMoving) {
                movement.setTarget(waypointWorldX, waypointWorldY, movement.isRunning);
            }
        }
    }
    
    private String getIdleAnimationForDirection(int direction) {
        switch(direction) {
            case Movement.DIR_EAST:
                return Sprite.ANIM_IDLE_RIGHT;
            case Movement.DIR_SOUTH_EAST:
                return Sprite.ANIM_IDLE_DOWN_RIGHT;
            case Movement.DIR_SOUTH:
                return Sprite.ANIM_IDLE_DOWN;
            case Movement.DIR_SOUTH_WEST:
                return Sprite.ANIM_IDLE_DOWN_LEFT;
            case Movement.DIR_WEST:
                return Sprite.ANIM_IDLE_LEFT;
            case Movement.DIR_NORTH_WEST:
                return Sprite.ANIM_IDLE_UP_LEFT;
            case Movement.DIR_NORTH:
                return Sprite.ANIM_IDLE_UP;
            case Movement.DIR_NORTH_EAST:
                return Sprite.ANIM_IDLE_UP_RIGHT;
            default:
                return Sprite.ANIM_IDLE_DOWN;
        }
    }
    
    private void moveTowardsTarget(Entity entity, Movement movement, Position position, float delta) {
        float dx = movement.targetX - position.x;
        float dy = movement.targetY - position.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (distance < 2f) {
            position.x = movement.targetX;
            position.y = movement.targetY;
            movement.stopMoving();
            return;
        }
        
        float moveAmount = movement.currentSpeed * delta;
        
        if (moveAmount >= distance) {
            // Would reach destination - check if destination is valid
            CollisionBox collisionBox = entity.getComponent(CollisionBox.class);
            TileMap map = state.getMap();
            
            if (collisionBox != null && map != null) {
                if (map.collidesWithTiles(collisionBox, movement.targetX, movement.targetY)) {
                    // Destination is blocked, stop here
                    movement.stopMoving();
                    return;
                }
            }
            
            position.x = movement.targetX;
            position.y = movement.targetY;
            movement.stopMoving();
        } else {
            // Moving partial distance
            float ratio = moveAmount / distance;
            float newX = position.x + dx * ratio;
            float newY = position.y + dy * ratio;
            
            // Check collision at new position
            CollisionBox collisionBox = entity.getComponent(CollisionBox.class);
            TileMap map = state.getMap();
            
            if (collisionBox != null && map != null) {
                // Try moving on both axes
                if (!map.collidesWithTiles(collisionBox, newX, newY)) {
                    // No collision, move normally
                    position.x = newX;
                    position.y = newY;
                } else {
                    // Collision detected - try sliding along walls
                    
                    // Try X axis only
                    if (!map.collidesWithTiles(collisionBox, newX, position.y)) {
                        position.x = newX;
                    }
                    // Try Y axis only
                    else if (!map.collidesWithTiles(collisionBox, position.x, newY)) {
                        position.y = newY;
                    }
                    // Completely blocked
                    else {
                        movement.stopMoving();
                        return;
                    }
                }
            } else {
                // No collision box, move freely
                position.x = newX;
                position.y = newY;
            }
            
            movement.direction = calculateDirection(dx, dy);
        }
    }
    
    private int calculateDirection(float dx, float dy) {
        // Returns 0-7 for 8 directions
        // 0=East, 1=SE, 2=South, 3=SW, 4=West, 5=NW, 6=North, 7=NE
        double angle = Math.atan2(dy, dx);
        int direction = (int) Math.round(angle / (Math.PI / 4));
        return (direction + 8) % 8;
    }
    
    private String getWalkAnimationForDirection(int direction) {
        switch(direction) {
            case Movement.DIR_EAST:
                return Sprite.ANIM_WALK_RIGHT;
            case Movement.DIR_SOUTH_EAST:
                return Sprite.ANIM_WALK_DOWN_RIGHT;
            case Movement.DIR_SOUTH:
                return Sprite.ANIM_WALK_DOWN;
            case Movement.DIR_SOUTH_WEST:
                return Sprite.ANIM_WALK_DOWN_LEFT;
            case Movement.DIR_WEST:
                return Sprite.ANIM_WALK_LEFT;
            case Movement.DIR_NORTH_WEST:
                return Sprite.ANIM_WALK_UP_LEFT;
            case Movement.DIR_NORTH:
                return Sprite.ANIM_WALK_UP;
            case Movement.DIR_NORTH_EAST:
                return Sprite.ANIM_WALK_UP_RIGHT;
            default:
                return Sprite.ANIM_WALK_DOWN;
        }
    }
    
    private String getRunAnimationForDirection(int direction) {
        switch(direction) {
            case Movement.DIR_EAST:
                return Sprite.ANIM_RUN_RIGHT;
            case Movement.DIR_SOUTH_EAST:
                return Sprite.ANIM_RUN_DOWN_RIGHT;
            case Movement.DIR_SOUTH:
                return Sprite.ANIM_RUN_DOWN;
            case Movement.DIR_SOUTH_WEST:
                return Sprite.ANIM_RUN_DOWN_LEFT;
            case Movement.DIR_WEST:
                return Sprite.ANIM_RUN_LEFT;
            case Movement.DIR_NORTH_WEST:
                return Sprite.ANIM_RUN_UP_LEFT;
            case Movement.DIR_NORTH:
                return Sprite.ANIM_RUN_UP;
            case Movement.DIR_NORTH_EAST:
                return Sprite.ANIM_RUN_UP_RIGHT;
            default:
                return Sprite.ANIM_RUN_DOWN;
        }
    }
    private void updateCamera(float delta) {
        Entity player = state.getPlayer();
        Position playerPos = player.getComponent(Position.class);
        TileMap map = state.getMap();
        
        if (playerPos != null && map != null) {
            float targetX = playerPos.x - Engine.WIDTH / 2;
            float targetY = playerPos.y - Engine.HEIGHT / 2;
            
            // Clamp to map bounds
            float maxCameraX = map.getWidthInPixels() - Engine.WIDTH;
            float maxCameraY = map.getHeightInPixels() - Engine.HEIGHT;
            
            targetX = Math.max(0, Math.min(targetX, maxCameraX));
            targetY = Math.max(0, Math.min(targetY, maxCameraY));
            
            // Smooth lerp
            float currentX = state.getCameraX();
            float currentY = state.getCameraY();
            
            float newX = currentX + (targetX - currentX) * cameraLerpSpeed * delta;
            float newY = currentY + (targetY - currentY) * cameraLerpSpeed * delta;
            
            state.setCameraPosition(newX, newY);
        }
    } 
    /**
     * Request pathfinding to a world position
     
    public void movePlayerTo(float worldX, float worldY, boolean run) {
        Entity player = state.getPlayer();
        Position position = player.getComponent(Position.class);
        Movement movement = player.getComponent(Movement.class);
        Path path = player.getComponent(Path.class);
        
        if (position == null || movement == null || path == null) {
            return;
        }
        
        // Determine smart starting point for pathfinding
        int startTileX, startTileY;
        
        if (path.isFollowing && path.waypoints != null && path.currentWaypoint < path.waypoints.size()) {
            // Hero is already following a path - start from current/next waypoint
            int[] currentWaypoint = path.waypoints.get(path.currentWaypoint);
            startTileX = currentWaypoint[0];
            startTileY = currentWaypoint[1];
            
            System.out.println("Recalculating path from current waypoint (" + startTileX + ", " + startTileY + ")");
        } else {
            // Not following a path - start from hero's current position
            startTileX = (int)(position.x / TileMap.TILE_SIZE);
            startTileY = (int)(position.y / TileMap.TILE_SIZE);
            
            System.out.println("New path from hero position (" + startTileX + ", " + startTileY + ")");
        }
        
        int goalTileX = (int)(worldX / TileMap.TILE_SIZE);
        int goalTileY = (int)(worldY / TileMap.TILE_SIZE);
        
        // Check if clicking the same tile we're already heading to
        if (path.isFollowing && path.waypoints != null && !path.waypoints.isEmpty()) {
            int[] lastWaypoint = path.waypoints.get(path.waypoints.size() - 1);
            if (lastWaypoint[0] == goalTileX && lastWaypoint[1] == goalTileY) {
                // Same destination, just update run state
                movement.isRunning = run;
                return;
            }
        }
        
        // Find new path
        Pathfinder pathfinder = state.getPathfinder();
        List<int[]> foundPath = pathfinder.findPath(startTileX, startTileY, goalTileX, goalTileY);
        
        if (foundPath != null) {
            path.setPath(foundPath);
            movement.isRunning = run;
            System.out.println("Path found with " + foundPath.size() + " waypoints");
        } else {
            System.out.println("No path to destination!");
            path.clear();
            movement.stopMoving();
        }
    }
    */
    public void movePlayerTo(float worldX, float worldY, boolean run) {
        Entity player = state.getPlayer();
        Position position = player.getComponent(Position.class);
        Movement movement = player.getComponent(Movement.class);
        Path path = player.getComponent(Path.class);
        TargetIndicator indicator = player.getComponent(TargetIndicator.class);  // NEW
        
        if (position == null || movement == null || path == null) {
            return;
        }
        
        // Determine smart starting point for pathfinding
        int startTileX, startTileY;
        
        if (path.isFollowing && path.waypoints != null && path.currentWaypoint < path.waypoints.size()) {
            int[] currentWaypoint = path.waypoints.get(path.currentWaypoint);
            startTileX = currentWaypoint[0];
            startTileY = currentWaypoint[1];
        } else {
            startTileX = (int)(position.x / TileMap.TILE_SIZE);
            startTileY = (int)(position.y / TileMap.TILE_SIZE);
        }
        
        int goalTileX = (int)(worldX / TileMap.TILE_SIZE);
        int goalTileY = (int)(worldY / TileMap.TILE_SIZE);
        
        // Check if clicking the same tile
        if (path.isFollowing && path.waypoints != null && !path.waypoints.isEmpty()) {
            int[] lastWaypoint = path.waypoints.get(path.waypoints.size() - 1);
            if (lastWaypoint[0] == goalTileX && lastWaypoint[1] == goalTileY) {
                movement.isRunning = run;
                return;
            }
        }
        
        // Find new path
        Pathfinder pathfinder = state.getPathfinder();
        List<int[]> foundPath = pathfinder.findPath(startTileX, startTileY, goalTileX, goalTileY);
        
        if (foundPath != null) {
            path.setPath(foundPath);
            movement.isRunning = run;
            
            // Set target indicator at final destination
            if (indicator != null) {
                indicator.setTarget(worldX, worldY);
            }
            
            System.out.println("Path found with " + foundPath.size() + " waypoints");
        } else {
            System.out.println("No path to destination!");
            path.clear();
            movement.stopMoving();
            
            if (indicator != null) {
                indicator.clear();
            }
        }
    }
    public void setCameraLerpSpeed(float speed) {
        this.cameraLerpSpeed = speed;
    }
}
/*

## Expected Sprite Sheet Layout

Your sprites sheet should have rows like this:
```
Row 0: Idle animation (facing down or neutral)
Row 1: Walk Down
Row 2: Walk Up
Row 3: Walk Left
Row 4: Walk Right
Row 5: Walk Down-Left (diagonal)
Row 6: Walk Down-Right (diagonal)
Row 7: Walk Up-Left (diagonal)
Row 8: Walk Up-Right (diagonal)
*/