package dev.main;

/**
 * Manages item creation and templates
 * EXPANDED: Items for all tab categories
 */
public class ItemManager {

    // ═══════════════════════════════════════════════════════════════
    // WEAPONS (Weap Tab)
    // ═══════════════════════════════════════════════════════════════
    
    public static Item createWoodenShortSword() {
        return new Item(
            "Wooden Short Sword",
            Item.ItemType.WEAPON,
            Item.Rarity.COMMON,
            6,  // attack bonus
            1,  // defense bonus
            0,  // magic attack bonus
            0,  // magic defense bonus
            999, // durability
            false, // upgradable
            false, // can infuse elemental
            false, // tradable
            false, // sellable
            100  // dismantle threshold
        );
    }
    
    public static Item createIronSword() {
        return new Item(
            "Iron Sword",
            Item.ItemType.WEAPON,
            Item.Rarity.COMMON,
            12,  // attack bonus
            2,   // defense bonus
            0,   // magic attack bonus
            0,   // magic defense bonus
            800, // durability
            true,  // upgradable
            false, // can infuse elemental
            true,  // tradable
            true,  // sellable
            100    // dismantle threshold
        );
    }
    
    public static Item createSteelLongsword() {
        return new Item(
            "Steel Longsword",
            Item.ItemType.WEAPON,
            Item.Rarity.UNCOMMON,
            20,  // attack bonus
            3,   // defense bonus
            0,   // magic attack bonus
            0,   // magic defense bonus
            1000, // durability
            true,  // upgradable
            true,  // can infuse elemental
            true,  // tradable
            true,  // sellable
            150    // dismantle threshold
        );
    }
    
    public static Item createMysticStaff() {
        return new Item(
            "Mystic Staff",
            Item.ItemType.WEAPON,
            Item.Rarity.RARE,
            5,   // attack bonus
            2,   // defense bonus
            25,  // magic attack bonus
            5,   // magic defense bonus
            600, // durability
            true,  // upgradable
            true,  // can infuse elemental
            true,  // tradable
            true,  // sellable
            200    // dismantle threshold
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // ARMOR (Arm Tab)
    // ═══════════════════════════════════════════════════════════════
    
    public static Item createLeatherArmor() {
        return new Item(
            "Leather Armor",
            Item.ItemType.ARMOR,
            Item.Rarity.COMMON,
            0,   // attack bonus
            8,   // defense bonus
            0,   // magic attack bonus
            2,   // magic defense bonus
            500, // durability
            true,  // upgradable
            false, // can infuse elemental
            true,  // tradable
            true,  // sellable
            100    // dismantle threshold
        );
    }
    
    public static Item createChainmail() {
        return new Item(
            "Chainmail",
            Item.ItemType.ARMOR,
            Item.Rarity.UNCOMMON,
            0,   // attack bonus
            15,  // defense bonus
            0,   // magic attack bonus
            3,   // magic defense bonus
            800, // durability
            true,  // upgradable
            false, // can infuse elemental
            true,  // tradable
            true,  // sellable
            150    // dismantle threshold
        );
    }
    
    public static Item createPlateArmor() {
        return new Item(
            "Plate Armor",
            Item.ItemType.ARMOR,
            Item.Rarity.RARE,
            0,   // attack bonus
            25,  // defense bonus
            0,   // magic attack bonus
            5,   // magic defense bonus
            1200, // durability
            true,  // upgradable
            true,  // can infuse elemental
            true,  // tradable
            true,  // sellable
            200    // dismantle threshold
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // ACCESSORIES (Acc Tab)
    // ═══════════════════════════════════════════════════════════════
    
    public static Item createRuneOfReturn() {
        return new Item(
            "Rune of Return",
            Item.ItemType.ACCESSORY,
            Item.Rarity.RARE,
            0,  // attack bonus
            2,  // defense bonus
            1,  // magic attack bonus
            1,  // magic defense bonus
            999, // durability
            false, // upgradable
            false, // can infuse elemental
            false, // tradable
            false, // sellable
            100  // dismantle threshold
        );
    }
    
    public static Item createPowerRing() {
        return new Item(
            "Ring of Power",
            Item.ItemType.ACCESSORY,
            Item.Rarity.UNCOMMON,
            5,   // attack bonus
            0,   // defense bonus
            3,   // magic attack bonus
            0,   // magic defense bonus
            999, // durability
            false, // upgradable
            false, // can infuse elemental
            true,  // tradable
            true,  // sellable
            100    // dismantle threshold
        );
    }
    
    public static Item createAmuletOfProtection() {
        return new Item(
            "Amulet of Protection",
            Item.ItemType.ACCESSORY,
            Item.Rarity.RARE,
            0,   // attack bonus
            10,  // defense bonus
            0,   // magic attack bonus
            8,   // magic defense bonus
            999, // durability
            false, // upgradable
            false, // can infuse elemental
            true,  // tradable
            true,  // sellable
            150    // dismantle threshold
        );
    }
    
    public static Item createSpeedBoots() {
        return new Item(
            "Boots of Speed",
            Item.ItemType.ACCESSORY,
            Item.Rarity.UNCOMMON,
            0,   // attack bonus
            3,   // defense bonus
            0,   // magic attack bonus
            0,   // magic defense bonus
            600, // durability
            false, // upgradable
            false, // can infuse elemental
            true,  // tradable
            true,  // sellable
            100    // dismantle threshold
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // MATERIALS & RUNES (Rune Tab)
    // ═══════════════════════════════════════════════════════════════
    
    public static Item createCarvedWood() {
        return new Item(
            "Carved Wood",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0,   // attack bonus
            0,   // defense bonus
            0,   // magic attack bonus
            0,   // magic defense bonus
            999, // durability
            false, // upgradable
            false, // can infuse elemental
            true,  // tradable
            true,  // sellable
            0      // dismantle threshold
        );
    }
    
    public static Item createClay() {
        return new Item(
            "Clay",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0,   // attack bonus
            0,   // defense bonus
            0,   // magic attack bonus
            0,   // magic defense bonus
            999, // durability
            false, // upgradable
            false, // can infuse elemental
            true,  // tradable
            true,  // sellable
            0      // dismantle threshold
        );
    }
    
    public static Item createCarvingStone() {
        return new Item(
            "Carving Stone",
            Item.ItemType.MATERIAL,
            Item.Rarity.COMMON,
            0,   // attack bonus
            0,   // defense bonus
            0,   // magic attack bonus
            0,   // magic defense bonus
            999, // durability
            false, // upgradable
            false, // can infuse elemental
            true,  // tradable
            true,  // sellable
            0      // dismantle threshold
        );
    }
    
    public static Item createFireRune() {
        return new Item(
            "Fire Rune",
            Item.ItemType.MATERIAL,
            Item.Rarity.UNCOMMON,
            0,   // attack bonus
            0,   // defense bonus
            0,   // magic attack bonus
            0,   // magic defense bonus
            999, // durability
            false, // upgradable
            false, // can infuse elemental
            true,  // tradable
            true,  // sellable
            0      // dismantle threshold
        );
    }
    
    public static Item createIceRune() {
        return new Item(
            "Ice Rune",
            Item.ItemType.MATERIAL,
            Item.Rarity.UNCOMMON,
            0,   // attack bonus
            0,   // defense bonus
            0,   // magic attack bonus
            0,   // magic defense bonus
            999, // durability
            false, // upgradable
            false, // can infuse elemental
            true,  // tradable
            true,  // sellable
            0      // dismantle threshold
        );
    }
    
    public static Item createLightningRune() {
        return new Item(
            "Lightning Rune",
            Item.ItemType.MATERIAL,
            Item.Rarity.RARE,
            0,   // attack bonus
            0,   // defense bonus
            0,   // magic attack bonus
            0,   // magic defense bonus
            999, // durability
            false, // upgradable
            false, // can infuse elemental
            true,  // tradable
            true,  // sellable
            0      // dismantle threshold
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // CONSUMABLES (Misc Tab)
    // ═══════════════════════════════════════════════════════════════
    
    public static Item createHealthPotion() {
        return new Item(
            "Health Potion",
            Item.ItemType.CONSUMABLE,
            Item.Rarity.COMMON,
            0,   // attack bonus
            0,   // defense bonus
            0,   // magic attack bonus
            0,   // magic defense bonus
            1,   // durability (single use)
            false, // upgradable
            false, // can infuse elemental
            true,  // tradable
            true,  // sellable
            0      // dismantle threshold
        );
    }
    
    public static Item createManaPotion() {
        return new Item(
            "Mana Potion",
            Item.ItemType.CONSUMABLE,
            Item.Rarity.COMMON,
            0,   // attack bonus
            0,   // defense bonus
            0,   // magic attack bonus
            0,   // magic defense bonus
            1,   // durability (single use)
            false, // upgradable
            false, // can infuse elemental
            true,  // tradable
            true,  // sellable
            0      // dismantle threshold
        );
    }
    
    public static Item createStaminaPotion() {
        return new Item(
            "Stamina Potion",
            Item.ItemType.CONSUMABLE,
            Item.Rarity.COMMON,
            0,   // attack bonus
            0,   // defense bonus
            0,   // magic attack bonus
            0,   // magic defense bonus
            1,   // durability (single use)
            false, // upgradable
            false, // can infuse elemental
            true,  // tradable
            true,  // sellable
            0      // dismantle threshold
        );
    }
}