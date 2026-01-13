package dev.main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * REFACTORED: All tabs share same inventory slots
 * Tabs filter items by category (Misc shows all)
 */
public class UIScrollableInventoryPanel extends UIComponent {
    
    private List<UIInventorySlot> slots;
    private int columns;
    private int totalRows;
    private int visibleRows;
    private int slotSize;
    private int gap;
    private int padding;
    
    // Scrolling
    private int scrollOffsetY;
    private int maxScrollY;
    private float scrollbarAlpha;
    private float scrollbarFadeTimer;
    private boolean showScrollbar;
    
    // Visual
    private Color backgroundColor;
    private Color borderColor;
    private Color scrollbarColor;
    private Color scrollbarThumbColor;
    
    // Scrollbar dimensions
    private int scrollbarWidth;
    private int scrollbarX;
    private int scrollbarY;
    private int scrollbarHeight;
    private int thumbY;
    private int thumbHeight;
    
    // Scrollbar interaction
    private boolean mouseOverScrollbar;
    private boolean draggingThumb = false;
    private int dragOffset = 0;
    
    // ★ NEW: Single shared inventory storage
    private Item[] sharedInventory;  // All items stored here
    private String currentTab;
    
    // Reference to UIManager
    private UIManager uiManager;
    
    // Cached visible range
    private int cachedFirstVisibleRow = -1;
    private int cachedLastVisibleRow = -1;
    private int lastScrollOffsetY = -1;
    
    public UIScrollableInventoryPanel(int x, int y, int width, int height, 
                                      int columns, int totalRows, int visibleRows, UIManager uiManager) {
        super(x, y, width, height);
        
        this.uiManager = uiManager;
        
        this.columns = columns;
        this.totalRows = totalRows;
        this.visibleRows = visibleRows;
        this.gap = 4;
        this.padding = 8;
        
        this.slots = new ArrayList<>();
        this.scrollOffsetY = 0;
        this.maxScrollY = 0;
        this.scrollbarAlpha = 0f;
        this.scrollbarFadeTimer = 0f;
        this.showScrollbar = totalRows > visibleRows;
        this.mouseOverScrollbar = false;
        
        // Colors
        this.backgroundColor = new Color(20, 20, 30, 230);
        this.borderColor = new Color(100, 100, 120);
        this.scrollbarColor = new Color(40, 40, 50, 200);
        this.scrollbarThumbColor = new Color(120, 120, 140, 255);
        
        // Calculate slot size
        int availableWidth = width - (padding * 2) - (gap * (columns - 1));
        if (showScrollbar) {
            availableWidth -= 10;
        }
        this.slotSize = availableWidth / columns;
        
        // Scrollbar setup
        this.scrollbarWidth = 8;
        this.scrollbarX = x + width - scrollbarWidth - 2;
        this.scrollbarY = y + padding;
        this.scrollbarHeight = height - (padding * 2);
        
        // ★ NEW: Single shared inventory (5x10 = 50 slots)
        this.sharedInventory = new Item[getTotalSlots()];
        this.currentTab = "Misc";
        
        // Create slots
        createSlots();
        calculateScrollLimits();
        refreshSlotDisplay();
    }
    
    private void createSlots() {
        slots.clear();
        
        int slotIndex = 0;
        for (int row = 0; row < totalRows; row++) {
            for (int col = 0; col < columns; col++) {
                int slotX = x + padding + (col * (slotSize + gap));
                int slotY = y + padding + (row * (slotSize + gap));
                
                UIInventorySlot slot = new UIInventorySlot(slotX, slotY, slotSize, slotIndex, uiManager);
                slots.add(slot);
                slotIndex++;
            }
        }
    }
    
    private void calculateScrollLimits() {
        int contentHeight = (slotSize * totalRows) + (gap * (totalRows - 1));
        int viewportHeight = (slotSize * visibleRows) + (gap * (visibleRows - 1));
        maxScrollY = Math.max(0, contentHeight - viewportHeight);
    }
    
    private void updateVisibleRange() {
        if (scrollOffsetY == lastScrollOffsetY) {
            return;
        }
        
        int rowHeight = slotSize + gap;
        cachedFirstVisibleRow = Math.max(0, scrollOffsetY / rowHeight);
        cachedLastVisibleRow = Math.min(totalRows - 1, 
            (scrollOffsetY + height - padding * 2) / rowHeight + 1);
        
        lastScrollOffsetY = scrollOffsetY;
    }
    
    public void handleScroll(int wheelRotation) {
        if (!showScrollbar) return;
        
        int scrollAmount = wheelRotation * (slotSize + gap);
        scrollOffsetY = Math.max(0, Math.min(maxScrollY, scrollOffsetY + scrollAmount));
        
        scrollbarAlpha = 1.0f;
        scrollbarFadeTimer = 1.5f;
        
        updateSlotPositions();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ★ NEW: TAB FILTERING SYSTEM
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Switch to a tab (filters display but shares same inventory)
     */
    public void switchToTab(String tabName) {
        this.currentTab = tabName;
        scrollOffsetY = 0;  // Reset scroll to top
        refreshSlotDisplay();
    }
    
    /**
     * Refresh slot display based on current tab filter
     */
    private void refreshSlotDisplay() {
        // Get filtered items for current tab
        List<Item> filteredItems = getFilteredItems(currentTab);
        
        // Clear all slots first
        for (UIInventorySlot slot : slots) {
            slot.removeItem();
            slot.setVisible(false);
        }
        
        // Populate visible slots with filtered items
        for (int i = 0; i < filteredItems.size() && i < slots.size(); i++) {
            UIInventorySlot slot = slots.get(i);
            slot.setItem(filteredItems.get(i));
            slot.setVisible(true);
        }
        
        // Update scroll limits based on filtered item count
        updateScrollLimitsForFilteredItems(filteredItems.size());
    }
    
    /**
     * Get items filtered by tab category
     */
    private List<Item> getFilteredItems(String tabName) {
        List<Item> filtered = new ArrayList<>();
        
        for (Item item : sharedInventory) {
            if (item == null) continue;
            
            // "Misc" shows everything
            if (tabName.equals("Misc")) {
                filtered.add(item);
                continue;
            }
            
            // Filter by category
            if (matchesTabFilter(item, tabName)) {
                filtered.add(item);
            }
        }
        
        return filtered;
    }
    
    /**
     * Check if item matches tab filter
     */
    private boolean matchesTabFilter(Item item, String tabName) {
        Item.ItemType type = item.getType();
        
        switch (tabName) {
            case "Weap":
                return type == Item.ItemType.WEAPON;
                
            case "Arm":
                return type == Item.ItemType.ARMOR;
                
            case "Acc":
                return type == Item.ItemType.ACCESSORY;
                
            case "Rune":
                return type == Item.ItemType.MATERIAL && 
                       item.getName().toLowerCase().contains("rune");
                
            default:
                return false;
        }
    }
    
    /**
     * Update scroll limits based on filtered item count
     */
    private void updateScrollLimitsForFilteredItems(int itemCount) {
        int requiredRows = (int) Math.ceil((double) itemCount / columns);
        int contentHeight = (slotSize * requiredRows) + (gap * (requiredRows - 1));
        int viewportHeight = (slotSize * visibleRows) + (gap * (visibleRows - 1));
        maxScrollY = Math.max(0, contentHeight - viewportHeight);
        
        // Clamp scroll offset
        scrollOffsetY = Math.min(scrollOffsetY, maxScrollY);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ★ NEW: INVENTORY MANAGEMENT (Shared Storage)
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Add item to shared inventory (finds first empty slot)
     */
    public boolean addItemToCurrentTab(Item item) {
        // Find first empty slot in shared inventory
        for (int i = 0; i < sharedInventory.length; i++) {
            if (sharedInventory[i] == null) {
                sharedInventory[i] = item;
                refreshSlotDisplay();
                return true;
            }
        }
        return false;  // Inventory full
    }
    
    /**
     * Remove item from specific slot index
     */
    public boolean removeItemFromSlot(int slotIndex) {
        // Get the actual item from filtered display
        List<Item> filteredItems = getFilteredItems(currentTab);
        
        if (slotIndex < 0 || slotIndex >= filteredItems.size()) {
            return false;
        }
        
        Item itemToRemove = filteredItems.get(slotIndex);
        
        // Find and remove from shared inventory
        for (int i = 0; i < sharedInventory.length; i++) {
            if (sharedInventory[i] == itemToRemove) {
                sharedInventory[i] = null;
                refreshSlotDisplay();
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get item at filtered slot index
     */
    public Item getItemAtSlot(int slotIndex) {
        List<Item> filteredItems = getFilteredItems(currentTab);
        if (slotIndex >= 0 && slotIndex < filteredItems.size()) {
            return filteredItems.get(slotIndex);
        }
        return null;
    }
    
    /**
     * Get total item count (all items, not filtered)
     */
    public int getTotalItemCount() {
        int count = 0;
        for (Item item : sharedInventory) {
            if (item != null) count++;
        }
        return count;
    }
    
    /**
     * Get filtered item count (current tab)
     */
    public int getFilteredItemCount() {
        return getFilteredItems(currentTab).size();
    }
    
    /**
     * Clear all items
     */
    public void clearInventory() {
        for (int i = 0; i < sharedInventory.length; i++) {
            sharedInventory[i] = null;
        }
        refreshSlotDisplay();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // RENDERING & UPDATES
    // ═══════════════════════════════════════════════════════════════
    
    private void updateSlotPositions() {
        // Update positions of visible filtered slots
        List<Item> filteredItems = getFilteredItems(currentTab);
        int visibleSlotCount = Math.min(filteredItems.size(), slots.size());
        
        for (int i = 0; i < visibleSlotCount; i++) {
            UIInventorySlot slot = slots.get(i);
            
            int row = i / columns;
            int col = i % columns;
            
            int slotX = x + padding + (col * (slotSize + gap));
            int slotY = y + padding + (row * (slotSize + gap)) - scrollOffsetY;
            
            slot.setPosition(slotX, slotY);
        }
    }
    
    @Override
    public void render(Graphics2D g) {
        if (!visible) return;
        
        // Draw background
        g.setColor(backgroundColor);
        g.fillRect(x, y, width, height);
        
        // Draw border
        g.setColor(borderColor);
        g.setStroke(new java.awt.BasicStroke(2));
        g.drawRect(x, y, width, height);
        
        // Create clipping region
        Rectangle oldClip = g.getClipBounds();
        g.setClip(x + padding, y + padding, 
                  width - padding * 2 - (showScrollbar ? 12 : 0), 
                  height - padding * 2);
        
        // Render visible slots
        updateVisibleRange();
        
        List<Item> filteredItems = getFilteredItems(currentTab);
        int visibleSlotCount = Math.min(filteredItems.size(), slots.size());
        
        for (int i = 0; i < visibleSlotCount; i++) {
            int row = i / columns;
            
            if (row >= cachedFirstVisibleRow && row <= cachedLastVisibleRow) {
                UIInventorySlot slot = slots.get(i);
                if (slot.isVisible()) {
                    slot.render(g);
                }
            }
        }
        
        // Restore clip
        g.setClip(oldClip);
        
        // Draw scrollbar
        if (showScrollbar && scrollbarAlpha > 0) {
            drawScrollbar(g);
        }
        
        // Draw item count indicator
        drawItemCount(g, filteredItems.size());
    }
    
    /**
     * Draw item count for current tab
     */
    private void drawItemCount(Graphics2D g, int count) {
        String countText = count + " / " + sharedInventory.length;
        
        g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 10));
        java.awt.FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(countText);
        
        int textX = x + width - textWidth - padding - (showScrollbar ? 12 : 0);
        int textY = y + height - 4;
        
        // Shadow
        g.setColor(new Color(0, 0, 0, 150));
        g.drawString(countText, textX + 1, textY + 1);
        
        // Text
        g.setColor(new Color(180, 180, 180));
        g.drawString(countText, textX, textY);
    }
    
    private void drawScrollbar(Graphics2D g) {
        List<Item> filteredItems = getFilteredItems(currentTab);
        int requiredRows = (int) Math.ceil((double) filteredItems.size() / columns);
        
        if (requiredRows <= visibleRows) return;  // No need for scrollbar
        
        float scrollRatio = maxScrollY > 0 ? (float)scrollOffsetY / maxScrollY : 0;
        float thumbRatio = (float)visibleRows / requiredRows;
        
        thumbHeight = Math.max(20, (int)(scrollbarHeight * thumbRatio));
        thumbY = scrollbarY + (int)((scrollbarHeight - thumbHeight) * scrollRatio);
        
        // Scrollbar background
        int bgAlpha = (int)(scrollbarAlpha * 150);
        g.setColor(new Color(scrollbarColor.getRed(), scrollbarColor.getGreen(), 
                            scrollbarColor.getBlue(), bgAlpha));
        g.fillRoundRect(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, 4, 4);
        
        // Thumb
        int thumbAlpha = (int)(scrollbarAlpha * 255);
        g.setColor(new Color(scrollbarThumbColor.getRed(), scrollbarThumbColor.getGreen(), 
                            scrollbarThumbColor.getBlue(), thumbAlpha));
        g.fillRoundRect(scrollbarX, thumbY, scrollbarWidth, thumbHeight, 4, 4);
    }
    
    @Override
    public void update(float delta) {
        if (!visible) return;
        
        // Scrollbar fade
        if (scrollbarFadeTimer > 0) {
            scrollbarFadeTimer -= delta;
        } else if (!mouseOverScrollbar && scrollbarAlpha > 0) {
            scrollbarAlpha -= delta * 1.5f;
            if (scrollbarAlpha < 0) scrollbarAlpha = 0;
        }
        
        // Update visible slots
        updateVisibleRange();
        
        List<Item> filteredItems = getFilteredItems(currentTab);
        int visibleSlotCount = Math.min(filteredItems.size(), slots.size());
        
        for (int i = 0; i < visibleSlotCount; i++) {
            int row = i / columns;
            
            if (row >= cachedFirstVisibleRow && row <= cachedLastVisibleRow) {
                slots.get(i).update(delta);
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // INPUT HANDLING
    // ═══════════════════════════════════════════════════════════════
    
    public void handleMouseMove(int mouseX, int mouseY, boolean pressed) {
        // Check scrollbar hover
        boolean wasOverScrollbar = mouseOverScrollbar;
        mouseOverScrollbar = showScrollbar && 
                            mouseX >= scrollbarX - 5 && 
                            mouseX <= scrollbarX + scrollbarWidth + 5 &&
                            mouseY >= scrollbarY && 
                            mouseY <= scrollbarY + scrollbarHeight;
        
        if (mouseOverScrollbar && !wasOverScrollbar) {
            scrollbarAlpha = 1.0f;
            scrollbarFadeTimer = 0;
        }
        
        // Update slot hovers
        updateVisibleRange();
        
        List<Item> filteredItems = getFilteredItems(currentTab);
        int visibleSlotCount = Math.min(filteredItems.size(), slots.size());
        
        for (int i = 0; i < visibleSlotCount; i++) {
            int row = i / columns;
            
            if (row >= cachedFirstVisibleRow && row <= cachedLastVisibleRow) {
                UIInventorySlot slot = slots.get(i);
                if (!slot.isVisible() || !slot.isEnabled()) continue;
                
                boolean contains = slot.contains(mouseX, mouseY);
                
                if (contains && !slot.isHovered()) {
                    slot.onMouseEnter();
                } else if (!contains && slot.isHovered()) {
                    slot.onMouseExit();
                }
            }
        }
        
        // Handle thumb dragging
        if (draggingThumb) {
            if (!pressed) {
                draggingThumb = false;
            } else {
                int newThumbY = mouseY - dragOffset;
                int minThumbY = scrollbarY;
                int maxThumbY = scrollbarY + scrollbarHeight - thumbHeight;
                newThumbY = Math.max(minThumbY, Math.min(maxThumbY, newThumbY));
                
                float scrollRatio = (float)(newThumbY - scrollbarY) / (scrollbarHeight - thumbHeight);
                scrollOffsetY = (int)(scrollRatio * maxScrollY);
                updateSlotPositions();
                
                scrollbarAlpha = 1.0f;
                scrollbarFadeTimer = 0.5f;
            }
        }
    }
    
    public boolean handleClick(int mouseX, int mouseY) {
        updateVisibleRange();
        
        List<Item> filteredItems = getFilteredItems(currentTab);
        int visibleSlotCount = Math.min(filteredItems.size(), slots.size());
        
        for (int i = visibleSlotCount - 1; i >= 0; i--) {
            int row = i / columns;
            
            if (row >= cachedFirstVisibleRow && row <= cachedLastVisibleRow) {
                UIInventorySlot slot = slots.get(i);
                if (!slot.isVisible() || !slot.isEnabled()) continue;
                
                if (slot.contains(mouseX, mouseY)) {
                    return slot.onClick();
                }
            }
        }
        
        // Handle scrollbar thumb
        if (showScrollbar) {
            List<Item> items = getFilteredItems(currentTab);
            int requiredRows = (int) Math.ceil((double) items.size() / columns);
            
            if (requiredRows > visibleRows) {
                float scrollRatio = maxScrollY > 0 ? (float)scrollOffsetY / maxScrollY : 0;
                float thumbRatio = (float)visibleRows / requiredRows;
                thumbHeight = Math.max(20, (int)(scrollbarHeight * thumbRatio));
                thumbY = scrollbarY + (int)((scrollbarHeight - thumbHeight) * scrollRatio);
                
                if (mouseX >= scrollbarX && mouseX <= scrollbarX + scrollbarWidth &&
                    mouseY >= thumbY && mouseY <= thumbY + thumbHeight) {
                    draggingThumb = true;
                    dragOffset = mouseY - thumbY;
                    scrollbarAlpha = 1.0f;
                    return true;
                }
            }
        }
        
        return this.contains(mouseX, mouseY);
    }
    
    public boolean handleRightClick(int mouseX, int mouseY) {
        updateVisibleRange();
        
        List<Item> filteredItems = getFilteredItems(currentTab);
        int visibleSlotCount = Math.min(filteredItems.size(), slots.size());
        
        for (int i = visibleSlotCount - 1; i >= 0; i--) {
            int row = i / columns;
            
            if (row >= cachedFirstVisibleRow && row <= cachedLastVisibleRow) {
                UIInventorySlot slot = slots.get(i);
                if (!slot.isVisible() || !slot.isEnabled()) continue;
                
                if (slot.contains(mouseX, mouseY)) {
                    return slot.onRightClick();
                }
            }
        }
        
        return this.contains(mouseX, mouseY);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════════
    
    public UIInventorySlot getSlot(int index) {
        if (index >= 0 && index < slots.size()) {
            return slots.get(index);
        }
        return null;
    }
    
    public List<UIInventorySlot> getSlots() {
        return slots;
    }
    
    public int getTotalSlots() {
        return columns * totalRows;
    }
    
    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
    }
    
    public void setBorderColor(Color color) {
        this.borderColor = color;
    }
    
    public UIInventorySlot getHoveredSlot(int mouseX, int mouseY) {
        updateVisibleRange();
        
        List<Item> filteredItems = getFilteredItems(currentTab);
        int visibleSlotCount = Math.min(filteredItems.size(), slots.size());
        
        for (int i = visibleSlotCount - 1; i >= 0; i--) {
            int row = i / columns;
            
            if (row >= cachedFirstVisibleRow && row <= cachedLastVisibleRow) {
                UIInventorySlot slot = slots.get(i);
                if (!slot.isVisible() || !slot.isEnabled()) continue;
                
                if (slot.contains(mouseX, mouseY)) {
                    return slot;
                }
            }
        }
        return null;
    }
    
    public String getCurrentTab() {
        return currentTab;
    }
}