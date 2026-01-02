package dev.main;

import java.util.concurrent.ThreadLocalRandom;

public class AI implements Component {
    
    // AI State
    public enum State {
        IDLE,
        ROAMING,
        CHASING,
        RETURNING,
        ATTACKING,
        DEAD
    }
    
    public State currentState;
    public String behaviorType;  // "passive", "aggressive", "neutral"
    
    // Home position (spawn point)
    public float homeX;
    public float homeY;
    public float roamRadius;  // How far can roam from home
    
    // Detection
    public float detectionRange;  // Tiles away to detect player
    public Entity target;
    
    // Roaming
    public float roamTimer;
    public float roamInterval;  // Time between roam movements
    
    // Returning
    public float returnThreshold;  // Distance from home before returning
    
    // Attack
    public float attackRange;
    public float attackCooldown;
    public float attackTimer;
    
    public AI(String behaviorType, float homeX, float homeY, float roamRadius, float detectionRange) {
        this.behaviorType = behaviorType;
        this.currentState = State.IDLE;
        
        this.homeX = homeX;
        this.homeY = homeY;
        this.roamRadius = roamRadius;
        
        this.detectionRange = detectionRange;
        this.target = null;
        
        this.roamTimer = 0;
        this.roamInterval = ThreadLocalRandom.current().nextFloat(3f, 6f);  // 3-6 seconds
        
        this.returnThreshold = roamRadius + 64f;  // Slightly beyond roam radius
        
        this.attackRange = 32f;  // ~half a tile
        this.attackCooldown = 1.5f;  // 1.5 seconds between attacks
        this.attackTimer = 0;
    }
    
    public void update(float delta) {
        roamTimer += delta;
        
        if (attackTimer > 0) {
            attackTimer -= delta;
        }
    }
    
    public boolean canAttack() {
        return attackTimer <= 0;
    }
    
    public void resetAttackCooldown() {
        attackTimer = attackCooldown;
    }
}