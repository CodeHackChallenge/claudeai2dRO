package dev.main;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages all UI panels and handles input routing
 */
public class UIManager {
    
    private List<UIPanel> panels;
    private UIComponent hoveredComponent;
    private GameState gameState;
    private GameLogic gameLogic;
    
    // UI Panels
    private UIPanel skillBar;
    private UIPanel verticalMenu;  // ☆ NEW: Right-side vertical menu
    private UIPanel inventoryPanel;  // ☆ NEW: Inventory panel (opens when clicked)
    private UIPanel gearPanel;  // ☆ NEW: Add to class fields
    
    
    public UIManager(GameState gameState) {
        this.gameState = gameState;
        this.gameLogic = null;
        this.panels = new ArrayList<>();
        
        initializeUI();
    }
    
    /**
     * Set game logic reference (for skill execution)
     */
    public void setGameLogic(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }
    
    private void initializeUI() {
        // Create skill bar at bottom center
        createSkillBar();
        
        // ☆ NEW: Create vertical menu on right side
        createVerticalMenu();
    }
    
    /**
     * Create the skill bar with 8 skill slots
     */
    private void createSkillBar() {
        int slotSize = 48;
        int numSlots = 8;
        int gap = 4;
        int padding = 8;
        
        int barWidth = (slotSize * numSlots) + (gap * (numSlots - 1)) + (padding * 2);
        int barHeight = slotSize + (padding * 2);
        
        int barX = (Engine.WIDTH - barWidth) / 2;
        int barY = Engine.HEIGHT - barHeight - 20;
        
        skillBar = new UIPanel(barX, barY, barWidth, barHeight);
        skillBar.setLayout(UIPanel.LayoutType.HORIZONTAL);
        skillBar.setGap(gap);
        skillBar.setPadding(padding);
        skillBar.setBackgroundColor(new java.awt.Color(30, 30, 30, 220));
        skillBar.setBorderColor(new java.awt.Color(100, 100, 100, 255));
        skillBar.setBorderWidth(2);
        
        // Add 8 skill slots with keybindings
        String[] keys = {"1", "2", "3", "4", "Q", "E", "R", "F"};
        for (int i = 0; i < numSlots; i++) {
            UISkillSlot slot = new UISkillSlot(0, 0, slotSize, keys[i]);
            slot.setMargin(0);
            slot.setUIManager(this, i);
            skillBar.addChild(slot);
        }
        
        // Add example skills
        addExampleSkills();
        
        panels.add(skillBar);
    }
    
    /**
     * ☆ NEW: Create vertical menu on right-center of screen
     */ 
    private void createVerticalMenu() {
        int buttonSize = 48;
        int gap = 4;
        int padding = 0;  // No padding - just icons
        
        int numButtons = 10;  // Total buttons (9 locked + 1 unlocked)
        int menuHeight = (buttonSize * numButtons) + (gap * (numButtons - 1)) + (padding * 2);
        int menuWidth = buttonSize + (padding * 2);
        
        // ☆ Position: right side, positioned ABOVE the skill bar
        int skillBarHeight = 64;  // Height of skill bar (48 + padding)
        int skillBarMargin = 20;  // Margin from bottom
        int menuMarginFromSkillBar = 10;  // Space between menu and skill bar
        
        int menuX = Engine.WIDTH - menuWidth - 10;
        int menuY = Engine.HEIGHT - skillBarHeight - skillBarMargin - menuHeight - menuMarginFromSkillBar;
        
        verticalMenu = new UIPanel(menuX, menuY, menuWidth, menuHeight);
        verticalMenu.setLayout(UIPanel.LayoutType.VERTICAL);
        verticalMenu.setGap(gap);
        verticalMenu.setPadding(padding);
        
        // ☆ No background, no border
        verticalMenu.setBackgroundColor(null);
        verticalMenu.setBorderColor(null);
        verticalMenu.setBorderWidth(0);
        
        // Create menu buttons (in order from top to bottom)
        String[] buttonLabels = {
            "Settings",
            "World",
            "Trade",
            "Message",
            "Quest",
            "Stats",
            "Character Info",
            "Skill Tree",
            "Gear",
            "Inventory"
        };
        
        String[] buttonIds = {
            "settings",
            "world",
            "trade",
            "message",
            "quest",
            "stats",
            "character",
            "skilltree",
            "gear",
            "inventory"
        };
        
        for (int i = 0; i < buttonLabels.length; i++) {
            UIButton button = new UIButton(0, 0, buttonSize, buttonSize, buttonIds[i], buttonLabels[i]);
            
            // Set icon paths
            String iconPath = "/ui/icons/" + buttonIds[i] + ".png";
            String iconHoverPath = "/ui/icons/" + buttonIds[i] + "_hover.png";
            String iconLockedPath = "/ui/icons/" + buttonIds[i] + "_locked.png";
            
            button.setIcons(iconPath, iconHoverPath, iconLockedPath);
            
            // ☆ Set callbacks based on button type
            if (buttonIds[i].equals("inventory")) {
                button.setLocked(false);
                button.setVisible(true);
                button.setOnClick(() -> toggleInventory());
            } else if (buttonIds[i].equals("gear")) {
                button.setLocked(true);
                button.setVisible(true);
                button.setOnClick(() -> toggleGear());  // ☆ Uses toggleGear()
            } else {
                button.setLocked(true);
                button.setVisible(true);
            }
            
            verticalMenu.addChild(button);
        }
        
        panels.add(verticalMenu);
        
        System.out.println("Vertical menu created at: (" + menuX + ", " + menuY + ") Size: " + menuWidth + "x" + menuHeight);
    }
    
    /**
     * ☆ NEW: Toggle inventory panel
     */
    private void toggleInventory() {
        if (inventoryPanel == null) {
            createInventoryPanel();
        } else {
            // Toggle visibility
            inventoryPanel.setVisible(!inventoryPanel.isVisible());
        }
        
        System.out.println("Inventory " + (inventoryPanel.isVisible() ? "opened" : "closed"));
    }
    /**
     * ☆ FIXED: Create inventory panel (5x10 grid beside menu, aligned with settings button)
     */
    private void createInventoryPanel() {
        int slotSize = 48;
        int columns = 5;
        int rows = 10;
        int gap = 4;
        int padding = 8;
        
        int panelWidth = (slotSize * columns) + (gap * (columns - 1)) + (padding * 2);
        int panelHeight = (slotSize * rows) + (gap * (rows - 1)) + (padding * 2);
        
        // ☆ FIXED: Position to the LEFT of vertical menu, aligned with SETTINGS button (top)
        UIButton settingsButton = getMenuButton("settings");
        int panelX;
        int panelY;
        
        if (verticalMenu != null && settingsButton != null) {
            // Align to left of vertical menu
            panelX = verticalMenu.getX() - panelWidth - 10;
            
            // ☆ FIXED: Align top with settings button (first button in menu)
            panelY = settingsButton.getY();
        } else {
            // Fallback: use menu position if settings button not found
            if (verticalMenu != null) {
                panelX = verticalMenu.getX() - panelWidth - 10;
                panelY = verticalMenu.getY();
            } else {
                // Last resort fallback
                panelX = Engine.WIDTH - panelWidth - 70;
                panelY = 100;
            }
        }
        
        inventoryPanel = new UIPanel(panelX, panelY, panelWidth, panelHeight);
        inventoryPanel.setLayout(UIPanel.LayoutType.GRID);
        inventoryPanel.setGridDimensions(columns, rows);
        inventoryPanel.setGap(gap);
        inventoryPanel.setPadding(padding);
        
        // Dark background with border
        inventoryPanel.setBackgroundColor(new java.awt.Color(20, 20, 30, 230));
        inventoryPanel.setBorderColor(new java.awt.Color(100, 100, 120));
        inventoryPanel.setBorderWidth(2);
        
        // Create 50 inventory slots (5x10 = 50)
        for (int i = 0; i < columns * rows; i++) {
            UIInventorySlot slot = new UIInventorySlot(0, 0, slotSize, i);
            inventoryPanel.addChild(slot);
        }
        
        // Add some test items to first few slots
        addTestItems();
        
        panels.add(inventoryPanel);
        
        System.out.println("Inventory created: 5x10 grid (50 slots)");
        System.out.println("Position: (" + panelX + ", " + panelY + ") Size: " + panelWidth + "x" + panelHeight);
        System.out.println("Aligned with: Settings button (top of menu)");
    }

    /**
     * ☆ NEW: Add test items for demonstration
     */
    private void addTestItems() {
        if (inventoryPanel == null) return;
        
        List<UIComponent> slots = inventoryPanel.getChildren();
        
        // Add placeholder items to first 5 slots for testing
        for (int i = 0; i < Math.min(5, slots.size()); i++) {
            if (slots.get(i) instanceof UIInventorySlot) {
                UIInventorySlot slot = (UIInventorySlot) slots.get(i);
                slot.setItem("TestItem" + i);  // Placeholder item
            }
        }
    } 

    /**
     * ☆ NEW: Get inventory slot by index
     */
    public UIInventorySlot getInventorySlot(int index) {
        if (inventoryPanel == null) return null;
        
        List<UIComponent> slots = inventoryPanel.getChildren();
        if (index >= 0 && index < slots.size() && slots.get(index) instanceof UIInventorySlot) {
            return (UIInventorySlot) slots.get(index);
        }
        
        return null;
    }

    /**
     * ☆ NEW: Add item to first empty inventory slot
     */
    public boolean addItemToInventory(Object item) {
        if (inventoryPanel == null) {
            createInventoryPanel();
        }
        
        List<UIComponent> slots = inventoryPanel.getChildren();
        
        // Find first empty slot
        for (UIComponent component : slots) {
            if (component instanceof UIInventorySlot) {
                UIInventorySlot slot = (UIInventorySlot) component;
                if (slot.isEmpty()) {
                    slot.setItem(item);
                    System.out.println("Added item to slot " + slot.getSlotIndex());
                    return true;
                }
            }
        }
        
        System.out.println("Inventory full! Cannot add item.");
        return false;  // Inventory full
    }

    /**
     * ☆ NEW: Remove item from inventory slot
     */
    public void removeItemFromSlot(int slotIndex) {
        UIInventorySlot slot = getInventorySlot(slotIndex);
        if (slot != null) {
            slot.removeItem();
            System.out.println("Removed item from slot " + slotIndex);
        }
    }

    /**
     * ☆ NEW: Get inventory panel reference
     */
    public UIPanel getInventoryPanel() {
        return inventoryPanel;
    }
    
    /**
     * ☆ NEW: Get a menu button by ID
     */
    public UIButton getMenuButton(String id) {
        if (verticalMenu == null) return null;
        
        for (UIComponent child : verticalMenu.getChildren()) {
            if (child instanceof UIButton) {
                UIButton button = (UIButton) child;
                if (button.getId().equals(id)) {
                    return button;
                }
            }
        }
        
        return null;
    }
    
    /**
     * ☆ NEW: Unlock a menu button
     */ 
    public void unlockMenuButton(String id) {
        UIButton button = getMenuButton(id);
        if (button != null) {
            button.unlock();
            
            // ☆ Force panel to relayout (in case positions were wrong)
            if (verticalMenu != null) {
                verticalMenu.relayout();
            }
            
            System.out.println("Unlocked: " + button.getLabel());
        }
    }

    /**
     * ☆ UPDATED: Lock a menu button
     */
    public void lockMenuButton(String id) {
        UIButton button = getMenuButton(id);
        if (button != null) {
            button.lock();
            
            // ☆ Force panel to relayout
            if (verticalMenu != null) {
                verticalMenu.relayout();
            }
            
            System.out.println("Locked: " + button.getLabel());
        }
    } 
    
    /**
     * Add example skills for testing
     */
    private void addExampleSkills() {
        // Create some test skills with mana costs
        Skill fireball = new Skill(
            "fireball",
            "Fireball",
            "Launch a blazing fireball at your enemy",
            Skill.SkillType.ATTACK,
            3.0f,
            12,
            1
        );
        
        Skill heal = new Skill(
            "heal",
            "Heal",
            "Restore health over time",
            Skill.SkillType.HEAL,
            8.0f,
            12,
            1
        ); 
        
        Skill shield = new Skill(
            "shield",
            "Shield",
            "Create a protective barrier",
            Skill.SkillType.DEFENSE,
            12.0f,
            12,
            3
        );
        
        Skill haste = new Skill(
            "haste",
            "Haste",
            "Increase movement speed",
            Skill.SkillType.BUFF,
            20.0f,
            12,
            2
        );
        
        // Assign skills to slots
        List<UIComponent> slots = skillBar.getChildren();
        if (slots.size() >= 4) {
            ((UISkillSlot)slots.get(0)).setSkill(fireball);
            ((UISkillSlot)slots.get(1)).setSkill(heal);
            ((UISkillSlot)slots.get(2)).setSkill(shield);
            ((UISkillSlot)slots.get(4)).setSkill(haste);
        }
    } 
 

 // ==================== GEAR PANEL METHODS ====================
 
    /**
     * ☆ Create gear/equipment panel with correct absolute positioning
     */
    private void createGearPanel() {
        int slotWidth = 48;
        int slotHeight = 48;
        int gap = 4;
        int padding = 8;
        int centerWidth = 80;
        
        int panelWidth = slotWidth + gap + centerWidth + gap + slotWidth + (padding * 2);
        
        if (inventoryPanel == null) {
            createInventoryPanel();
        }
        int inventoryHeight = inventoryPanel.getHeight();
        int panelHeight = (int)(inventoryHeight * 0.7f);
        
        UIButton settingsButton = getMenuButton("settings");
        int panelX;
        int panelY;
        
        if (inventoryPanel != null && settingsButton != null) {
            panelX = inventoryPanel.getX() - panelWidth - 10;
            panelY = settingsButton.getY();
        } else {
            panelX = Engine.WIDTH - panelWidth - 320;
            panelY = 100;
        }
        
        gearPanel = new UIPanel(panelX, panelY, panelWidth, panelHeight);
        gearPanel.setLayout(UIPanel.LayoutType.NONE);
        gearPanel.setPadding(0);
        gearPanel.setBackgroundColor(new java.awt.Color(20, 20, 30, 230));
        gearPanel.setBorderColor(new java.awt.Color(100, 100, 120));
        gearPanel.setBorderWidth(2);
        
        int slotSpacing = (panelHeight - (padding * 2) - (slotHeight * 6)) / 5;
        
        // Left column - use helper method
        addGearSlotRelative(padding, padding + (slotHeight + slotSpacing) * 0, slotWidth, slotHeight, UIGearSlot.SlotType.HEAD);
        addGearSlotRelative(padding, padding + (slotHeight + slotSpacing) * 1, slotWidth, slotHeight, UIGearSlot.SlotType.TOP_ARMOR);
        addGearSlotRelative(padding, padding + (slotHeight + slotSpacing) * 2, slotWidth, slotHeight, UIGearSlot.SlotType.GLOVES);
        addGearSlotRelative(padding, padding + (slotHeight + slotSpacing) * 3, slotWidth, slotHeight, UIGearSlot.SlotType.BELT);
        addGearSlotRelative(padding, padding + (slotHeight + slotSpacing) * 4, slotWidth, slotHeight, UIGearSlot.SlotType.PANTS);
        addGearSlotRelative(padding, padding + (slotHeight + slotSpacing) * 5, slotWidth, slotHeight, UIGearSlot.SlotType.SHOES);
        
        // Center character preview
        addCharacterPreviewRelative(padding + slotWidth + gap, padding, centerWidth, panelHeight - (padding * 2));
        
        // Right column
        int rightOffset = padding + slotWidth + gap + centerWidth + gap;
        addGearSlotRelative(rightOffset, padding + (slotHeight + slotSpacing) * 0, slotWidth, slotHeight, UIGearSlot.SlotType.TIARA);
        addGearSlotRelative(rightOffset, padding + (slotHeight + slotSpacing) * 1, slotWidth, slotHeight, UIGearSlot.SlotType.EARRINGS);
        addGearSlotRelative(rightOffset, padding + (slotHeight + slotSpacing) * 2, slotWidth, slotHeight, UIGearSlot.SlotType.NECKLACE);
        addGearSlotRelative(rightOffset, padding + (slotHeight + slotSpacing) * 3, slotWidth, slotHeight, UIGearSlot.SlotType.BRACELET);
        addGearSlotRelative(rightOffset, padding + (slotHeight + slotSpacing) * 4, slotWidth, slotHeight, UIGearSlot.SlotType.RING_1);
        addGearSlotRelative(rightOffset, padding + (slotHeight + slotSpacing) * 5, slotWidth, slotHeight, UIGearSlot.SlotType.RING_2);
        
        addTestEquipment();
        panels.add(gearPanel);
        
        System.out.println("Gear panel created at: (" + panelX + ", " + panelY + ")");
    }

    /**
     * ☆ Helper method to add gear slot with relative positioning
     */
    private void addGearSlotRelative(int relX, int relY, int width, int height, UIGearSlot.SlotType slotType) {
        int absoluteX = gearPanel.getX() + relX;
        int absoluteY = gearPanel.getY() + relY;
        UIGearSlot slot = new UIGearSlot(absoluteX, absoluteY, width, height, slotType);
        gearPanel.addChild(slot);
    }

    /**
     * ☆ Helper method to add character preview with relative positioning
     */
    private void addCharacterPreviewRelative(int relX, int relY, int width, int height) {
        int absoluteX = gearPanel.getX() + relX;
        int absoluteY = gearPanel.getY() + relY;
        UIPanel previewPanel = new UIPanel(absoluteX, absoluteY, width, height);
        previewPanel.setBackgroundColor(new java.awt.Color(40, 40, 50, 150));
        previewPanel.setBorderColor(new java.awt.Color(80, 80, 90));
        previewPanel.setBorderWidth(1);
        gearPanel.addChild(previewPanel);
    }

    /**
     * ☆ Add test equipment to gear slots
     */
    private void addTestEquipment() {
        if (gearPanel == null) return;
        
        List<UIComponent> children = gearPanel.getChildren();
        int equipped = 0;
        
        for (UIComponent component : children) {
            if (component instanceof UIGearSlot && equipped < 3) {
                UIGearSlot slot = (UIGearSlot) component;
                slot.equipItem("Test" + slot.getSlotType());
                equipped++;
            }
        }
    } 
    /**
     * ☆ Get gear slot by type
     */
    public UIGearSlot getGearSlot(UIGearSlot.SlotType slotType) {
        if (gearPanel == null) return null;
        
        for (UIComponent component : gearPanel.getChildren()) {
            if (component instanceof UIGearSlot) {
                UIGearSlot slot = (UIGearSlot) component;
                if (slot.getSlotType() == slotType) {
                    return slot;
                }
            }
        }
        
        return null;
    }

    /**
     * ☆ Equip item to gear slot
     */
    public boolean equipItem(UIGearSlot.SlotType slotType, Object item) {
        UIGearSlot slot = getGearSlot(slotType);
        if (slot != null) {
            slot.equipItem(item);
            System.out.println("Equipped item to " + slotType);
            return true;
        }
        return false;
    } 
 
 
    /**
     * Update all UI components
     */
    public void update(float delta) {
        for (UIPanel panel : panels) {
            if (panel.isVisible()) {
                panel.update(delta);
            }
        }
    }
    
    /**
     * Render all UI panels
     */
    public void render(Graphics2D g) {
        for (UIPanel panel : panels) {
            if (panel.isVisible()) {
                panel.render(g);
            }
        }
    }
    
    /**
     * Handle mouse movement
     */
    public void handleMouseMove(int mouseX, int mouseY) {
        for (UIPanel panel : panels) {
            if (panel.isVisible()) {
                panel.handleMouseMove(mouseX, mouseY);
            }
        }
    }
    
    /**
     * Handle mouse click
     */
    public boolean handleClick(int mouseX, int mouseY) {
        // Check panels in reverse order (top-most first)
        for (int i = panels.size() - 1; i >= 0; i--) {
            UIPanel panel = panels.get(i);
            if (!panel.isVisible()) continue;
            
            if (panel.handleClick(mouseX, mouseY)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Handle right click
     */
    public boolean handleRightClick(int mouseX, int mouseY) {
        for (int i = panels.size() - 1; i >= 0; i--) {
            UIPanel panel = panels.get(i);
            if (!panel.isVisible()) continue;
            
            if (panel.handleRightClick(mouseX, mouseY)) {
                return true;
            }
        }
        
        return false;
    }
      
    /**
     * Handle keyboard input for skill hotkeys
     */
    public void handleKeyPress(int keyCode) {
        // Map keycodes to skill slots
        if (skillBar != null) {
            List<UIComponent> slots = skillBar.getChildren();
            
            // Number keys 1-8
            if (keyCode >= java.awt.event.KeyEvent.VK_1 && 
                keyCode <= java.awt.event.KeyEvent.VK_8) {
                int index = keyCode - java.awt.event.KeyEvent.VK_1;
                if (index < slots.size()) {
                    useSkillInSlot(index);
                }
            }
            
            // Letter keys Q, E, R, F
            switch (keyCode) {
                case java.awt.event.KeyEvent.VK_Q:
                    if (slots.size() > 4) useSkillInSlot(4);
                    break;
                case java.awt.event.KeyEvent.VK_E:
                    if (slots.size() > 5) useSkillInSlot(5);
                    break;
                case java.awt.event.KeyEvent.VK_R:
                    if (slots.size() > 6) useSkillInSlot(6);
                    break;
                case java.awt.event.KeyEvent.VK_F:
                    if (slots.size() > 7) useSkillInSlot(7);
                    break;
            }
        }
        
        // Toggle inventory with 'I' key
        if (keyCode == java.awt.event.KeyEvent.VK_I) {
            toggleInventory();
        }
        
        // ☆ FIXED: Toggle gear with 'K' key - auto-unlock if needed
        if (keyCode == java.awt.event.KeyEvent.VK_K) {
            // Check if gear button is locked
            UIButton gearButton = getMenuButton("gear");
            if (gearButton != null && gearButton.isLocked()) {
                // Unlock it first (but don't relayout during this frame)
                gearButton.setLocked(false);
                gearButton.setEnabled(true);
                System.out.println("Auto-unlocked Gear button");
            }
            
            // Toggle the gear panel
            toggleGear();
        }
    }
    /**
     * ☆ Toggle gear panel
     */
    private void toggleGear() {
        try {
            if (gearPanel == null) {
                createGearPanel();
            } else {
                gearPanel.setVisible(!gearPanel.isVisible());
            }
            
            System.out.println("Gear panel " + (gearPanel != null && gearPanel.isVisible() ? "opened" : "closed"));
        } catch (Exception e) {
            System.err.println("Error toggling gear panel: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Use skill in specific slot (called by hotkey OR by clicking slot)
     */
    public void useSkillInSlot(int slotIndex) {
        UISkillSlot slot = getSkillSlot(slotIndex);
        if (slot != null && slot.getSkill() != null) {
            Skill skill = slot.getSkill();
            
            if (skill.isReady()) {
                // Execute skill through GameLogic
                if (gameLogic != null) {
                    Entity player = gameState.getPlayer();
                    gameLogic.useSkill(player, skill);
                } else {
                    // Fallback: just start cooldown
                    skill.use();
                    System.out.println("Used skill: " + skill.getName());
                }
            } else {
                System.out.println("Skill on cooldown: " + String.format("%.1f", skill.getRemainingCooldown()) + "s remaining");
            }
        }
    }
    
    /**
     * Upgrade a skill (spend skill points)
     */
    public boolean upgradeSkill(int slotIndex) {
        Entity player = gameState.getPlayer();
        SkillLevel skillLevel = player.getComponent(SkillLevel.class);
        UISkillSlot slot = getSkillSlot(slotIndex);
        
        if (slot == null || skillLevel == null) return false;
        
        Skill skill = slot.getSkill();
        if (skill == null) return false;
        
        // Check if can upgrade
        if (!skill.canUpgrade()) {
            System.out.println("Skill is already max level!");
            return false;
        }
        
        int cost = skill.getUpgradeCost();
        
        // Check if have enough points
        if (!skillLevel.canAfford(cost)) {
            System.out.println("Not enough skill points! Need " + cost + ", have " + skillLevel.availablePoints);
            return false;
        }
        
        // Spend points and upgrade
        skillLevel.spendPoints(cost);
        skill.upgrade();
        
        System.out.println("╔════════════════════════════════╗");
        System.out.println("║     SKILL UPGRADED!            ║");
        System.out.println("╠════════════════════════════════╣");
        System.out.println("║ " + skill.getName() + " → Level " + skill.getSkillLevel());
        System.out.println("║ Cost: " + cost + " point(s)");
        System.out.println("║ Remaining: " + skillLevel.availablePoints + " point(s)");
        
        if (skill.getType() == Skill.SkillType.HEAL) {
            System.out.println("║ Heal Power: " + String.format("%.1f", skill.getHealPercent() * 100) + "%");
        }
        
        System.out.println("╚════════════════════════════════╝");
        
        return true;
    }
    
    /**
     * Add a panel to the manager
     */
    public void addPanel(UIPanel panel) {
        panels.add(panel);
    }
    
    /**
     * Remove a panel from the manager
     */
    public void removePanel(UIPanel panel) {
        panels.remove(panel);
    }
    
    /**
     * Get the skill bar
     */
    public UIPanel getSkillBar() {
        return skillBar;
    }
    
    /**
     * Get a skill slot by index
     */
    public UISkillSlot getSkillSlot(int index) {
        if (skillBar == null) return null;
        
        List<UIComponent> slots = skillBar.getChildren();
        if (index >= 0 && index < slots.size()) {
            return (UISkillSlot)slots.get(index);
        }
        
        return null;
    }
    
    /**
     * Equip a skill to a specific slot
     */
    public void equipSkill(Skill skill, int slotIndex) {
        UISkillSlot slot = getSkillSlot(slotIndex);
        if (slot != null) {
            slot.setSkill(skill);
            System.out.println("Equipped " + skill.getName() + " to slot " + (slotIndex + 1));
        }
    }
    
    /**
     * Clear a skill slot
     */
    public void clearSkillSlot(int slotIndex) {
        UISkillSlot slot = getSkillSlot(slotIndex);
        if (slot != null) {
            slot.setSkill(null);
        }
    }
}