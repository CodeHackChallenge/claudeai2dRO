package dev.main.drops;

import dev.main.item.Item;

/**
 * Represents an actual dropped item with quantity
 */
public class DroppedItem {
    private final DropItem dropTemplate;
    private final int quantity;
    
    // ★ NEW FIELDS
    private boolean isGuaranteedQuestDrop;
    private String questId;
    
    public DroppedItem(DropItem dropTemplate, int quantity) {
        this.dropTemplate = dropTemplate;
        this.quantity = quantity;
        this.isGuaranteedQuestDrop = false;  // ★ NEW
        this.questId = null;  // ★ NEW
    }
    
    public DropItem getDropTemplate() {
        return dropTemplate;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public String getItemName() {
        return dropTemplate.getItemName();
    }
    
    public DropRarity getRarity() {
        return dropTemplate.getRarity();
    }
    
    // ★ NEW METHODS - Add these
    /**
     * Mark this drop as a guaranteed quest drop
     */
    public void setGuaranteedQuestDrop(boolean isGuaranteed) {
        this.isGuaranteedQuestDrop = isGuaranteed;
    }
    
    /**
     * Set which quest this guaranteed drop is for
     */
    public void setQuestId(String questId) {
        this.questId = questId;
    }
    
    /**
     * Check if this is a guaranteed quest drop
     */
    public boolean isGuaranteedQuestDrop() {
        return isGuaranteedQuestDrop;
    }
    
    /**
     * Get the quest ID this drop is for (if guaranteed)
     */
    public String getQuestId() {
        return questId;
    }
    // ★ END NEW METHODS
    
    /**
     * Create the actual Item instances for the player's inventory
     */
    public Item[] createItems() {
        Item[] items = new Item[quantity];
        for (int i = 0; i < quantity; i++) {
            items[i] = dropTemplate.createItem();
        }
        return items;
    }
    
    @Override
    public String toString() {
        String base = quantity + "x " + dropTemplate.getItemName() + " [" + dropTemplate.getRarity() + "]";
        // ★ UPDATED: Show if it's a quest drop
        if (isGuaranteedQuestDrop) {
            base += " 🎯 QUEST";
        }
        return base;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DroppedItem other = (DroppedItem) obj;
        return dropTemplate.equals(other.dropTemplate);
    }
    
    @Override
    public int hashCode() {
        return dropTemplate.hashCode();
    }
}