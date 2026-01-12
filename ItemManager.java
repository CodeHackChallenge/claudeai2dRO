package dev.main;

/**
 * Manages item creation and templates
 */
public class ItemManager {

    // Predefined item templates
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

    // Future: Add more item creation methods
    // public static Item createIronSword() { ... }
    // public static Item createLeatherArmor() { ... }
}