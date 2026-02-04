package dev.main.item;

/**
 * Manages item creation and templates
 * UPDATED: Runes now use RuneManager instead
 */
public class ItemManager {

    // ═══════════════════════════════════════════════════════════════
    // WEAPONS (Weap Tab)
    // ═══════════════════════════════════════════════════════════════
    
    public static Item createWoodenShortSword() {
        return new Item(
            "Wooden Short Sword",
            "A reliable weapon for beginners",
            Item.ItemType.WEAPON,
            Item.Rarity.COMMON,
            6, 1, 0, 0, 999,
            false, false, false, false, 100
        );
    }
    
    public static Item createIronSword() {
        return new Item(
            "Iron Sword",
            null,
            Item.ItemType.WEAPON,
            Item.Rarity.COMMON,
            12, 2, 0, 0, 800,
            true, false, true, true, 100
        );
    }
    
    public static Item createSteelLongsword() {
        return new Item(
            "Steel Longsword",
            null,
            Item.ItemType.WEAPON,
            Item.Rarity.UNCOMMON,
            20, 3, 0, 0, 1000,
            true, true, true, true, 150
        );
    }
    
    public static Item createMysticStaff() {
        return new Item(
            "Mystic Staff",
            null,
            Item.ItemType.WEAPON,
            Item.Rarity.RARE,
            5, 2, 25, 5, 600,
            true, true, true, true, 200
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // ARMOR (Arm Tab)
    // ═══════════════════════════════════════════════════════════════
    
    public static Item createLeatherArmor() {
        return new Item(
            "Leather Armor",
            null,
            Item.ItemType.ARMOR,
            Item.Rarity.COMMON,
            0, 8, 0, 2, 500,
            true, false, true, true, 100
        );
    }
    
    public static Item createChainmail() {
        return new Item(
            "Chainmail",
            null,
            Item.ItemType.ARMOR,
            Item.Rarity.UNCOMMON,
            0, 15, 0, 3, 800,
            true, false, true, true, 150
        );
    }
    
    public static Item createPlateArmor() {
        return new Item(
            "Plate Armor",
            null,
            Item.ItemType.ARMOR,
            Item.Rarity.RARE,
            0, 25, 0, 5, 1200,
            true, true, true, true, 200
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // ACCESSORIES (Acc Tab)
    // ═══════════════════════════════════════════════════════════════
    
    public static Item createPowerRing() {
        return new Item(
            "Ring of Power",
            null,
            Item.ItemType.ACCESSORY,
            Item.Rarity.UNCOMMON,
            5, 0, 3, 0, 999,
            false, false, true, true, 100
        );
    }
    
    public static Item createAmuletOfProtection() {
        return new Item(
            "Amulet of Protection",
            null,
            Item.ItemType.ACCESSORY,
            Item.Rarity.RARE,
            0, 10, 0, 8, 999,
            false, false, true, true, 150
        );
    }
    
    public static Item createSpeedBoots() {
        return new Item(
            "Boots of Speed",
            null,
            Item.ItemType.ACCESSORY,
            Item.Rarity.UNCOMMON,
            0, 3, 0, 0, 600,
            false, false, true, true, 100
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // MATERIALS (Rune Tab)
    // ═══════════════════════════════════════════════════════════════
    
    public static Item createCarvedWood() {
        return new Item(
            "Carved Wood",
            "Use for rune crafting",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
     
    public static Item createClay() {
        return new Item(
            "Clay",
            "Use for rune crafting",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    
    public static Item createBrokenTooth() {
        return new Item(
            "Broken Tooth",
            "Use for crafting gears",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    
    public static Item createAnimalClaws() {
        return new Item(
            "Animal Claws",
            "Use for crafting gears",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    
    public static Item createMpLargeBottle() {
        return new Item(
            "MP Large Bottle",
            "Use for making potions",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    
    public static Item createMpMediumBottle() {
        return new Item(
            "Medium Large Bottle",
            "Use for making potions",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    public static Item createMpSmallVial() {
        return new Item(
            "MP Small Vial",
            "Use for making potions",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    public static Item createHpSmallVial() {
        return new Item(
            "HP Small Vial",
            "Use for making potions",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    public static Item createHpMediumBottle() {
        return new Item(
            "HP Medium Bottle",
            "Use for making potions",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    public static Item createHpLargeBottle() {
        return new Item(
            "HP Large Bottle",
            "Use for making potions",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    public static Item createStaminaLargeBottle() {
        return new Item(
            "Stamina Large Bottle",
            "Use for making potions",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    public static Item createStaminaMediumBottle() {
        return new Item(
            "Stamina Medium Bottle",
            "Use for making potions",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    public static Item createStaminaSmallVial() {
        return new Item(
            "Stamina Small Vial Banana",
            "Use for making potions",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    public static Item createCureSmallVial() {
        return new Item(
            "Cure Small Vial",
            "Use for making potions",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    public static Item createFruitBanana() {
        return new Item(
            "Fruit Banana",
            "Use for making potions",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    public static Item createFruitDark() {
        return new Item(
            "Fruit Dark",
            "Use for making potions",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    public static Item createFruitGrapes() {
        return new Item(
            "Fruit Grapes",
            "Use for making potions",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    public static Item createFruitLime() {
        return new Item(
            "Fruit Lime",
            "Use for making potions",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    public static Item createFruitTomatoe() {
        return new Item(
            "Fruit Tomatoe",
            "Use for making potions",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    public static Item createFruitWaterMelon() {
        return new Item(
            "Fruit Watermelon",
            "Use for making potions",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    public static Item createAnimalSkull() {
        return new Item(
            "Animal Skull",
            "Use for crafting gears",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    
    public static Item createRawFish() {
        return new Item(
            "Raw Fish",
            "Use for cooking delicious food!",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    
    public static Item createAnimalBone() {
        return new Item(
            "Animal Bone",
            "Use for crafting gears",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    
    public static Item createCarvingStone() {
        return new Item(
            "Carving Stone",
            "Use for rune crafting",
            Item.ItemType.MATERIAL,
            Item.Rarity.UNCOMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    
    //lucky drops
    public static Item createLuckyPouch() {
        return new Item(
            "Lucky Pouch",
            "Sell to market for aurels",
            Item.ItemType.MATERIAL,
            Item.Rarity.UNCOMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    
    // ★ NEW: Crafting materials for runes
    public static Item createWoodenTablet() {
        return new Item(
            "Wooden Tablet",
            "Use for rune crafting",
            Item.ItemType.MATERIAL,
            Item.Rarity.UNCOMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    
    public static Item createEssence() {
        return new Item(
            "Essence",
            "Use for rune crafting",
            Item.ItemType.MATERIAL,
            Item.Rarity.UNCOMMON,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    
    public static Item createVerdantShard() {
        return new Item(
            "Verdant Shard",
            "Use for rune crafting",
            Item.ItemType.MATERIAL,
            Item.Rarity.RARE,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    
    public static Item createScrollOfPurity() {
        return new Item(
            "Scroll of Purity",
            "Use for crafting divine gears",
            Item.ItemType.MATERIAL,
            Item.Rarity.EPIC,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    
    // Elemental runes (materials for crafting)
    public static Item createFireRune() {
        return new Item(
            "Fire Rune",
            null,
            Item.ItemType.MATERIAL,
            Item.Rarity.EPIC,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    
    public static Item createIceRune() {
        return new Item(
            "Ice Rune",
            null,
            Item.ItemType.MATERIAL,
            Item.Rarity.EPIC,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }
    
    public static Item createLightningRune() {
        return new Item(
            "Lightning Rune",
            null,
            Item.ItemType.MATERIAL,
            Item.Rarity.EPIC,
            0, 0, 0, 0, 999,
            false, false, true, true, 0
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // CONSUMABLES (Misc Tab)
    // ═══════════════════════════════════════════════════════════════
    
    public static Item createHealthPotion() {
        return new Item(
            "Health Potion",
            "Restores HP",
            Item.ItemType.CONSUMABLE,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 1,
            false, false, true, true, 0
        );
    }
    
    public static Item createManaPotion() {
        return new Item(
            "Mana Potion",
            "Restores MP",
            Item.ItemType.CONSUMABLE,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 1,
            false, false, true, true, 0
        );
    }
    
    public static Item createStaminaPotion() {
        return new Item(
            "Stamina Potion",
            "Restores Stamina",
            Item.ItemType.CONSUMABLE,
            Item.Rarity.COMMON,
            0, 0, 0, 0, 1,
            false, false, true, true, 0
        );
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ★ NEW: RUNES (Magical Consumables)
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * ★ REFACTORED: Create Rune of Return using RuneManager
     * @deprecated Use RuneManager.createRuneItem(RuneType.RETURN) instead
     */
    @Deprecated
    public static Item createRuneOfReturn() {
        return RuneManager.createRuneItem(Rune.RuneType.RETURN);
    }
    
    /**
     * ★ NEW: Create Rune of Spawn using RuneManager
     */
    public static Item createRuneOfSpawn() {
        return RuneManager.createRuneItem(Rune.RuneType.SPAWN);
    }
    
    /**
     * ★ NEW: Create any rune by type
     */
    public static Item createRuneItem(Rune.RuneType type) {
        return RuneManager.createRuneItem(type);
    }
}