package dev.main;

public class Stats implements Component {
    // Base stats (set at character creation, don't change with level)
    public int baseMaxHp;
    public int baseAttack;
    public int baseDefense;
    public int baseAccuracy;
    public int baseMaxMana;  // ☆ NEW: Base max mana
    
    // Current stats (calculated from base + level bonuses)
    public int hp;
    public int maxHp;
    public int attack;
    public int defense;
    public int accuracy;
    
    // ☆ NEW: Mana system
    public int mana;
    public int maxMana;
    public float manaRegenRate;  // MP regen per second
    
    // Stamina (doesn't scale with level)
    public float stamina;
    public float maxStamina;
    public float staminaRegenRate;
    
    // Stats that don't grow with level
    public int evasion;
    public int magicAttack;
    public int magicDefense;
    
    // Element resistances (percentage 0-100)
    public int fireResistance;
    public int lightningResistance;
    public int poisonResistance;
    
    // Debuff resistances (percentage 0-100)
    public int silenceResistance;
    public int blindResistance;
    public int curseResistance;
    
    /**
     * Constructor with base stats (for player at level 1)
     */
    public Stats(int baseMaxHp, int baseAttack, int baseDefense, int baseAccuracy, float maxStamina, int baseMaxMana) {
        // Store base stats
        this.baseMaxHp = baseMaxHp;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.baseAccuracy = baseAccuracy;
        this.baseMaxMana = baseMaxMana;
        
        // Initialize current stats to base (will be updated by applyLevelStats)
        this.maxHp = baseMaxHp;
        this.hp = baseMaxHp;
        this.attack = baseAttack;
        this.defense = baseDefense;
        this.accuracy = baseAccuracy;
        
        // ☆ NEW: Initialize mana
        this.maxMana = baseMaxMana;
        this.mana = baseMaxMana;
        this.manaRegenRate = baseMaxMana * 0.01f;  // 1% of max mana per second
        
        // Stamina
        this.maxStamina = maxStamina;
        this.stamina = maxStamina;
        this.staminaRegenRate = 10f;
        
        // Non-growing stats
        this.evasion = 0;
        this.magicAttack = 0;
        this.magicDefense = 0;
        
        // Resistances
        this.fireResistance = 0;
        this.lightningResistance = 0;
        this.poisonResistance = 0;
        
        this.silenceResistance = 0;
        this.blindResistance = 0;
        this.curseResistance = 0;
    }
    
    /**
     * Old constructor for backwards compatibility (monsters, no mana)
     */
    public Stats(int maxHp, float maxStamina, int attack, int defense) {
        this.baseMaxHp = maxHp;
        this.maxHp = maxHp;
        this.hp = maxHp;
        
        this.baseAttack = attack;
        this.attack = attack;
        
        this.baseDefense = defense;
        this.defense = defense;
        
        this.baseAccuracy = 0;
        this.accuracy = 0;
        
        this.baseMaxMana = 0;
        this.maxMana = 0;
        this.mana = 0;
        this.manaRegenRate = 0f;
        
        this.maxStamina = maxStamina;
        this.stamina = maxStamina;
        this.staminaRegenRate = 10f;
        
        // Non-growing stats
        this.evasion = 0;
        this.magicAttack = 0;
        this.magicDefense = 0;
        
        // Resistances
        this.fireResistance = 0;
        this.lightningResistance = 0;
        this.poisonResistance = 0;
        
        this.silenceResistance = 0;
        this.blindResistance = 0;
        this.curseResistance = 0;
    }
    
    /**
     * Apply level-based stat bonuses from Experience component
     * Call this after leveling up to recalculate stats
     * @param exp The experience component
     * @param fullHeal If true, restore HP, mana, and stamina to full (default for level-ups)
     */
    public void applyLevelStats(Experience exp, boolean fullHeal) {
        // Calculate new max stats
        this.maxHp = exp.calculateMaxHP(baseMaxHp);
        this.attack = exp.calculateAttack(baseAttack);
        this.defense = exp.calculateDefense(baseDefense);
        this.accuracy = exp.calculateAccuracy(baseAccuracy);
        
        // ☆ NEW: Calculate max mana
        this.maxMana = exp.calculateMaxMana(baseMaxMana);
        this.manaRegenRate = maxMana * 0.01f;  // Update regen rate (1% of new max)
        
        // ☆ FULL HEAL on level up
        if (fullHeal) {
            this.hp = this.maxHp;
            this.stamina = this.maxStamina;
            this.mana = this.maxMana;  // ☆ NEW: Full mana restore
        } else {
            // Just cap HP/mana at new max if not healing
            if (this.hp > this.maxHp) {
                this.hp = this.maxHp;
            }
            if (this.mana > this.maxMana) {
                this.mana = this.maxMana;
            }
        }
    }
    
    /**
     * Apply level stats without healing (for initialization)
     */
    public void applyLevelStats(Experience exp) {
        applyLevelStats(exp, false);
    }
    
    /**
     * Fully restore HP, stamina, and mana
     */
    public void fullHeal() {
        this.hp = this.maxHp;
        this.stamina = this.maxStamina;
        this.mana = this.maxMana;  // ☆ NEW
    }
    
    /**
     * ☆ NEW: Consume mana, returns false if not enough
     */
    public boolean consumeMana(int amount) {
        if (mana >= amount) {
            mana -= amount;
            return true;
        }
        return false;
    }
    
    /**
     * ☆ NEW: Regenerate mana
     */
    public void regenerateMana(float delta) {
        if (mana < maxMana) {
            mana = Math.min(maxMana, (int)(mana + manaRegenRate * delta));
        }
    }
    
    /**
     * Get hit chance against target
     * Returns value from 0.0 (never hit) to 1.0 (always hit)
     */
    public float getHitChance(Stats targetStats) {
        // Base 95% hit chance, modified by accuracy vs evasion
        float baseHitChance = 0.95f;
        float accModifier = this.accuracy * 0.01f; // Each point = +1% hit
        float evaModifier = targetStats.evasion * 0.01f; // Each point = -1% hit
        
        float hitChance = baseHitChance + accModifier - evaModifier;
        
        // Clamp between 5% and 100%
        return Math.max(0.05f, Math.min(1.0f, hitChance));
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