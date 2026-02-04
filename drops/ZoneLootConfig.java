package dev.main.drops;

import java.util.*;

/**
 * Zone-specific loot configuration loaded from map JSON
 */
public class ZoneLootConfig {
    
    public enum LootTier {
        BEGINNER(1.0),
        NOVICE(1.2),
        INTERMEDIATE(1.5),
        ADVANCED(2.0),
        EXPERT(3.0),
        MASTER(5.0);
        
        private final double qualityMultiplier;
        
        LootTier(double qualityMultiplier) {
            this.qualityMultiplier = qualityMultiplier;
        }
        
        public double getQualityMultiplier() {
            return qualityMultiplier;
        }
    }
    
    private final LootTier lootTier;
    private final Map<DropRarity, Double> rarityMultipliers;
    private final List<ZoneDropItem> extraDrops;
    private final List<GuaranteedDrop> guaranteedDrops;
    
    // Track guaranteed drops that have been claimed
    private final Set<String> claimedGuaranteedDrops;
    
    public ZoneLootConfig(LootTier lootTier) {
        this.lootTier = lootTier;
        this.rarityMultipliers = new EnumMap<>(DropRarity.class);
        this.extraDrops = new ArrayList<>();
        this.guaranteedDrops = new ArrayList<>();
        this.claimedGuaranteedDrops = new HashSet<>();
        
        // Default multipliers (can be overridden from JSON)
        initializeDefaultMultipliers();
    }
    
    private void initializeDefaultMultipliers() {
        rarityMultipliers.put(DropRarity.COMMON, 1.0);
        rarityMultipliers.put(DropRarity.UNCOMMON, 1.0);
        rarityMultipliers.put(DropRarity.RARE, 1.0);
        rarityMultipliers.put(DropRarity.EPIC, 1.0);
        rarityMultipliers.put(DropRarity.LEGENDARY, 1.0);
        rarityMultipliers.put(DropRarity.MYTHIC, 1.0);
    }
    
    public void setRarityMultiplier(DropRarity rarity, double multiplier) {
        rarityMultipliers.put(rarity, multiplier);
    }
    
    public double getRarityMultiplier(DropRarity rarity) {
        return rarityMultipliers.getOrDefault(rarity, 1.0);
    }
    
    public void addExtraDrop(ZoneDropItem drop) {
        extraDrops.add(drop);
    }
    
    public void addGuaranteedDrop(GuaranteedDrop drop) {
        guaranteedDrops.add(drop);
    }
    
    public List<ZoneDropItem> getExtraDrops() {
        return extraDrops;
    }
    
    public List<GuaranteedDrop> getGuaranteedDrops() {
        return guaranteedDrops;
    }
    
    public LootTier getLootTier() {
        return lootTier;
    }
    
    /**
     * Calculate final drop chance with zone modifiers
     */
    public double calculateFinalDropChance(double baseChance, DropRarity rarity, int monsterLevel) {
        double zoneMultiplier = rarityMultipliers.getOrDefault(rarity, 1.0);
        double tierMultiplier = lootTier.getQualityMultiplier();
        
        // Level modifier: higher level monsters = slightly better drops
        double levelModifier = 1.0 + (monsterLevel * 0.05);
        
        return baseChance * zoneMultiplier * levelModifier * tierMultiplier;
    }
    
    /**
     * Check if a guaranteed drop is available for this quest/monster
     */
    public GuaranteedDrop checkGuaranteedDrop(String questId, String monsterType) {
        for (GuaranteedDrop drop : guaranteedDrops) {
            // Match both quest ID AND monster type
            if (drop.questId.equals(questId) && drop.monsterType.equals(monsterType)) {
                String dropKey = drop.questId + ":" + drop.monsterType;
                
                // Check if already claimed
                if (!claimedGuaranteedDrops.contains(dropKey)) {
                    if (drop.dropOnFirstKill) {
                        claimedGuaranteedDrops.add(dropKey);
                        System.out.println("✓ Claimed guaranteed drop: " + dropKey);
                    }
                    return drop;
                } else {
                    System.out.println("✗ Already claimed: " + dropKey);
                }
            }
        }
        return null;
    }
    
    /**
     * Reset guaranteed drops (useful for testing or zone reset)
     */
    public void resetGuaranteedDrops() {
        claimedGuaranteedDrops.clear();
    }
    
    /**
     * Zone-specific drop item with chance
     */
    public static class ZoneDropItem {
        public final DropItem dropItem;
        public final double dropChance;
        
        public ZoneDropItem(DropItem dropItem, double dropChance) {
            this.dropItem = dropItem;
            this.dropChance = dropChance;
        }
    }
    
    /**
     * Guaranteed drop for quest progression
     */
    public static class GuaranteedDrop {
        public final String questId;
        public final String monsterType;  // ← ADD THIS
        public final DropItem dropItem;
        public final int quantity;
        public final boolean dropOnFirstKill;
        
        public GuaranteedDrop(String questId, String monsterType, DropItem dropItem, 
                int quantity, boolean dropOnFirstKill) {
			this.questId = questId;
			this.monsterType = monsterType;  // ← ADD THIS
			this.dropItem = dropItem;
			this.quantity = quantity;
			this.dropOnFirstKill = dropOnFirstKill;
        }
    }
}