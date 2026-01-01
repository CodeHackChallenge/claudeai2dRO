package dev.main;

import java.util.ArrayList;
import java.util.List;
//"/sprites/enemy2_64x64.png"
public class GameState {
	
	private TileMap map;
    private List<Entity> entities;
    private Entity player;
    
    private float gameTime;
    private float cameraX;
    private float cameraY;
    
    public GameState() {
        entities = new ArrayList<>();
        gameTime = 0f;
        cameraX = 0f;
        cameraY = 0f;
        
        // Load map first
        map = new TileMap("/maps/map01.txt");
        
        initializeWorld();
    }
    
    private void initializeWorld() {
    	// Spawn player in center of map
        float centerX = (map.getWidthInPixels()) / 2f;
        float centerY = (map.getHeightInPixels()) / 2f;
        
        // Create player with idle animation
        player = createPlayer(centerX, centerY);
    }
    
    private Entity createPlayer(float x, float y) {
        Entity player = new Entity("Player");
        
        player.addComponent(new Position(x, y));
        
        // New Sprite constructor - no longer needs totalFrames parameter
        player.addComponent(new Sprite("/sprites/hero.png", 64, 64, 0.15f));
        
        player.addComponent(new Movement(150f));
        player.addComponent(new Stats(100, 100, 10, 5));
        player.addComponent(new HealthBar(40, 4, 40));
        
        entities.add(player);
        return player;
    }
    
    // Getters
    public List<Entity> getEntities() { return entities; }
    public Entity getPlayer() { return player; }
    public float getGameTime() { return gameTime; }
    public void incrementGameTime(float delta) { gameTime += delta; }
    public float getCameraX() { return cameraX; }
    public float getCameraY() { return cameraY; }
    public TileMap getMap() { return map; }
    public void setCameraPosition(float x, float y) {
        this.cameraX = x;
        this.cameraY = y;
    }
    
    
    
}
/*

## Sprite Sheet Format Expected

Your sprites sheet should look like this:
```
[Frame 0][Frame 1][Frame 2][Frame 3]  ← Row 0 (idle)
[Frame 0][Frame 1][Frame 2][Frame 3]  ← Row 1 (walk down)
[Frame 0][Frame 1][Frame 2][Frame 3]  ← Row 2 (walk up)
...
```

## File Structure
```
src/
  ├── resources/
  │   └── sprites/
  │       └── hero_idle.png  (your animated sprites sheet)
  └── dev/main/
      ├── Engine.java
      ├── GameState.java
      ├── GameLogic.java
      ├── Renderer.java
      ├── Entity.java
      ├── Component.java
      ├── Position.java
      ├── Sprite.java
      └── TextureManager.java
*/