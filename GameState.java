package dev.main;

  

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
//map = new TileMap("/maps/map_96x96.txt");
public class GameState {
    
    private TileMap map;
    private List<Entity> entities;
    private List<Entity> entitiesToRemove;  // NEW: For safe removal
    private Entity player;
    private Pathfinder pathfinder;
    
    private float gameTime;
    private float cameraX;
    private float cameraY;
    
    public GameState() {
        entities = new ArrayList<>();
        entitiesToRemove = new ArrayList<>();
        gameTime = 0f;
        cameraX = 0f;
        cameraY = 0f;
        
        map = new TileMap("/maps/map_96x96.txt");
        pathfinder = new Pathfinder(map);
        
        initializeWorld();
    }
    
    private void initializeWorld() {
        // Create player
        //float centerX = (map.getWidthInPixels()) / 2f;
        //float centerY = (map.getHeightInPixels()) / 2f;
        
        player = EntityFactory.createPlayer(64, 64);
        entities.add(player);
        
        // Spawn some monsters
        //spawnMonster("Slime", 400, 400);
        //spawnMonster("Slime", 600, 300);
        spawnMonster("Goblin", 800, 500);
        //spawnMonster("Goblin", 300, 700);
        //spawnMonster("Poring", 500, 600);
    }
    
    public void spawnMonster(String type, float x, float y) {
        Entity monster = EntityFactory.createMonster(type, x, y);
        entities.add(monster);
        System.out.println("Spawned " + type + " at (" + x + ", " + y + ")");
    }
    
    public void markForRemoval(Entity entity) {
        if (!entitiesToRemove.contains(entity)) {
            entitiesToRemove.add(entity);
        }
    }
    
    public void removeMarkedEntities() {
        for (Entity entity : entitiesToRemove) {
            entities.remove(entity);
            System.out.println("Removed " + entity.getName());
        }
        entitiesToRemove.clear();
    }
    
    public List<Entity> getEntities() {
        return entities;
    }
    
    public Entity getPlayer() {
        return player;
    }
    
    public TileMap getMap() {
        return map;
    }
    
    public Pathfinder getPathfinder() {
        return pathfinder;
    }
    
    public float getGameTime() {
        return gameTime;
    }
    
    public void incrementGameTime(float delta) {
        gameTime += delta;
    }
    
    public float getCameraX() {
        return cameraX;
    }
    
    public float getCameraY() {
        return cameraY;
    }
    
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