package dev.main.ui;

import java.awt.Graphics2D;

import dev.main.item.Item;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

/**
 * UI component representing a gear/equipment slot
 */
public class UIGearSlot extends UIComponent {
	 // ⭐ ADD THIS at the top of the class:
    private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 9);
   
    public enum SlotType {
        HEAD,
        TOP_ARMOR,
        PANTS,
        GLOVES, 
        SHOES,
        WEAPON,
        EARRINGS,
        NECKLACE,
        BRACELET,
        RING_1,
        RING_2,
        SPECIAL
    }
    
    private SlotType slotType;
    private Item item;
    
    // Reference to UIManager for unequipping
    private UIManager uiManager;
    
    // Visual properties
    private Color emptyColor;
    private Color hoverColor;
    private Color fillColor;
    
    public UIGearSlot(int x, int y, int width, int height, SlotType slotType, UIManager uiManager) {
        super(x, y, width, height);
        
        this.slotType = slotType;
        this.uiManager = uiManager;
        this.item = null;
        
        // Colors
        this.emptyColor = new Color(60, 60, 60, 180);
        this.hoverColor = new Color(100, 100, 100, 200);
        this.fillColor = new Color(80, 100, 120, 200);
    }
    
    @Override
    public void render(Graphics2D g) {
        if (!visible) return;
        
        // Choose background color
        Color bgColor;
        if (item != null) {
            bgColor = hovered ? fillColor.brighter() : fillColor;
        } else {
            bgColor = hovered ? hoverColor : emptyColor;
        }
        
        // Draw slot background
        g.setColor(bgColor);
        g.fillRect(x, y, width, height);
        
        // Draw border
        g.setColor(hovered ? Color.WHITE : new Color(100, 100, 100));
        g.setStroke(new java.awt.BasicStroke(1f));
        g.drawRect(x, y, width, height);
        
        // Draw slot label (only if empty or on hover)
        if (item == null || hovered) {
            drawSlotLabel(g);
        }
        
        // If not empty, draw item
        if (item != null) {
            drawPlaceholderItem(g);
        }
        
        // Draw hover effect
        if (hovered) {
            g.setColor(new Color(255, 255, 255, 30));
            g.fillRect(x, y, width, height);
        }
    }
    
    /**
     * Draw slot type label
     */
    // ⭐ UPDATE drawSlotLabel() to use cached font:
    private void drawSlotLabel(Graphics2D g) {
        Font originalFont = g.getFont();
        g.setFont(LABEL_FONT);  // ⭐ Use cached font (was: new Font(...))
        
        String label = getSlotLabel();
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(label);
        int textHeight = fm.getHeight();
        
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height + textHeight / 2) / 2 - 2;
        
        // Shadow
        g.setColor(new Color(0, 0, 0, 150));
        g.drawString(label, textX + 1, textY + 1);
        
        // Text
        g.setColor(new Color(200, 200, 200));
        g.drawString(label, textX, textY);
        
        g.setFont(originalFont);
    }
    
    /**
     * Get display label for slot type
     */
    private String getSlotLabel() {
        switch (slotType) {
            case HEAD: return "Head";
            case TOP_ARMOR: return "Armor";
            case PANTS: return "Pants";
            case GLOVES: return "Gloves";
            case SHOES: return "Shoes";
            case WEAPON: return "Weapon"; 
            case EARRINGS: return "Earring";
            case NECKLACE: return "Neck";
            case BRACELET: return "Bracelet";
            case RING_1: return "Ring";
            case RING_2: return "Ring";
            case SPECIAL: return "Special";
            default: return "???";
        }
    }
    
    /**
     * Draw placeholder for equipped item
     */
    private void drawPlaceholderItem(Graphics2D g) {
        // Draw colored square as placeholder
        Color itemColor = getSlotColor();
        g.setColor(itemColor);
        
        int itemSize = (int)(Math.min(width, height) * 0.7f);
        int itemX = x + (width - itemSize) / 2;
        int itemY = y + (height - itemSize) / 2;
        g.fillRect(itemX, itemY, itemSize, itemSize);
        
        // Draw border
        g.setColor(itemColor.brighter());
        g.drawRect(itemX, itemY, itemSize, itemSize);
    }
    
    /**
     * Get color based on slot type
     */
    private Color getSlotColor() {
        switch (slotType) {
            case HEAD:
            case TOP_ARMOR:
            case PANTS:
            case GLOVES:
            case SHOES:
                return new Color(120, 80, 60);  // Brown for armor
            
            case WEAPON:
                return new Color(100, 100, 80);  // Tan for accessories
            case EARRINGS:
            	 return new Color(150, 150, 180);  // Silver for earrings                 
            case NECKLACE:
            case BRACELET:
            case RING_1:
            case RING_2: 
            case SPECIAL: 
            default:
                return new Color(100, 100, 100);
        }
    }
    
    @Override
    public void update(float delta) {
        // Gear slots don't need per-frame updates
    }
    
    @Override
    public boolean onClick() {
        if (item == null) {
            System.out.println("Clicked empty gear slot: " + slotType);
        } else {
            System.out.println("Clicked gear slot: " + slotType + " with item");
            // TODO: Handle item click (unequip, view stats, etc.)
        }
        return true;  // Consume click
    }
    
    @Override
    public boolean onRightClick() {
        if (item != null) {
            System.out.println("Right-clicked gear slot: " + slotType + " - unequipping");
            uiManager.unequipItem(slotType);
        }
        return true;  // Consume click
    }
    
    // Item management
    public Item equipItem(Item item) {
        Item oldItem = this.item;
        this.item = item;
        return oldItem;
    }
    
    public Item unequipItem() {
        Item oldItem = this.item;
        this.item = null;
        return oldItem;
    }
    
    public Item getItem() {
        return item;
    }
    
    public boolean isEmpty() {
        return item == null;
    }
    
    public SlotType getSlotType() {
        return slotType;
    }
    
    @Override
    public String getTooltipText() {
        if (item != null) {
            return getItemDescription(item);
        }
        return null;
    }
    
    private String getItemDescription(Item item) {
        StringBuilder sb = new StringBuilder();
        sb.append(item.getName()).append("\n");
        sb.append("Type: ").append(item.getType()).append("\n");
        sb.append("Rarity: ").append(item.getRarity()).append("\n");
        
        if (item.getAttackBonus() > 0) {
            sb.append("Attack: +").append(item.getAttackBonus()).append("\n");
        }
        if (item.getDefenseBonus() > 0) {
            sb.append("Defense: +").append(item.getDefenseBonus()).append("\n");
        }
        if (item.getMagicAttackBonus() > 0) {
            sb.append("Magic Attack: +").append(item.getMagicAttackBonus()).append("\n");
        }
        if (item.getMagicDefenseBonus() > 0) {
            sb.append("Magic Defense: +").append(item.getMagicDefenseBonus()).append("\n");
        }
        
        sb.append("Durability: ").append(item.getCurrentDurability()).append("/").append(item.getMaxDurability()).append("\n");
        
        if (!item.isUpgradable()) {
            sb.append("Not upgradable\n");
        }
        if (!item.canInfuseElemental()) {
            sb.append("Cannot infuse elemental stones\n");
        }
        if (!item.isTradable()) {
            sb.append("Cannot be traded\n");
        }
        if (!item.isSellable()) {
            sb.append("Cannot be sold\n");
        }
        
        return sb.toString().trim();
    }
}