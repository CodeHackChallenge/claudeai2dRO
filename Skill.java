package dev.main;

import java.awt.Color;

/**
 * Represents a skill that can be equipped in skill slots
 */
public class Skill {
    
    public enum SkillType {
        ATTACK,      // Offensive skill
        DEFENSE,     // Defensive skill
        BUFF,        // Buff/support skill
        HEAL,        // Healing skill
        PASSIVE      // Passive ability
    }
    
    private String id;
    private String name;
    private String description;
    private SkillType type;
    private String iconPath;  // Path to skill icon image
    
    // Skill properties
    private float cooldown;
    private float currentCooldown;
    private int manaCost;
    private int levelRequired;
    
    // Visual
    private Color iconColor;  // Fallback color if no icon
    
    public Skill(String id, String name, String description, SkillType type, 
                 float cooldown, int manaCost, int levelRequired) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.cooldown = cooldown;
        this.currentCooldown = 0;
        this.manaCost = manaCost;
        this.levelRequired = levelRequired;
        this.iconPath = null;
        
        // Default colors based on type
        switch (type) {
            case ATTACK:
                this.iconColor = new Color(220, 20, 60);  // Red
                break;
            case DEFENSE:
                this.iconColor = new Color(70, 130, 180);  // Blue
                break;
            case BUFF:
                this.iconColor = new Color(255, 215, 0);  // Gold
                break;
            case HEAL:
                this.iconColor = new Color(50, 205, 50);  // Green
                break;
            case PASSIVE:
                this.iconColor = new Color(138, 43, 226);  // Purple
                break;
            default:
                this.iconColor = Color.GRAY;
        }
    }
    
    /**
     * Update cooldown timer
     */
    public void update(float delta) {
        if (currentCooldown > 0) {
            currentCooldown -= delta;
            if (currentCooldown < 0) {
                currentCooldown = 0;
            }
        }
    }
    
    /**
     * Use the skill (start cooldown)
     */
    public boolean use() {
        if (!isReady()) return false;
        
        currentCooldown = cooldown;
        return true;
    }
    
    /**
     * Check if skill is ready to use
     */
    public boolean isReady() {
        return currentCooldown <= 0;
    }
    
    /**
     * Get cooldown progress (0.0 = ready, 1.0 = just used)
     */
    public float getCooldownProgress() {
        if (cooldown <= 0) return 0f;
        return currentCooldown / cooldown;
    }
    
    /**
     * Get remaining cooldown time
     */
    public float getRemainingCooldown() {
        return currentCooldown;
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public SkillType getType() { return type; }
    public String getIconPath() { return iconPath; }
    public float getCooldown() { return cooldown; }
    public int getManaCost() { return manaCost; }
    public int getLevelRequired() { return levelRequired; }
    public Color getIconColor() { return iconColor; }
    
    // Setters
    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }
    
    public void setIconColor(Color color) {
        this.iconColor = color;
    }
    
    @Override
    public String toString() {
        return name + " (" + type + ")";
    }
}