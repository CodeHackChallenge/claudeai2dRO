package dev.main;

/**
 * Represents an item in the game with stats, durability, and properties
 * UPDATED: Added stackable support
 */
public class Item {
    public enum ItemType {
        WEAPON,      // "Weap" tab
        ARMOR,       // "Arm" tab
        ACCESSORY,   // "Acc" tab
        CONSUMABLE,  // "Misc" tab (potions, food)
        MATERIAL     // "Misc" or "Rune" tab (materials, runes)
    }

    public enum Rarity {
        COMMON,
        UNCOMMON,
        RARE,
        EPIC,
        LEGENDARY
    }

    // Basic properties
    private String name;
    private ItemType type;
    private Rarity rarity;

    // Stats bonuses
    private int attackBonus;
    private int defenseBonus;
    private int magicAttackBonus;
    private int magicDefenseBonus;

    // Durability system
    private int maxDurability;
    private int currentDurability;

    // Item flags
    private boolean upgradable;
    private boolean canInfuseElemental;
    private boolean tradable;
    private boolean sellable;

    // Dismantle threshold
    private int dismantleThreshold;
    
    // ★ NEW: Stackable properties
    private boolean stackable;
    private int maxStackSize;

    public Item(String name, ItemType type, Rarity rarity,
                int attackBonus, int defenseBonus, int magicAttackBonus, int magicDefenseBonus,
                int durability, boolean upgradable, boolean canInfuseElemental,
                boolean tradable, boolean sellable, int dismantleThreshold) {
        this.name = name;
        this.type = type;
        this.rarity = rarity;
        this.attackBonus = attackBonus;
        this.defenseBonus = defenseBonus;
        this.magicAttackBonus = magicAttackBonus;
        this.magicDefenseBonus = magicDefenseBonus;
        this.maxDurability = durability;
        this.currentDurability = durability;
        this.upgradable = upgradable;
        this.canInfuseElemental = canInfuseElemental;
        this.tradable = tradable;
        this.sellable = sellable;
        this.dismantleThreshold = dismantleThreshold;
        
        // ★ NEW: Auto-determine stackability based on type
        this.stackable = determineStackability(type);
        this.maxStackSize = determineMaxStackSize(type);
    }
    
    // ★ NEW: Constructor with explicit stackable parameters
    public Item(String name, ItemType type, Rarity rarity,
                int attackBonus, int defenseBonus, int magicAttackBonus, int magicDefenseBonus,
                int durability, boolean upgradable, boolean canInfuseElemental,
                boolean tradable, boolean sellable, int dismantleThreshold,
                boolean stackable, int maxStackSize) {
        this.name = name;
        this.type = type;
        this.rarity = rarity;
        this.attackBonus = attackBonus;
        this.defenseBonus = defenseBonus;
        this.magicAttackBonus = magicAttackBonus;
        this.magicDefenseBonus = magicDefenseBonus;
        this.maxDurability = durability;
        this.currentDurability = durability;
        this.upgradable = upgradable;
        this.canInfuseElemental = canInfuseElemental;
        this.tradable = tradable;
        this.sellable = sellable;
        this.dismantleThreshold = dismantleThreshold;
        this.stackable = stackable;
        this.maxStackSize = maxStackSize;
    }
    
    /**
     * ★ NEW: Auto-determine if item should be stackable based on type
     */
    private boolean determineStackability(ItemType type) {
        switch (type) {
            case WEAPON:
            case ARMOR:
            case ACCESSORY:
                return false; // Gear is not stackable
            case CONSUMABLE:
            case MATERIAL:
                return true;  // Consumables and materials are stackable
            default:
                return false;
        }
    }
    
    /**
     * ★ NEW: Auto-determine max stack size based on type
     */
    private int determineMaxStackSize(ItemType type) {
        switch (type) {
            case CONSUMABLE:
                return 99;   // Potions stack to 99
            case MATERIAL:
                return 999;  // Materials stack to 999
            case WEAPON:
            case ARMOR:
            case ACCESSORY:
            default:
                return 1;    // Gear doesn't stack
        }
    }

    // ★ NEW: Stackable getters
    public boolean isStackable() { 
        return stackable; 
    }
    
    public int getMaxStackSize() { 
        return maxStackSize; 
    }
    
    /**
     * ★ NEW: Check if this item can stack with another
     */
    public boolean canStackWith(Item other) {
        if (!this.stackable || !other.stackable) {
            return false;
        }
        
        // Items can stack if they have the same name and type
        return this.name.equals(other.name) && 
               this.type == other.type &&
               this.rarity == other.rarity;
    }

    // Existing getters
    public String getName() { return name; }
    public ItemType getType() { return type; }
    public Rarity getRarity() { return rarity; }

    public int getAttackBonus() { return attackBonus; }
    public int getDefenseBonus() { return defenseBonus; }
    public int getMagicAttackBonus() { return magicAttackBonus; }
    public int getMagicDefenseBonus() { return magicDefenseBonus; }

    public int getMaxDurability() { return maxDurability; }
    public int getCurrentDurability() { return currentDurability; }
    public boolean isUpgradable() { return upgradable; }
    public boolean canInfuseElemental() { return canInfuseElemental; }
    public boolean isTradable() { return tradable; }
    public boolean isSellable() { return sellable; }
    public int getDismantleThreshold() { return dismantleThreshold; }

    // Durability methods
    public void reduceDurability(int amount) {
        currentDurability = Math.max(0, currentDurability - amount);
    }

    public void repairDurability(int amount) {
        currentDurability = Math.min(maxDurability, currentDurability + amount);
    }

    public boolean canDismantle() {
        return currentDurability < dismantleThreshold;
    }

    public boolean isBroken() {
        return currentDurability <= 0;
    }

    // Utility methods
    public boolean isWeapon() { return type == ItemType.WEAPON; }
    public boolean isArmor() { return type == ItemType.ARMOR; }
    public boolean isAccessory() { return type == ItemType.ACCESSORY; }
    public boolean isConsumable() { return type == ItemType.CONSUMABLE; }
    public boolean isMaterial() { return type == ItemType.MATERIAL; }

    @Override
    public String toString() {
        String base = name + " (" + rarity + " " + type + ")";
        if (stackable) {
            base += " [Stackable: " + maxStackSize + "]";
        }
        if (maxDurability > 0) {
            base += " - Dur: " + currentDurability + "/" + maxDurability;
        }
        return base;
    }
}