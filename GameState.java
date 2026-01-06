package dev.main;

  

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
//map = new TileMap("/maps/map_96x96.txt");
public class GameState {
    
    private TileMap map;
    private List<Entity> entities;
    private List<Entity> entitiesToRemove;  // NEW: For safe removal
    private List<DamageText> damageTexts;  // NEW
    private List<SpawnPoint> spawnPoints;  // NEW
    
    private Entity player;
    private Entity hoveredEntity;  // NEW
    private Entity targetedEntity;  // NEW
    private Entity autoAttackTarget;  // NEW: Auto-attack target
    private Pathfinder pathfinder;
    
    private float gameTime;
    private float cameraX;
    private float cameraY;
    
    public GameState() {
        entities = new ArrayList<>();
        entitiesToRemove = new ArrayList<>();
        damageTexts = new ArrayList<>();  // NEW
        spawnPoints = new ArrayList<>();  // NEW
        
        gameTime = 0f;
        cameraX = 0f;
        cameraY = 0f;
        
        // ⭐ NEW: Pass both map image and collision map
        map = new TileMap("/maps/world_map.png", "/maps/world_collision.txt");
        pathfinder = new Pathfinder(map);
        
        initializeWorld();
    }
    // ★ UPDATE initializeWorld() method:
    private void initializeWorld() {
        // Create player
        player = EntityFactory.createPlayer(8 * 64, 5 * 64);
        entities.add(player);
        
        float normalRespawn = 30f;  // 30 seconds for normal mobs
        float bossRespawn = 50f;    // 50 seconds for bosses 
        
        // Example spawns with level and tier:
        
        // TRASH tier goblins (weak, low XP)
        addSpawnPoint("Goblin", 10 * 64, 10 * 64, normalRespawn, 1, MobTier.TRASH);
        addSpawnPoint("Goblin", 11 * 64, 10 * 64, normalRespawn, 1, MobTier.TRASH);
        //me
        addSpawnPoint("Goblin", 5 * 64, 5 * 64, normalRespawn, 1, MobTier.TRASH);
        addSpawnPoint("Goblin", 7 * 64, 10 * 64, normalRespawn, 1, MobTier.TRASH);
        addSpawnPoint("Goblin", 8 * 64, 10 * 64, normalRespawn, 1, MobTier.TRASH);
        addSpawnPoint("Goblin", 8 * 64, 9 * 64, normalRespawn, 1, MobTier.TRASH);
        addSpawnPoint("Goblin", 9 * 64, 2 * 64, normalRespawn, 1, MobTier.TRASH);
        addSpawnPoint("Goblin", 9 * 64, 5 * 64, normalRespawn, 1, MobTier.TRASH);
        addSpawnPoint("Goblin", 4 * 64, 6 * 64, normalRespawn, 1, MobTier.TRASH);
        addSpawnPoint("Goblin", 4 * 64, 4 * 64, normalRespawn, 1, MobTier.TRASH);
        
        // NORMAL tier goblins
        addSpawnPoint("Goblin", 12 * 64, 10 * 64, normalRespawn, 2, MobTier.NORMAL);
        addSpawnPoint("Goblin", 13 * 64, 10 * 64, normalRespawn, 2, MobTier.NORMAL);
        
        //me
        addSpawnPoint("Goblin", 12 * 64, 2 * 64, normalRespawn, 2, MobTier.NORMAL);
        addSpawnPoint("Goblin", 13 * 64, 2 * 64, normalRespawn, 2, MobTier.NORMAL);
        addSpawnPoint("Goblin", 12 * 64, 3 * 64, normalRespawn, 2, MobTier.NORMAL);
        addSpawnPoint("Goblin", 14 * 64, 10 * 64, normalRespawn, 2, MobTier.NORMAL);
        // ELITE goblin (stronger, more XP)
        addSpawnPoint("Goblin", 14 * 64, 10 * 64, normalRespawn, 3, MobTier.ELITE);
        
        // NORMAL bunnies
        addSpawnPoint("Bunny", 20 * 64, 20 * 64, normalRespawn, 1, MobTier.NORMAL);
        addSpawnPoint("Bunny", 20 * 64, 21 * 64, normalRespawn, 2, MobTier.NORMAL);
        addSpawnPoint("Bunny", 21 * 64, 22 * 64, normalRespawn, 2, MobTier.NORMAL);
        addSpawnPoint("Bunny", 21 * 64, 23 * 64, normalRespawn, 3, MobTier.NORMAL);
        //me
        addSpawnPoint("Bunny", 20 * 64, 20 * 64, normalRespawn, 1, MobTier.NORMAL);
        addSpawnPoint("Bunny", 20 * 64, 22 * 64, normalRespawn, 2, MobTier.NORMAL);
        addSpawnPoint("Bunny", 22 * 64, 22 * 64, normalRespawn, 2, MobTier.NORMAL);
        addSpawnPoint("Bunny", 23 * 64, 23 * 64, normalRespawn, 3, MobTier.NORMAL);
        
        // MINIBOSS spawns (high level, great rewards)
        addSpawnPoint("GoblinBoss", 13 * 64, 12 * 64, bossRespawn, 5, MobTier.MINIBOSS);
        addSpawnPoint("BunnyBoss", 22 * 64, 23 * 64, bossRespawn, 5, MobTier.MINIBOSS);
        addSpawnPoint("MinotaurBoss", 22 * 64, 21 * 64, bossRespawn, 7, MobTier.MINIBOSS);
        
        // Initial spawn of all monsters
        for (SpawnPoint sp : spawnPoints) {
            spawnMonsterAtPoint(sp);
        }
    }
    /**
     * OLD: Backwards compatibility - defaults to level 1, NORMAL tier
     */
   // public void addSpawnPoint(String monsterType, float x, float y, float respawnDelay) {
    //    addSpawnPoint(monsterType, x, y, respawnDelay, 1, MobTier.NORMAL);
   // }

    /**
     * NEW: Add spawn point with level and tier
     */
    public void addSpawnPoint(String monsterType, float x, float y, float respawnDelay, int level, MobTier tier) {
        SpawnPoint sp = new SpawnPoint(monsterType, x, y, respawnDelay, level, tier);
        spawnPoints.add(sp);
        System.out.println("Added spawn point: " + sp);
    }
    
    public void updateSpawnPoints(float delta) {
        for (SpawnPoint sp : spawnPoints) {
            sp.update(delta);
            
            if (sp.canRespawn()) {
                spawnMonsterAtPoint(sp);
            }
        }
    }
    
    public void onMonsterDeath(Entity monster) {
        // Find the spawn point this monster belongs to
        Respawn respawn = monster.getComponent(Respawn.class);
        if (respawn != null) {
            for (SpawnPoint sp : spawnPoints) {
                if (sp.currentMonster == monster) {
                    sp.onMonsterDeath();
                    break;
                }
            }
        }
    }
    
    public List<SpawnPoint> getSpawnPoints() {
        return spawnPoints;
    } 
    /**
     * Spawn monster at spawn point (uses spawn point's level and tier)
     */
    public void spawnMonsterAtPoint(SpawnPoint spawnPoint) {
        if (spawnPoint.isOccupied) {
            return;  // Already has a monster
        }
        
        // ★ Create monster with spawn point's level and tier
        Entity monster = EntityFactory.createMonster(
            spawnPoint.monsterType, 
            spawnPoint.x, 
            spawnPoint.y,
            spawnPoint.level,
            spawnPoint.tier
        );
        
        // Add respawn component so we can track which spawn point this monster belongs to
        monster.addComponent(new Respawn(
            spawnPoint.monsterType, 
            spawnPoint.x, 
            spawnPoint.y, 
            spawnPoint.respawnDelay
        ));
        
        entities.add(monster);
        spawnPoint.spawn(monster);
        
        MonsterLevel monsterLevel = monster.getComponent(MonsterLevel.class);
        Stats stats = monster.getComponent(Stats.class);
        
        System.out.println("Spawned " + spawnPoint.monsterType + 
                         " Lv" + spawnPoint.level + " " + spawnPoint.tier +
                         " at (" + (int)spawnPoint.x + ", " + (int)spawnPoint.y + ")" +
                         " - HP:" + stats.maxHp + " ATK:" + stats.attack + 
                         " DEF:" + stats.defense + " ACC:" + stats.accuracy + 
                         " EVA:" + stats.evasion);
    }
    /**
     * OLD: Backwards compatibility - spawns level 1 NORMAL monster
     */
    public void spawnMonster(String type, float x, float y) {
        spawnMonster(type, x, y, 1, MobTier.NORMAL);
    }
    /**
     * NEW: Spawn monster directly with level and tier
     */
    public void spawnMonster(String type, float x, float y, int level, MobTier tier) {
        Entity monster = EntityFactory.createMonster(type, x, y, level, tier);
        entities.add(monster);
        System.out.println("Spawned " + type + " Lv" + level + " " + tier + " at (" + x + ", " + y + ")");
    }
    
    public Entity getAutoAttackTarget() {
        return autoAttackTarget;
    }
    
    public void setAutoAttackTarget(Entity target) {
        this.autoAttackTarget = target;
    }
    
    public void clearAutoAttackTarget() {
        this.autoAttackTarget = null;
    }
    
    public void addDamageText(DamageText text) {
        damageTexts.add(text);
    }
    
    public List<DamageText> getDamageTexts() {
        return damageTexts;
    }
    
    public void updateDamageTexts(float delta) {
        Iterator<DamageText> it = damageTexts.iterator();
        while (it.hasNext()) {
            DamageText dt = it.next();
            dt.update(delta);
            if (dt.shouldRemove()) {
                it.remove();
            }
        }
    }
    
    public Entity getHoveredEntity() {
        return hoveredEntity;
    }
    
    public void setHoveredEntity(Entity entity) {
        // Hide previous hover
        if (hoveredEntity != null && hoveredEntity != targetedEntity) {
            NameTag tag = hoveredEntity.getComponent(NameTag.class);
            if (tag != null) tag.hide();
        }
        
        this.hoveredEntity = entity;
        
        // Show new hover
        if (hoveredEntity != null) {
            NameTag tag = hoveredEntity.getComponent(NameTag.class);
            if (tag != null) tag.show();
        }
    }
    
    public Entity getTargetedEntity() {
        return targetedEntity;
    }
    
    public void setTargetedEntity(Entity entity) {
        // Hide previous target
        if (targetedEntity != null && targetedEntity != hoveredEntity) {
            NameTag tag = targetedEntity.getComponent(NameTag.class);
            if (tag != null) tag.hide();
        }
        
        this.targetedEntity = entity;
        
        // Show new target
        if (targetedEntity != null) {
            NameTag tag = targetedEntity.getComponent(NameTag.class);
            if (tag != null) tag.show();
        }
    }
     /*       
    public void spawnMonster(String type, float x, float y) {
        Entity monster = EntityFactory.createMonster(type, x, y);
        entities.add(monster);
        System.out.println("Spawned " + type + " at (" + x + ", " + y + ")");
    }*/
    
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