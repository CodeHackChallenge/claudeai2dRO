package dev.main;

import java.awt.Graphics2D;
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
        GLOVES,
        BELT,
        PANTS,
        SHOES,
        TIARA,
        EARRINGS,
        NECKLACE,
        BRACELET,
        RING_1,
        RING_2
    }
    
    private SlotType slotType;
    private Object item;  // TODO: Replace with actual Item class later
    private boolean isEmpty;
    
    // Visual properties
    private Color emptyColor;
    private Color hoverColor;
    private Color fillColor;
    
    public UIGearSlot(int x, int y, int width, int height, SlotType slotType) {
        super(x, y, width, height);
        
        this.slotType = slotType;
        this.item = null;
        this.isEmpty = true;
        
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
        if (!isEmpty) {
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
        if (isEmpty || hovered) {
            drawSlotLabel(g);
        }
        
        // If not empty, draw item
        if (!isEmpty) {
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
            case GLOVES: return "Gloves";
            case BELT: return "Belt";
            case PANTS: return "Pants";
            case SHOES: return "Shoes";
            case TIARA: return "Tiara";
            case EARRINGS: return "Earring";
            case NECKLACE: return "Neck";
            case BRACELET: return "Bracelet";
            case RING_1: return "Ring";
            case RING_2: return "Ring";
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
            case SHOES:
                return new Color(120, 80, 60);  // Brown for armor
            case GLOVES:
            case BELT:
                return new Color(100, 100, 80);  // Tan for accessories
            case TIARA:
            case NECKLACE:
            case BRACELET:
            case RING_1:
            case RING_2:
                return new Color(180, 150, 50);  // Gold for jewelry
            case EARRINGS:
                return new Color(150, 150, 180);  // Silver for earrings
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
        if (isEmpty) {
            System.out.println("Clicked empty gear slot: " + slotType);
        } else {
            System.out.println("Clicked gear slot: " + slotType + " with item");
            // TODO: Handle item click (unequip, view stats, etc.)
        }
        return true;  // Consume click
    }
    
    @Override
    public boolean onRightClick() {
        if (!isEmpty) {
            System.out.println("Right-clicked gear slot: " + slotType + " - unequipping");
            unequipItem();
        }
        return true;  // Consume click
    }
    
    // Item management
    public void equipItem(Object item) {
        this.item = item;
        this.isEmpty = false;
    }
    
    public void unequipItem() {
        this.item = null;
        this.isEmpty = true;
    }
    
    public Object getItem() {
        return item;
    }
    
    public boolean isEmpty() {
        return isEmpty;
    }
    
    public SlotType getSlotType() {
        return slotType;
    }
}