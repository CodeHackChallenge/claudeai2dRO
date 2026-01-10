package dev.main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Scrollable inventory panel with mouse wheel support
 * Fixed scrollbar flickering and thumb dragging
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
    
    public UIScrollableInventoryPanel(int x, int y, int width, int height, 
                                      int columns, int totalRows, int visibleRows) {
        super(x, y, width, height);
        
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
            availableWidth -= 10; // Reserve space for scrollbar
        }
        this.slotSize = availableWidth / columns;
        
        // Scrollbar setup
        this.scrollbarWidth = 8;
        this.scrollbarX = x + width - scrollbarWidth - 2;
        this.scrollbarY = y + padding;
        this.scrollbarHeight = height - (padding * 2);
        
        // Create slots
        createSlots();
        calculateScrollLimits();
        // Initialize per-tab storage with default tab
        this.tabItems = new HashMap<>();
        this.currentTab = "Misc";
        this.tabItems.put(currentTab, new Object[getTotalSlots()]);
        applyTabToSlots(currentTab);
    }

    // Per-tab storage
    private Map<String, Object[]> tabItems;
    private String currentTab;

    private void applyTabToSlots(String tabName) {
        Object[] items = tabItems.get(tabName);
        if (items == null) return;
        for (int i = 0; i < slots.size(); i++) {
            UIInventorySlot slot = slots.get(i);
            Object it = items[i];
            if (it == null) slot.removeItem(); else slot.setItem(it);
        }
    }
    
    private void createSlots() {
        slots.clear();
        
        int slotIndex = 0;
        for (int row = 0; row < totalRows; row++) {
            for (int col = 0; col < columns; col++) {
                int slotX = x + padding + (col * (slotSize + gap));
                int slotY = y + padding + (row * (slotSize + gap));
                
                UIInventorySlot slot = new UIInventorySlot(slotX, slotY, slotSize, slotIndex);
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
    
    /**
     * Handle mouse wheel scrolling
     */
    public void handleScroll(int wheelRotation) {
        if (!showScrollbar) return;
        
        int scrollAmount = wheelRotation * (slotSize + gap);
        scrollOffsetY = Math.max(0, Math.min(maxScrollY, scrollOffsetY + scrollAmount));
        
        // Show scrollbar and reset fade timer
        scrollbarAlpha = 1.0f;
        scrollbarFadeTimer = 1.5f; // Keep visible for 1.5 seconds
        
        updateSlotPositions();
    }

    // Switch the visible tab
    public void switchToTab(String tabName) {
        if (!tabItems.containsKey(tabName)) {
            tabItems.put(tabName, new Object[getTotalSlots()]);
        }
        this.currentTab = tabName;
        applyTabToSlots(tabName);
    }

    // Add an item to the current tab's first empty slot
    public boolean addItemToCurrentTab(Object item) {
        if (currentTab == null) currentTab = "Misc";
        Object[] items = tabItems.get(currentTab);
        if (items == null) {
            items = new Object[getTotalSlots()];
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
        
        // Create clipping region for slots
        Rectangle oldClip = g.getClipBounds();
        g.setClip(x + padding, y + padding, 
                  width - padding * 2 - (showScrollbar ? 12 : 0), 
                  height - padding * 2);
        
        // Render visible slots
        for (UIInventorySlot slot : slots) {
            if (slot.getY() + slot.getHeight() < y || slot.getY() > y + height) {
                continue; // Skip slots outside viewport
            }
            
            if (slot.isVisible()) {
                slot.render(g);
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
        // Calculate thumb position and size
        float scrollRatio = maxScrollY > 0 ? (float)scrollOffsetY / maxScrollY : 0;
        float thumbRatio = (float)visibleRows / totalRows;
        
        thumbHeight = Math.max(20, (int)(scrollbarHeight * thumbRatio));
        thumbY = scrollbarY + (int)((scrollbarHeight - thumbHeight) * scrollRatio);
        
        // Scrollbar background
        int bgAlpha = (int)(scrollbarAlpha * 150);
        g.setColor(new Color(scrollbarColor.getRed(), scrollbarColor.getGreen(), 
                            scrollbarColor.getBlue(), bgAlpha));
        g.fillRoundRect(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, 4, 4);
        
        // Draw thumb
        int thumbAlpha = (int)(scrollbarAlpha * 255);
        g.setColor(new Color(scrollbarThumbColor.getRed(), scrollbarThumbColor.getGreen(), 
                            scrollbarThumbColor.getBlue(), thumbAlpha));
        g.fillRoundRect(scrollbarX, thumbY, scrollbarWidth, thumbHeight, 4, 4);
    }
    
    @Override
    public void update(float delta) {
        if (!visible) return;
        
        // Fade out scrollbar when not in use
        if (scrollbarFadeTimer > 0) {
            scrollbarFadeTimer -= delta;
            if (scrollbarFadeTimer <= 0) {
                scrollbarFadeTimer = 0;
            }
        } else if (!mouseOverScrollbar) {
            // Only fade if mouse is not over scrollbar
            if (scrollbarAlpha > 0) {
                scrollbarAlpha -= delta * 1.5f; // Slower fade
                if (scrollbarAlpha < 0) scrollbarAlpha = 0;
            }
        }
        
        // Update slots
        for (UIInventorySlot slot : slots) {
            slot.update(delta);
        }
    }
    
    public void handleMouseMove(int mouseX, int mouseY, boolean pressed) {
        // Check if mouse is over scrollbar area
        boolean wasOverScrollbar = mouseOverScrollbar;
        mouseOverScrollbar = showScrollbar && 
                            mouseX >= scrollbarX - 5 && 
                            mouseX <= scrollbarX + scrollbarWidth + 5 &&
                            mouseY >= scrollbarY && 
                            mouseY <= scrollbarY + scrollbarHeight;
        
        // Show scrollbar on hover (but don't flicker)
        if (mouseOverScrollbar && !wasOverScrollbar) {
            scrollbarAlpha = 1.0f;
            scrollbarFadeTimer = 0; // Don't auto-fade while hovering
        }
        
        // Update slot hover states
        for (UIInventorySlot slot : slots) {
            if (!slot.isVisible() || !slot.isEnabled()) continue;
            
            // Only check slots in viewport
            if (slot.getY() + slot.getHeight() < y || slot.getY() > y + height) {
                if (slot.isHovered()) {
                    slot.onMouseExit();
                }
                continue;
            }
            
            boolean contains = slot.contains(mouseX, mouseY);
            
            if (contains && !slot.isHovered()) {
                slot.onMouseEnter();
            } else if (!contains && slot.isHovered()) {
                slot.onMouseExit();
            }
        }
        // Handle dragging the thumb
        if (draggingThumb) {
            if (!pressed) {
                // Released
                draggingThumb = false;
            } else {
                // Compute new thumbY constrained
                int newThumbY = mouseY - dragOffset;
                int minThumbY = scrollbarY;
                int maxThumbY = scrollbarY + scrollbarHeight - thumbHeight;
                newThumbY = Math.max(minThumbY, Math.min(maxThumbY, newThumbY));

                float scrollRatio = (float)(newThumbY - scrollbarY) / (float)(scrollbarHeight - thumbHeight);
                scrollOffsetY = (int)(scrollRatio * Math.max(0, maxScrollY));
                updateSlotPositions();

                scrollbarAlpha = 1.0f;
                scrollbarFadeTimer = 0.5f;
            }
        }
    }
    
    public boolean handleClick(int mouseX, int mouseY) {
        // Check slots (in reverse order for proper z-ordering)
        for (int i = slots.size() - 1; i >= 0; i--) {
            UIInventorySlot slot = slots.get(i);
            
            if (!slot.isVisible() || !slot.isEnabled()) continue;
            
            // Skip slots outside viewport
            if (slot.getY() + slot.getHeight() < y || slot.getY() > y + height) {
                continue;
            }
            
            if (slot.contains(mouseX, mouseY)) {
                return slot.onClick();
            }
        }
        
        // Consume click if inside panel
        // If click was on scrollbar thumb start dragging
        if (showScrollbar) {
            // Ensure thumb metrics are up-to-date
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
        // Check slots
        for (int i = slots.size() - 1; i >= 0; i--) {
            UIInventorySlot slot = slots.get(i);
            
            if (!slot.isVisible() || !slot.isEnabled()) continue;
            
            // Skip slots outside viewport
            if (slot.getY() + slot.getHeight() < y || slot.getY() > y + height) {
                continue;
            }
            
            if (slot.contains(mouseX, mouseY)) {
                return slot.onRightClick();
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
}