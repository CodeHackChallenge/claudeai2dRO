package dev.main;

public class SpawnPoint {
    public String monsterType;
    public float x;
    public float y;
    public float respawnDelay;
    
    public boolean isOccupied;
    public Entity currentMonster;
    
    public float respawnTimer;
    
    public SpawnPoint(String monsterType, float x, float y, float respawnDelay) {
        this.monsterType = monsterType;
        this.x = x;
        this.y = y;
        this.respawnDelay = respawnDelay;
        this.isOccupied = false;
        this.currentMonster = null;
        this.respawnTimer = 0;
    }
    
    public void update(float delta) {
        if (!isOccupied) {
            respawnTimer += delta;
        }
    }
    
    public boolean canRespawn() {
        return !isOccupied && respawnTimer >= respawnDelay;
    }
    
    public void spawn(Entity monster) {
        this.currentMonster = monster;
        this.isOccupied = true;
        this.respawnTimer = 0;
    }
    
    public void onMonsterDeath() {
        this.currentMonster = null;
        this.isOccupied = false;
        this.respawnTimer = 0;  // Start respawn timer
        System.out.println("Spawn point at (" + (int)x + ", " + (int)y + ") will respawn " + monsterType + " in " + respawnDelay + " seconds");
    }
}