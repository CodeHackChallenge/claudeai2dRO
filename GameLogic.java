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
            
            if (movement != null && position != null && sprite != null) {
                if (movement.isMoving) {
                    // Moving - update position and play walk animation
                    moveTowardsTarget(entity, movement, position, delta);
                    
                    String walkAnim = getWalkAnimationForDirection(movement.direction);
                    sprite.setAnimation(walkAnim);
                } else {
                    // Not moving - play idle animation based on last facing direction (8-way)
                    String idleAnim = getIdleAnimationForDirection(movement.lastDirection);
                    sprite.setAnimation(idleAnim);
                }
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
        
        float moveAmount = movement.speed * delta;
        
        if (moveAmount >= distance) {
            position.x = movement.targetX;
            position.y = movement.targetY;
            movement.stopMoving();
        } else {
            float ratio = moveAmount / distance;
            position.x += dx * ratio;
            position.y += dy * ratio;
            
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
    
    public void movePlayerTo(float worldX, float worldY) {
        Entity player = state.getPlayer();
        Movement movement = player.getComponent(Movement.class);
        
        if (movement != null) {
            movement.setTarget(worldX, worldY);
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