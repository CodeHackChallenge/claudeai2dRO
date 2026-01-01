package dev.main;

public class Stats implements Component {
    // Health
    public int hp;
    public int maxHp;
    
    // Stamina
    public float stamina;
    public float maxStamina;
    public float staminaRegenRate;  // stamina per second
    
    // Combat stats
    public int attack;
    public int defense;
    
    public Stats(int maxHp, float maxStamina, int attack, int defense) {
        this.maxHp = maxHp;
        this.hp = maxHp;  // Start at full health
        
        this.maxStamina = maxStamina;
        this.stamina = maxStamina;  // Start at full stamina
        this.staminaRegenRate = 10f;  // Regenerate 10 stamina per second
        
        this.attack = attack;
        this.defense = defense;
    }
    
    // Consume stamina, returns false if not enough
    public boolean consumeStamina(float amount) {
        if (stamina >= amount) {
            stamina -= amount;
            return true;
        }
        return false;
    }
    
    // Regenerate stamina
    public void regenerateStamina(float delta) {
        stamina = Math.min(maxStamina, stamina + staminaRegenRate * delta);
    }
}