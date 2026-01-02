package dev.main;

public class Combat implements Component {
    public float attackCooldown;
    public float attackTimer;
    
    // Attack stats
    public float critChance;   // 0.0 to 1.0
    public float critMultiplier;
    public float evasionChance;
    
    // Animation state
    public boolean isAttacking;
    public float attackAnimationTimer;
    public float attackAnimationDuration;
    
    public Combat(float attackCooldown, float critChance, float evasionChance) {
        this.attackCooldown = attackCooldown;
        this.attackTimer = 0;
        this.critChance = critChance;
        this.critMultiplier = 2.0f;
        this.evasionChance = evasionChance;
        this.isAttacking = false;
        this.attackAnimationTimer = 0;
        this.attackAnimationDuration = 0.5f;  // 500ms attack animation
    }
    
    public boolean canAttack() {
        return attackTimer <= 0 && !isAttacking;
    }
    
    public void startAttack() {
        isAttacking = true;
        attackAnimationTimer = 0;
        attackTimer = attackCooldown;
    }
    
    public void update(float delta) {
        if (attackTimer > 0) {
            attackTimer -= delta;
        }
        
        if (isAttacking) {
            attackAnimationTimer += delta;
            if (attackAnimationTimer >= attackAnimationDuration) {
                isAttacking = false;
                attackAnimationTimer = 0;
            }
        }
    }
}