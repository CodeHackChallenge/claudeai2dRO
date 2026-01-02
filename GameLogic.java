package dev.main;

import java.util.List;

public class GameLogic {
    
    private GameState state;
    private float cameraLerpSpeed = 5f;
    
    public GameLogic(GameState state) {
        this.state = state;
    } 
    
    public void update(float delta) {
        state.incrementGameTime(delta);
        
        for (Entity entity : state.getEntities()) {
            Movement movement = entity.getComponent(Movement.class);
            Position position = entity.getComponent(Position.class);
            Sprite sprite = entity.getComponent(Sprite.class);
            Stats stats = entity.getComponent(Stats.class);
            Path path = entity.getComponent(Path.class);
            TargetIndicator indicator = entity.getComponent(TargetIndicator.class);  // NEW
            
            if (movement != null && position != null && sprite != null && stats != null) {
                
                // Follow path if one exists
                if (path != null && path.isFollowing) {
                    followPath(entity, path, movement, position);
                }
                
                if (movement.isMoving) {
                    // Handle stamina
                    if (movement.isRunning) {
                        boolean hasStamina = stats.consumeStamina(movement.staminaCostPerSecond * delta);
                        if (!hasStamina) {
                            movement.stopRunning();
                        }
                    }
                    
                    moveTowardsTarget(entity, movement, position, delta);
                    
                    String moveAnim;
                    if (movement.isRunning) {
                        moveAnim = getRunAnimationForDirection(movement.direction);
                    } else {
                        moveAnim = getWalkAnimationForDirection(movement.direction);
                    }
                    sprite.setAnimation(moveAnim);
                    
                } else {
                    // Idle - clear target indicator
                    if (indicator != null) {
                        indicator.clear();
                    }
                    
                    String idleAnim = getIdleAnimationForDirection(movement.lastDirection);
                    sprite.setAnimation(idleAnim);
                    stats.regenerateStamina(delta);
                }
            }
            
            if (stats != null && movement != null && movement.isMoving && !movement.isRunning) {
                stats.regenerateStamina(delta);
            }
            
            if (sprite != null) {
                sprite.update(delta);
            }
            
            // Update target indicator animation
            if (indicator != null) {
                indicator.update(delta);
            }
        }
        
        updateCamera(delta);
    }

    /**
     * Follow the current path waypoint by waypoint
     */
    private void followPath(Entity entity, Path path, Movement movement, Position position) {
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