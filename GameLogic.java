package dev.main;

public class GameLogic {
    
    private GameState state;
    private float cameraLerpSpeed = 2f; //higher = snappier, lower = smoother
    
    public GameLogic(GameState state) {
        this.state = state;
    }
    
    public void update(float delta) {
        state.incrementGameTime(delta);
        
        // Update all entities
        for (Entity entity : state.getEntities()) {
            
            // Update movement
            Movement movement = entity.getComponent(Movement.class);
            Position position = entity.getComponent(Position.class);
            
            if (movement != null && position != null && movement.isMoving) {
                moveTowardsTarget(entity, movement, position, delta);
            }
            
            // Update sprite animations
            Sprite sprite = entity.getComponent(Sprite.class);
            if (sprite != null) {
                sprite.update(delta);
            }
        }
        
        updateCamera(delta);
    }
    
    private void moveTowardsTarget(Entity entity, Movement movement, Position position, float delta) {
    	
        float dx = movement.targetX - position.x;
        float dy = movement.targetY - position.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        // Arrived at destination
        if (distance < 2f) {
            position.x = movement.targetX;
            position.y = movement.targetY;
            movement.stopMoving();
            return;
        }
        
        // Move towards target
        float moveAmount = movement.speed * delta;
        
        if (moveAmount >= distance) {
            // Will overshoot, just arrive
            position.x = movement.targetX;
            position.y = movement.targetY;
            movement.stopMoving();
        } else {
            // Move partial distance
            float ratio = moveAmount / distance;
            position.x += dx * ratio;
            position.y += dy * ratio;
            
            // Update facing direction (for sprite animation later)
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
    
    private void updateCamera(float delta) {
        Entity player = state.getPlayer();
        Position playerPos = player.getComponent(Position.class);
        TileMap map = state.getMap();
        
        if (playerPos != null && map != null) {
            // Center camera on player
            float targetX = playerPos.x - Engine.WIDTH / 2;
            float targetY = playerPos.y - Engine.HEIGHT / 2;
            
            // Clamp camera to map bounds
            float maxCameraX = map.getWidthInPixels() - Engine.WIDTH;
            float maxCameraY = map.getHeightInPixels() - Engine.HEIGHT;
            
            targetX = Math.max(0, Math.min(targetX, maxCameraX));
            targetY = Math.max(0, Math.min(targetY, maxCameraY));
            
            // Smooth lerp from current position to target
            float currentX = state.getCameraX();
            float currentY = state.getCameraY();
            
            // Lerp formula: current + (target - current) * speed * delta
            float newX = currentX + (targetX - currentX) * cameraLerpSpeed * delta;
            float newY = currentY + (targetY - currentY) * cameraLerpSpeed * delta;
            
            state.setCameraPosition(newX, newY);
        }
    }
    // Optional: Method to change camera speed
    public void setCameraLerpSpeed(float speed) {
        this.cameraLerpSpeed = speed;
    }
    
    // PUBLIC METHOD - Called from Engine's input handler
    public void movePlayerTo(float worldX, float worldY) {
        Entity player = state.getPlayer();
        Movement movement = player.getComponent(Movement.class);
        
        if (movement != null) {
            movement.setTarget(worldX, worldY);
            System.out.println("Player moving to: " + worldX + ", " + worldY);
        }
    }
}