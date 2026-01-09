package dev.main;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * FIXED: Tabs now work properly with click and hover
 */
public class UIManager {
    
    private List<UIPanel> panels;
    private UIComponent hoveredComponent;
    private GameState gameState;
    private GameLogic gameLogic;
    
    private UIPanel skillBar;
    private UIPanel verticalMenu;
    private UIPanel inventoryPanel;
    private UIPanel gearPanel;
    
    private String currentInventoryTab = "Misc";
    private List<UIInventoryTab> inventoryTabs;  // ⭐ Store tabs directly
    
    public UIManager(GameState gameState) {
        this.gameState = gameState;
        this.gameLogic = null;
        this.panels = new ArrayList<>();
        this.inventoryTabs = new ArrayList<>();
        
        initializeUI();
    }
    
    public void setGameLogic(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }
    
    private void initializeUI() {
        createSkillBar();
        createVerticalMenu();
    }
    
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
        
        String[] keys = {"1", "2", "3", "4", "Q", "E", "R", "F"};
        for (int i = 0; i < numSlots; i++) {
            UISkillSlot slot = new UISkillSlot(0, 0, slotSize, keys[i]);
            slot.setMargin(0);
            slot.setUIManager(this, i);
            skillBar.addChild(slot);
        }
        
        addExampleSkills();
        panels.add(skillBar);
    }
    
    private void createVerticalMenu() {
        int buttonSize = 48;
        int gap = 4;
        int padding = 0;
        
        int numButtons = 10;
        int menuHeight = (buttonSize * numButtons) + (gap * (numButtons - 1)) + (padding * 2);
        int menuWidth = buttonSize + (padding * 2);
        
        int skillBarHeight = 64;
        int skillBarMargin = 20;
        int menuMarginFromSkillBar = 10;
        
        int menuX = Engine.WIDTH - menuWidth - 10;
        int menuY = Engine.HEIGHT - skillBarHeight - skillBarMargin - menuHeight - menuMarginFromSkillBar;
        
        verticalMenu = new UIPanel(menuX, menuY, menuWidth, menuHeight);
        verticalMenu.setLayout(UIPanel.LayoutType.VERTICAL);
        verticalMenu.setGap(gap);
        verticalMenu.setPadding(padding);
        verticalMenu.setBackgroundColor(null);
        verticalMenu.setBorderColor(null);
        verticalMenu.setBorderWidth(0);
        
        String[] buttonLabels = {
            "Settings", "World", "Trade", "Message", "Quest",
            "Stats", "Character Info", "Skill Tree", "Rune", "Inventory"
        };
        
        String[] buttonIds = {
            "settings", "world", "trade", "message", "quest",
            "stats", "character", "skilltree", "rune", "inventory"
        };
        
        for (int i = 0; i < buttonLabels.length; i++) {
            UIButton button = new UIButton(0, 0, buttonSize, buttonSize, buttonIds[i], buttonLabels[i]);
            
            String iconPath = "/ui/icons/" + buttonIds[i] + ".png";
            String iconHoverPath = "/ui/icons/" + buttonIds[i] + "_hover.png";
            String iconLockedPath = "/ui/icons/" + buttonIds[i] + "_locked.png";
            
            button.setIcons(iconPath, iconHoverPath, iconLockedPath);
            
            if (buttonIds[i].equals("inventory")) {
                button.setLocked(false);
                button.setVisible(true);
                button.setOnClick(() -> toggleInventoryAndGear());
            } else if (buttonIds[i].equals("rune")) {
                button.setLocked(true);
                button.setVisible(true);
                button.setOnClick(() -> toggleInventoryAndGear());
            } else {
                button.setLocked(true);
                button.setVisible(true);
            }
            
            verticalMenu.addChild(button);
        }
        
        panels.add(verticalMenu);
    }
    
    private void toggleInventoryAndGear() {
        if (inventoryPanel == null) {
            createInventoryPanel();
        }
        if (gearPanel == null) {
            createGearPanel();
        }
        
        boolean newVisibility = !inventoryPanel.isVisible();
        inventoryPanel.setVisible(newVisibility);
        gearPanel.setVisible(newVisibility);
        
        System.out.println("Inventory & Gear " + (newVisibility ? "opened" : "closed"));
    }
    
    /**
     * ⭐ REDESIGNED: Simpler structure with tabs added directly to main panel
     */
    private void createInventoryPanel() {
        int slotSize = 48;
        int columns = 5;
        int rows = 4;
        int gap = 4;
        int padding = 8;
        int tabBarHeight = 32;
        
        int panelWidth = (slotSize * columns) + (gap * (columns - 1)) + (padding * 2);
        int slotsHeight = (slotSize * rows) + (gap * (rows - 1)) + (padding * 2);
        int panelHeight = slotsHeight + tabBarHeight;
        
        UIButton settingsButton = getMenuButton("settings");
        int panelX;
        int panelY;
        
        if (settingsButton != null) {
            panelX = settingsButton.getX() - panelWidth - 10;
            int gearPanelHeight = 400;
            panelY = settingsButton.getY() + gearPanelHeight + 10;
        } else {
            panelX = Engine.WIDTH - panelWidth - 70;
            panelY = 100;
        }
        
        // ⭐ Main panel with NONE layout (manual positioning)
        inventoryPanel = new UIPanel(panelX, panelY, panelWidth, panelHeight);
        inventoryPanel.setLayout(UIPanel.LayoutType.NONE);
        inventoryPanel.setPadding(0);
        inventoryPanel.setBackgroundColor(new java.awt.Color(20, 20, 30, 230));
        inventoryPanel.setBorderColor(new java.awt.Color(100, 100, 120));
        inventoryPanel.setBorderWidth(2);
        
        // ⭐ Create tabs DIRECTLY in main panel (not nested)
        String[] tabNames = {"Misc", "Weap", "Arm", "Acc", "Rune"};
        int tabWidth = (panelWidth - (padding * 2) - (gap * 4)) / 5;
        
        inventoryTabs.clear();
        
        for (int i = 0; i < tabNames.length; i++) {
            int tabX = panelX + padding + (i * (tabWidth + gap));
            int tabY = panelY + padding;
            
            UIInventoryTab tab = new UIInventoryTab(tabX, tabY, tabWidth, tabBarHeight - (padding * 2), tabNames[i]);
            final String tabName = tabNames[i];
            tab.setOnClick(() -> switchInventoryTab(tabName));
            
            if (tabNames[i].equals(currentInventoryTab)) {
                tab.setActive(true);
            }
            
            inventoryTabs.add(tab);
            inventoryPanel.addChild(tab);  // ⭐ Add directly to main panel
        }
        
        // ⭐ Create inventory slots DIRECTLY in main panel
        int gridStartY = tabBarHeight;
        int slotStartX = panelX + padding;
        int slotStartY = panelY + gridStartY + padding;
        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int slotX = slotStartX + (col * (slotSize + gap));
                int slotY = slotStartY + (row * (slotSize + gap));
                int slotIndex = row * columns + col;
                
                UIInventorySlot slot = new UIInventorySlot(slotX, slotY, slotSize, slotIndex);
                inventoryPanel.addChild(slot);  // ⭐ Add directly to main panel
            }
        }
        
        panels.add(inventoryPanel);
        
        System.out.println("Inventory created: 5x4 grid (20 slots) with tabs");
        System.out.println("Position: (" + panelX + ", " + panelY + ") Size: " + panelWidth + "x" + panelHeight);
    }
    
    private void switchInventoryTab(String tabName) {
        currentInventoryTab = tabName;
        
        // Update tab visuals
        for (UIInventoryTab tab : inventoryTabs) {
            tab.setActive(tab.getTabName().equals(tabName));
        }
        
        System.out.println("Switched to tab: " + tabName);
    }
    
    private void createGearPanel() {
        int slotWidth = 48;
        int slotHeight = 48;
        int gap = 4;
        int padding = 8;
        
        int inventorySlotSize = 48;
        int inventoryColumns = 5;
        int inventoryGap = 4;
        int inventoryPadding = 8;
        int panelWidth = (inventorySlotSize * inventoryColumns) + (inventoryGap * (inventoryColumns - 1)) + (inventoryPadding * 2);
        
        int panelHeight = 400;
        
        UIButton settingsButton = getMenuButton("settings");
        int panelX;
        int panelY;
        
        if (settingsButton != null) {
            panelX = settingsButton.getX() - panelWidth - 10;
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
        
        int sideColumnWidth = slotWidth + padding;
        int centerWidth = panelWidth - (sideColumnWidth * 2) - (padding * 2);
        int slotSpacing = (panelHeight - (padding * 2) - (slotHeight * 6)) / 5;
        
        // Left column
        addGearSlotRelative(padding, padding + (slotHeight + slotSpacing) * 0, slotWidth, slotHeight, UIGearSlot.SlotType.HEAD);
        addGearSlotRelative(padding, padding + (slotHeight + slotSpacing) * 1, slotWidth, slotHeight, UIGearSlot.SlotType.TOP_ARMOR);
        addGearSlotRelative(padding, padding + (slotHeight + slotSpacing) * 2, slotWidth, slotHeight, UIGearSlot.SlotType.GLOVES);
        addGearSlotRelative(padding, padding + (slotHeight + slotSpacing) * 3, slotWidth, slotHeight, UIGearSlot.SlotType.BELT);
        addGearSlotRelative(padding, padding + (slotHeight + slotSpacing) * 4, slotWidth, slotHeight, UIGearSlot.SlotType.PANTS);
        addGearSlotRelative(padding, padding + (slotHeight + slotSpacing) * 5, slotWidth, slotHeight, UIGearSlot.SlotType.SHOES);
        
        // Center preview
        int centerX = padding + slotWidth + gap;
        addCharacterPreviewRelative(centerX, padding, centerWidth, panelHeight - (padding * 2));
        
        // Right column
        int rightOffset = panelWidth - padding - slotWidth;
        addGearSlotRelative(rightOffset, padding + (slotHeight + slotSpacing) * 0, slotWidth, slotHeight, UIGearSlot.SlotType.TIARA);
        addGearSlotRelative(rightOffset, padding + (slotHeight + slotSpacing) * 1, slotWidth, slotHeight, UIGearSlot.SlotType.EARRINGS);
        addGearSlotRelative(rightOffset, padding + (slotHeight + slotSpacing) * 2, slotWidth, slotHeight, UIGearSlot.SlotType.NECKLACE);
        addGearSlotRelative(rightOffset, padding + (slotHeight + slotSpacing) * 3, slotWidth, slotHeight, UIGearSlot.SlotType.BRACELET);
        addGearSlotRelative(rightOffset, padding + (slotHeight + slotSpacing) * 4, slotWidth, slotHeight, UIGearSlot.SlotType.RING_1);
        addGearSlotRelative(rightOffset, padding + (slotHeight + slotSpacing) * 5, slotWidth, slotHeight, UIGearSlot.SlotType.RING_2);
        
        addTestEquipment();
        panels.add(gearPanel);
        
        System.out.println("Gear panel created at: (" + panelX + ", " + panelY + ") - Width: " + panelWidth);
    }
    
    private void addGearSlotRelative(int relX, int relY, int width, int height, UIGearSlot.SlotType slotType) {
        int absoluteX = gearPanel.getX() + relX;
        int absoluteY = gearPanel.getY() + relY;
        UIGearSlot slot = new UIGearSlot(absoluteX, absoluteY, width, height, slotType);
        gearPanel.addChild(slot);
    }
    
    private void addCharacterPreviewRelative(int relX, int relY, int width, int height) {
        int absoluteX = gearPanel.getX() + relX;
        int absoluteY = gearPanel.getY() + relY;
        UIPanel previewPanel = new UIPanel(absoluteX, absoluteY, width, height);
        previewPanel.setBackgroundColor(new java.awt.Color(40, 40, 50, 150));
        previewPanel.setBorderColor(new java.awt.Color(80, 80, 90));
        previewPanel.setBorderWidth(1);
        gearPanel.addChild(previewPanel);
    }
    
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
    
    private void addExampleSkills() {
        Skill fireball = new Skill("fireball", "Fireball", "Launch a blazing fireball", 
            Skill.SkillType.ATTACK, 3.0f, 12, 1);
        Skill heal = new Skill("heal", "Heal", "Restore health", 
            Skill.SkillType.HEAL, 8.0f, 12, 1);
        Skill shield = new Skill("shield", "Shield", "Create a barrier", 
            Skill.SkillType.DEFENSE, 12.0f, 12, 3);
        Skill haste = new Skill("haste", "Haste", "Increase speed", 
            Skill.SkillType.BUFF, 20.0f, 12, 2);
        
        List<UIComponent> slots = skillBar.getChildren();
        if (slots.size() >= 4) {
            ((UISkillSlot)slots.get(0)).setSkill(fireball);
            ((UISkillSlot)slots.get(1)).setSkill(heal);
            ((UISkillSlot)slots.get(2)).setSkill(shield);
            ((UISkillSlot)slots.get(4)).setSkill(haste);
        }
    }
    
    public void update(float delta) {
        for (UIPanel panel : panels) {
            if (panel.isVisible()) {
                panel.update(delta);
            }
        }
        
        // ⭐ Update tabs separately since they're not in a child panel
        for (UIInventoryTab tab : inventoryTabs) {
            if (inventoryPanel != null && inventoryPanel.isVisible()) {
                tab.update(delta);
            }
        }
    }
    
    public void render(Graphics2D g) {
        for (UIPanel panel : panels) {
            if (panel.isVisible()) {
                panel.render(g);
            }
        }
    }
    
    public void handleMouseMove(int mouseX, int mouseY) {
        for (UIPanel panel : panels) {
            if (panel.isVisible()) {
                panel.handleMouseMove(mouseX, mouseY);
            }
        }
    }
    
    public boolean handleClick(int mouseX, int mouseY) {
        for (int i = panels.size() - 1; i >= 0; i--) {
            UIPanel panel = panels.get(i);
            if (!panel.isVisible()) continue;
            
            if (panel.handleClick(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }
    
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
    
    public void handleKeyPress(int keyCode) {
        if (skillBar != null) {
            List<UIComponent> slots = skillBar.getChildren();
            
            if (keyCode >= java.awt.event.KeyEvent.VK_1 && 
                keyCode <= java.awt.event.KeyEvent.VK_8) {
                int index = keyCode - java.awt.event.KeyEvent.VK_1;
                if (index < slots.size()) {
                    useSkillInSlot(index);
                }
            }
            
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
        
        if (keyCode == java.awt.event.KeyEvent.VK_I) {
            toggleInventoryAndGear();
        }
    }
    
    public void useSkillInSlot(int slotIndex) {
        UISkillSlot slot = getSkillSlot(slotIndex);
        if (slot != null && slot.getSkill() != null) {
            Skill skill = slot.getSkill();
            
            if (skill.isReady()) {
                if (gameLogic != null) {
                    Entity player = gameState.getPlayer();
                    gameLogic.useSkill(player, skill);
                } else {
                    skill.use();
                    System.out.println("Used skill: " + skill.getName());
                }
            }
        }
    }
    
    public boolean upgradeSkill(int slotIndex) {
        Entity player = gameState.getPlayer();
        SkillLevel skillLevel = player.getComponent(SkillLevel.class);
        UISkillSlot slot = getSkillSlot(slotIndex);
        
        if (slot == null || skillLevel == null) return false;
        
        Skill skill = slot.getSkill();
        if (skill == null) return false;
        
        if (!skill.canUpgrade()) {
            System.out.println("Skill is already max level!");
            return false;
        }
        
        int cost = skill.getUpgradeCost();
        
        if (!skillLevel.canAfford(cost)) {
            System.out.println("Not enough skill points!");
            return false;
        }
        
        skillLevel.spendPoints(cost);
        skill.upgrade();
        
        System.out.println("Upgraded " + skill.getName() + " to level " + skill.getSkillLevel());
        return true;
    }
    
    public void addPanel(UIPanel panel) {
        panels.add(panel);
    }
    
    public void removePanel(UIPanel panel) {
        panels.remove(panel);
    }
    
    public UIPanel getSkillBar() {
        return skillBar;
    }
    
    public UISkillSlot getSkillSlot(int index) {
        if (skillBar == null) return null;
        
        List<UIComponent> slots = skillBar.getChildren();
        if (index >= 0 && index < slots.size()) {
            return (UISkillSlot)slots.get(index);
        }
        return null;
    }
    
    public void equipSkill(Skill skill, int slotIndex) {
        UISkillSlot slot = getSkillSlot(slotIndex);
        if (slot != null) {
            slot.setSkill(skill);
        }
    }
    
    public void clearSkillSlot(int slotIndex) {
        UISkillSlot slot = getSkillSlot(slotIndex);
        if (slot != null) {
            slot.setSkill(null);
        }
    }
    
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
    
    public void unlockMenuButton(String id) {
        UIButton button = getMenuButton(id);
        if (button != null) {
            button.unlock();
            if (verticalMenu != null) {
                verticalMenu.relayout();
            }
            System.out.println("Unlocked: " + button.getLabel());
        }
    }
    
    public void lockMenuButton(String id) {
        UIButton button = getMenuButton(id);
        if (button != null) {
            button.lock();
            if (verticalMenu != null) {
                verticalMenu.relayout();
            }
            System.out.println("Locked: " + button.getLabel());
        }
    }
    
    public UIPanel getInventoryPanel() {
        return inventoryPanel;
    }
    
    public UIInventorySlot getInventorySlot(int index) {
        if (inventoryPanel == null) return null;
        
        for (UIComponent component : inventoryPanel.getChildren()) {
            if (component instanceof UIInventorySlot) {
                UIInventorySlot slot = (UIInventorySlot) component;
                if (slot.getSlotIndex() == index) {
                    return slot;
                }
            }
        }
        return null;
    }
    
    public boolean addItemToInventory(Object item) {
        if (inventoryPanel == null) {
            createInventoryPanel();
        }
        
        for (UIComponent component : inventoryPanel.getChildren()) {
            if (component instanceof UIInventorySlot) {
                UIInventorySlot slot = (UIInventorySlot) component;
                if (slot.isEmpty()) {
                    slot.setItem(item);
                    System.out.println("Added item to slot " + slot.getSlotIndex());
                    return true;
                }
            }
        }
        
        System.out.println("Inventory full!");
        return false;
    }
    
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
    
    public boolean equipItem(UIGearSlot.SlotType slotType, Object item) {
        UIGearSlot slot = getGearSlot(slotType);
        if (slot != null) {
            slot.equipItem(item);
            return true;
        }
        return false;
    }
}