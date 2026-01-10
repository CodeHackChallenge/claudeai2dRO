package dev.main;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

/**
 * UI component representing an inventory slot
 * Similar visual style to menu buttons but for holding items
 */
public class UIInventorySlot extends UIComponent {
    private static final Font SLOT_FONT = new Font("Arial", Font.PLAIN, 10);
   
    private Object item;  // TODO: Replace with actual Item class later
    private int slotIndex;
    private boolean isEmpty;
    
    // Visual properties
    private Color emptyColor;
    private Color hoverColor;
    private Color fillColor;
    
    public UIInventorySlot(int x, int y, int size, int slotIndex) {
        super(x, y, size, size);
        
        this.slotIndex = slotIndex;
        this.item = null;
        this.isEmpty = true;
        
        // Colors similar to locked menu buttons
        this.emptyColor = new Color(60, 60, 60, 180);
        this.hoverColor = new Color(100, 100, 100, 200);
        this.fillColor = new Color(80, 80, 120, 200);
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
        
        // If empty, draw empty slot indicator (grid pattern)
        if (isEmpty) {
            drawEmptySlotPattern(g);
        } else {
            // TODO: Draw item icon here when item system is implemented
            drawPlaceholderItem(g);
        }
        
        // Draw hover effect
        if (hovered) {
            g.setColor(new Color(255, 255, 255, 30));
            g.fillRect(x, y, width, height);
        }
    }
    
    /**
     * Draw empty slot pattern (subtle grid/cross)
     */
    private void drawEmptySlotPattern(Graphics2D g) {
        g.setColor(new Color(80, 80, 80, 100));
        
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        int size = width / 3;
        
        // Draw small cross pattern
        g.fillRect(centerX - 1, centerY - size / 2, 2, size);
        g.fillRect(centerX - size / 2, centerY - 1, size, 2);
    }
    
    /**
     * Draw placeholder for item (for testing)
     */
    private void drawPlaceholderItem(Graphics2D g) {
        // Draw colored square as placeholder
        g.setColor(new Color(150, 100, 50));
        int itemSize = (int)(width * 0.7f);
        int itemX = x + (width - itemSize) / 2;
        int itemY = y + (height - itemSize) / 2;
        g.fillRect(itemX, itemY, itemSize, itemSize);
        
        // Draw border
        g.setColor(new Color(200, 150, 100));
        g.drawRect(itemX, itemY, itemSize, itemSize);
    }
    
    @Override
    public void update(float delta) {
        // Inventory slots don't need per-frame updates
    }
    
    @Override
    public boolean onClick() {
        if (isEmpty) {
            System.out.println("Clicked empty inventory slot " + slotIndex);
        } else {
            System.out.println("Clicked inventory slot " + slotIndex + " with item");
            // TODO: Handle item click (use, move, etc.)
        }
        return true;  // Consume click
    }
    
    @Override
    public boolean onRightClick() {
        if (!isEmpty) {
            System.out.println("Right-clicked inventory slot " + slotIndex + " - removing item");
            // TODO: Handle right-click (drop, delete, context menu)
            removeItem();  // For testing
        }
        return true;  // Consume click
    }
    
    // Item management methods
    public void setItem(Object item) {
        this.item = item;
        this.isEmpty = false;
    }
    
    public void removeItem() {
        this.item = null;
        this.isEmpty = true;
    }
    
    public Object getItem() {
        return item;
    }
    
    public boolean isEmpty() {
        return isEmpty;
    }
    
    public int getSlotIndex() {
        return slotIndex;
    }
}