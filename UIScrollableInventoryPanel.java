package dev.main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * ⭐ OPTIMIZED: Only renders visible slots, pre-allocates tab storage
 */
public class UIScrollableInventoryPanel extends UIComponent {
    
    // ⭐ NEW: Known tab names as constant
    private static final String[] KNOWN_TABS = {"Misc", "Weap", "Arm", "Acc", "Rune"};
    
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
    
    // Per-tab storage
    private Map<String, Item[]> tabItems;
    private String currentTab;
    
    // Reference to UIManager for equipping items
    private UIManager uiManager;
    
    // ⭐ NEW: Cached visible range
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
        
        // ⭐ OPTIMIZED: Pre-allocate all known tabs
        this.tabItems = new HashMap<>();
        this.currentTab = "Misc";
        
        for (String tabName : KNOWN_TABS) {
            tabItems.put(tabName, new Item[getTotalSlots()]);
        }
        
        // Create slots
        createSlots();
        calculateScrollLimits();
        applyTabToSlots(currentTab);
    }
    
    private void applyTabToSlots(String tabName) {
        Item[] items = tabItems.get(tabName);
        if (items == null) return;
        
        for (int i = 0; i < slots.size(); i++) {
            UIInventorySlot slot = slots.get(i);
            Item it = items[i];
            if (it == null) {
                slot.removeItem();
            } else {
                slot.setItem(it);
            }
        }
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
    
    // ⭐ NEW: Calculate visible row range
    private void updateVisibleRange() {
        if (scrollOffsetY == lastScrollOffsetY) {
            return; // No change
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
    
    public void switchToTab(String tabName) {
        // ⭐ Only allocate if unknown tab (shouldn't happen with pre-allocation)
        if (!tabItems.containsKey(tabName)) {
            System.out.println("Warning: Unknown tab '" + tabName + "' - allocating");
            tabItems.put(tabName, new Item[getTotalSlots()]);
        }
        
        this.currentTab = tabName;
        applyTabToSlots(tabName);
    }
    
    public boolean addItemToCurrentTab(Item item) {
        if (currentTab == null) currentTab = "Misc";
        
        Item[] items = tabItems.get(currentTab);
        if (items == null) {
            items = new Item[getTotalSlots()];
            tabItems.put(currentTab, items);
        }
        
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) {
                items[i] = item;
                UIInventorySlot slot = getSlot(i);
                if (slot != null) slot.setItem(item);
                return true;
            }
        }
        return false;
    }
    
    public boolean removeItemFromSlot(int slotIndex) {
        if (currentTab == null || slotIndex < 0 || slotIndex >= getTotalSlots()) {
            return false;
        }
        
        Item[] items = tabItems.get(currentTab);
        if (items != null && items[slotIndex] != null) {
            items[slotIndex] = null;
            UIInventorySlot slot = getSlot(slotIndex);
            if (slot != null) {
                slot.removeItem();
            }
            return true;
        }
        return false;
    }
    
    private void updateSlotPositions() {
        int slotIndex = 0;
        for (int row = 0; row < totalRows; row++) {
            for (int col = 0; col < columns; col++) {
                UIInventorySlot slot = slots.get(slotIndex);
                
                int slotX = x + padding + (col * (slotSize + gap));
                int slotY = y + padding + (row * (slotSize + gap)) - scrollOffsetY;
                
                slot.setPosition(slotX, slotY);
                slotIndex++;
            }
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
        
        // ⭐ OPTIMIZED: Only render visible slots
        updateVisibleRange();
        
        for (int row = cachedFirstVisibleRow; row <= cachedLastVisibleRow; row++) {
            for (int col = 0; col < columns; col++) {
                int index = row * columns + col;
                if (index < slots.size()) {
                    UIInventorySlot slot = slots.get(index);
                    if (slot.isVisible()) {
                        slot.render(g);
                    }
                }
            }
        }
        
        // Restore clip
        g.setClip(oldClip);
        
        // Draw scrollbar
        if (showScrollbar && scrollbarAlpha > 0) {
            drawScrollbar(g);
        }
    }
    
    private void drawScrollbar(Graphics2D g) {
        float scrollRatio = maxScrollY > 0 ? (float)scrollOffsetY / maxScrollY : 0;
        float thumbRatio = (float)visibleRows / totalRows;
        
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
        
        // ⭐ OPTIMIZED: Unified scrollbar fade logic
        if (scrollbarFadeTimer > 0) {
            scrollbarFadeTimer -= delta;
        } else if (!mouseOverScrollbar && scrollbarAlpha > 0) {
            scrollbarAlpha -= delta * 1.5f;
            if (scrollbarAlpha < 0) scrollbarAlpha = 0;
        }
        
        // ⭐ OPTIMIZED: Only update visible slots
        updateVisibleRange();
        for (int row = cachedFirstVisibleRow; row <= cachedLastVisibleRow; row++) {
            for (int col = 0; col < columns; col++) {
                int index = row * columns + col;
                if (index < slots.size()) {
                    slots.get(index).update(delta);
                }
            }
        }
    }
    
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
        
        // ⭐ OPTIMIZED: Only check visible slots
        updateVisibleRange();
        
        for (int row = cachedFirstVisibleRow; row <= cachedLastVisibleRow; row++) {
            for (int col = 0; col < columns; col++) {
                int index = row * columns + col;
                if (index >= slots.size()) continue;
                
                UIInventorySlot slot = slots.get(index);
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
        // ⭐ OPTIMIZED: Only check visible slots
        updateVisibleRange();
        
        for (int row = cachedLastVisibleRow; row >= cachedFirstVisibleRow; row--) {
            for (int col = columns - 1; col >= 0; col--) {
                int index = row * columns + col;
                if (index >= slots.size()) continue;
                
                UIInventorySlot slot = slots.get(index);
                if (!slot.isVisible() || !slot.isEnabled()) continue;
                
                if (slot.contains(mouseX, mouseY)) {
                    return slot.onClick();
                }
            }
        }
        
        // Handle scrollbar thumb
        if (showScrollbar) {
            float scrollRatio = maxScrollY > 0 ? (float)scrollOffsetY / maxScrollY : 0;
            float thumbRatio = (float)visibleRows / totalRows;
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
        
        return this.contains(mouseX, mouseY);
    }
    
    public boolean handleRightClick(int mouseX, int mouseY) {
        // ⭐ OPTIMIZED: Only check visible slots
        updateVisibleRange();
        
        for (int row = cachedLastVisibleRow; row >= cachedFirstVisibleRow; row--) {
            for (int col = columns - 1; col >= 0; col--) {
                int index = row * columns + col;
                if (index >= slots.size()) continue;
                
                UIInventorySlot slot = slots.get(index);
                if (!slot.isVisible() || !slot.isEnabled()) continue;
                
                if (slot.contains(mouseX, mouseY)) {
                    return slot.onRightClick();
                }
            }
        }
        
        return this.contains(mouseX, mouseY);
    }
    
    // Public methods
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
        // Check visible slots
        updateVisibleRange();
        
        for (int row = cachedLastVisibleRow; row >= cachedFirstVisibleRow; row--) {
            for (int col = columns - 1; col >= 0; col--) {
                int index = row * columns + col;
                if (index >= slots.size()) continue;
                
                UIInventorySlot slot = slots.get(index);
                if (!slot.isVisible() || !slot.isEnabled()) continue;
                
                if (slot.contains(mouseX, mouseY)) {
                    return slot;
                }
            }
        }
        return null;
    }
}