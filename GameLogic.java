package dev.main;

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
            
            if (movement != null && position != null && sprite != null && stats != null) {
                if (movement.isMoving) {
                    // Check if running and has stamina
                    if (movement.isRunning) {
                        // Try to consume stamina
                        boolean hasStamina = stats.consumeStamina(movement.staminaCostPerSecond * delta);
                        
                        if (!hasStamina) {
                            // Out of stamina, switch to walking
                            movement.stopRunning();
                        }
                    }
                    
                    // Move the entity
                    moveTowardsTarget(entity, movement, position, delta);
                    
                    // Set animation based on movement state
                    String moveAnim;
                    if (movement.isRunning) {
                        moveAnim = getRunAnimationForDirection(movement.direction);
                    } else {
                        moveAnim = getWalkAnimationForDirection(movement.direction);
                    }
                    sprite.setAnimation(moveAnim);
                    
                } else {
                    // Not moving - idle animation and regenerate stamina
                    String idleAnim = getIdleAnimationForDirection(movement.lastDirection);
                    sprite.setAnimation(idleAnim);
                    
                    // Regenerate stamina when not running
                    stats.regenerateStamina(delta);
                }
            }
            
            // Always regenerate stamina when walking (not running)
            if (stats != null && movement != null && movement.isMoving && !movement.isRunning) {
                stats.regenerateStamina(delta);
            }
            
            if (sprite != null) {
                sprite.update(delta);
            }
        }
        
        updateCamera(delta);
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
    
    public void movePlayerTo(float worldX, float worldY, boolean run) {
        Entity player = state.getPlayer();
        Movement movement = player.getComponent(Movement.class);
        
        if (movement != null) {
            movement.setTarget(worldX, worldY, run);
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