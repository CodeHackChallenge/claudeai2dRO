package dev.main;

import java.awt.Color;

/**
 * Examples of how to use the UI system for different purposes
 */
public class UIExamples {
    
    /**
     * Example 1: Create a simple horizontal skill bar
     */
    public static UIPanel createHorizontalSkillBar() {
        UIPanel skillBar = new UIPanel(100, 500, 400, 64);
        skillBar.setLayout(UIPanel.LayoutType.HORIZONTAL);
        skillBar.setGap(4);
        skillBar.setPadding(8);
        
        // Add 5 skill slots
        for (int i = 0; i < 5; i++) {
            UISkillSlot slot = new UISkillSlot(0, 0, 48, String.valueOf(i + 1));
            skillBar.addChild(slot);
        }
        
        return skillBar;
    }
    
    /**
     * Example 2: Create a vertical stats panel
     */
    public static UIPanel createStatsPanel() {
        UIPanel statsPanel = new UIPanel(20, 20, 200, 300);
        statsPanel.setLayout(UIPanel.LayoutType.VERTICAL);
        statsPanel.setGap(8);
        statsPanel.setPadding(16);
        statsPanel.setBackgroundColor(new Color(20, 20, 20, 220));
        statsPanel.setBorderColor(new Color(150, 150, 150));
        
        // Add stat labels (you'd create a UILabel component)
        // For now, this is a placeholder showing structure
        
        return statsPanel;
    }
    
    /**
     * Example 3: Create a grid-based inventory (4x5 grid)
     */
    public static UIPanel createInventoryGrid() {
        int slotSize = 48;
        int columns = 5;
        int rows = 4;
        int gap = 4;
        int padding = 8;
        
        int width = (slotSize * columns) + (gap * (columns - 1)) + (padding * 2);
        int height = (slotSize * rows) + (gap * (rows - 1)) + (padding * 2);
        
        UIPanel inventory = new UIPanel(300, 100, width, height);
        inventory.setLayout(UIPanel.LayoutType.GRID);
        inventory.setGridDimensions(columns, rows);
        inventory.setGap(gap);
        inventory.setPadding(padding);
        inventory.setBackgroundColor(new Color(30, 30, 30, 230));
        
        // Add 20 inventory slots (4 rows × 5 columns)
        for (int i = 0; i < 20; i++) {
            UISkillSlot slot = new UISkillSlot(0, 0, slotSize);
            inventory.addChild(slot);
        }
        
        return inventory;
    }
    
    /**
     * Example 4: Create a minimap panel (placeholder)
     */
    public static UIPanel createMinimap() {
        int size = 150;
        UIPanel minimap = new UIPanel(
            Engine.WIDTH - size - 20,  // Top-right corner
            20,
            size,
            size
        );
        minimap.setPadding(4);
        minimap.setBackgroundColor(new Color(0, 0, 0, 180));
        minimap.setBorderColor(new Color(100, 100, 100));
        minimap.setBorderWidth(2);
        
        // You'd add custom minimap rendering logic here
        
        return minimap;
    }
    
    /**
     * Example 5: Create a buff bar (horizontal list of status effects)
     */
    public static UIPanel createBuffBar() {
        UIPanel buffBar = new UIPanel(
            (Engine.WIDTH - 300) / 2,  // Center top
            20,
            300,
            48
        );
        buffBar.setLayout(UIPanel.LayoutType.HORIZONTAL);
        buffBar.setGap(4);
        buffBar.setPadding(4);
        buffBar.setBackgroundColor(new Color(0, 0, 0, 150));
        
        // Add buff icons (would be custom UIBuff components)
        for (int i = 0; i < 5; i++) {
            UISkillSlot buffIcon = new UISkillSlot(0, 0, 36);
            buffBar.addChild(buffIcon);
        }
        
        return buffBar;
    }
    
    /**
     * Example 6: Create a quest log panel
     */
    public static UIPanel createQuestLog() {
        UIPanel questLog = new UIPanel(20, 100, 300, 400);
        questLog.setLayout(UIPanel.LayoutType.VERTICAL);
        questLog.setGap(12);
        questLog.setPadding(16);
        questLog.setBackgroundColor(new Color(25, 25, 35, 230));
        questLog.setBorderColor(new Color(100, 100, 150));
        questLog.setBorderWidth(3);
        
        // Would add UIQuestEntry components here
        
        return questLog;
    }
    
    /**
     * Example 7: Create nested panels - character sheet with tabs
     */
    public static UIPanel createCharacterSheet() {
        // Main panel
        UIPanel mainPanel = new UIPanel(200, 100, 500, 600);
        mainPanel.setLayout(UIPanel.LayoutType.VERTICAL);
        mainPanel.setGap(0);
        mainPanel.setPadding(0);
        mainPanel.setBackgroundColor(new Color(20, 20, 20, 240));
        
        // Tab bar (horizontal)
        UIPanel tabBar = new UIPanel(0, 0, 500, 40);
        tabBar.setLayout(UIPanel.LayoutType.HORIZONTAL);
        tabBar.setGap(2);
        tabBar.setPadding(4);
        tabBar.setBackgroundColor(new Color(30, 30, 30));
        mainPanel.addChild(tabBar);
        
        // Content area (would switch based on selected tab)
        UIPanel contentArea = new UIPanel(0, 0, 500, 560);
        contentArea.setPadding(16);
        contentArea.setBackgroundColor(new Color(15, 15, 15));
        mainPanel.addChild(contentArea);
        
        return mainPanel;
    }
    
    /**
     * Example 8: Dynamic skill bar that grows with player level
     */
    public static UIPanel createDynamicSkillBar(int playerLevel) {
        // Unlock more slots as player levels up
        int numSlots = Math.min(4 + (playerLevel / 5), 10);  // Start with 4, max 10
        
        int slotSize = 48;
        int gap = 4;
        int padding = 8;
        
        int width = (slotSize * numSlots) + (gap * (numSlots - 1)) + (padding * 2);
        int height = slotSize + (padding * 2);
        
        UIPanel skillBar = new UIPanel(
            (Engine.WIDTH - width) / 2,
            Engine.HEIGHT - height - 20,
            width,
            height
        );
        skillBar.setLayout(UIPanel.LayoutType.HORIZONTAL);
        skillBar.setGap(gap);
        skillBar.setPadding(padding);
        
        String[] keys = {"1", "2", "3", "4", "Q", "E", "R", "F", "Z", "X"};
        for (int i = 0; i < numSlots; i++) {
            UISkillSlot slot = new UISkillSlot(0, 0, slotSize, keys[i]);
            
            // Lock slots that are above player level
            boolean isLocked = i >= (4 + (playerLevel / 5));
            slot.setEnabled(!isLocked);
            
            skillBar.addChild(slot);
        }
        
        return skillBar;
    }
    
    /**
     * Example 9: Create a tooltip panel (follows mouse)
     */
    public static UIPanel createTooltip(String text, int mouseX, int mouseY) {
        // Calculate size based on text
        int width = 200;
        int height = 100;
        
        // Position near mouse but stay on screen
        int x = Math.min(mouseX + 10, Engine.WIDTH - width - 10);
        int y = Math.min(mouseY + 10, Engine.HEIGHT - height - 10);
        
        UIPanel tooltip = new UIPanel(x, y, width, height);
        tooltip.setPadding(8);
        tooltip.setBackgroundColor(new Color(0, 0, 0, 220));
        tooltip.setBorderColor(new Color(255, 255, 255));
        tooltip.setBorderWidth(1);
        
        // Would add UILabel with text here
        
        return tooltip;
    }
    
    /**
     * Example 10: Adaptive layout - changes based on screen size
     */
    public static UIPanel createAdaptivePanel(int screenWidth, int screenHeight) {
        boolean useVertical = screenHeight > screenWidth;  // Portrait vs landscape
        
        UIPanel panel = new UIPanel(20, 20, 200, 300);
        
        if (useVertical) {
            panel.setLayout(UIPanel.LayoutType.VERTICAL);
            panel.setSize(screenWidth - 40, 400);
        } else {
            panel.setLayout(UIPanel.LayoutType.HORIZONTAL);
            panel.setSize(600, screenHeight - 40);
        }
        
        panel.setGap(8);
        panel.setPadding(12);
        
        return panel;
    }
}