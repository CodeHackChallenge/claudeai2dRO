package dev.main;
 

/**
 * Component to track a monster's level and tier
 * Used for XP calculation and scaling
 */
public class MonsterLevel implements Component {
    public int level;
    public MobTier tier;
    
    public MonsterLevel(int level, MobTier tier) {
        this.level = level;
        this.tier = tier;
    }
    
    /**
     * Calculate XP reward for killing this monster
     * Formula: BaseXP × LevelFactor × TierMultiplier
     */
    public int calculateXPReward() {
        // Base XP calculation
        int baseXP = 50; // Base XP per kill
        
        // Level scaling (quadratic growth)
        double levelFactor = 1.0 + (level * 0.5);
        
        // Tier multipliers
        double tierMultiplier;
        switch (tier) {
            case TRASH:
                tierMultiplier = 0.5;
                break;
            case NORMAL:
                tierMultiplier = 1.0;
                break;
            case ELITE:
                tierMultiplier = 2.5;
                break;
            case MINIBOSS:
                tierMultiplier = 5.0;
                break;
            default:
                tierMultiplier = 1.0;
        }
        
        int totalXP = (int)(baseXP * levelFactor * tierMultiplier);
        
        return Math.max(10, totalXP); // Minimum 10 XP
    }
    
    @Override
    public String toString() {
        return "Lv" + level + " " + tier;
    }
}