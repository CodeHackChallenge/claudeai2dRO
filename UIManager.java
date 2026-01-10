package dev.main;

import java.awt.Graphics2D;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

/**
 * UI Manager with fixed layout:
 * Top: [Left Gear] [Hero Preview] [Right Gear]
 * Middle: [Inventory Tabs]
 * Bottom: [5x10 Scrollable Inventory Grid]
 */
public class UIManager implements MouseWheelListener {
    
    private List<UIPanel> panels;
    private UIComponent hoveredComponent;
    private GameState gameState;
    private GameLogic gameLogic;
    
    private UIPanel skillBar;
    private UIPanel verticalMenu;
    private UIPanel inventoryContainer;
    private UIScrollableInventoryPanel inventoryGrid;
    private UIPanel heroPreviewPanel;
    
    private String currentInventoryTab = "Misc";
    private List<UIInventoryTab> inventoryTabs;
    
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
                button.setOnClick(() -> toggleInventory());
            } else if (buttonIds[i].equals("rune")) {
                button.setLocked(true);
                button.setVisible(true);
                button.setOnClick(() -> toggleInventory());
            } else {
                button.setLocked(true);
                button.setVisible(true);
            }
            
            verticalMenu.addChild(button);
        }
        
        panels.add(verticalMenu);
    }
    
    private void toggleInventory() {
        if (inventoryContainer == null) {
            createInventorySystem();
        }
        
        boolean newVisibility = !inventoryContainer.isVisible();
        inventoryContainer.setVisible(newVisibility);
        
        System.out.println("Inventory " + (newVisibility ? "opened" : "closed"));
    }
    
    /**
     * Create inventory system with tabs ABOVE the inventory grid
     */
    private void createInventorySystem() {
        int slotSize = 48;
        int gap = 4;
        int padding = 8;
        int tabHeight = 28;
        
        int columnWidth = slotSize;
        
        // Inventory grid: 5 columns, 10 total rows, 4 visible rows
        int inventoryColumns = 5;
        int inventoryTotalRows = 10;
        int inventoryVisibleRows = 4;
        
        // Total width = 5 columns (inventory width)
        int totalWidth = (columnWidth * inventoryColumns) + (gap * (inventoryColumns - 1)) + (padding * 2) + 12;
        
        // Top section: gear slots + hero preview
        int gearColumnHeight = (slotSize * 4) + (gap * 3);
        int heroPreviewHeight = gearColumnHeight;
        
        // Middle section: tabs
        int tabSectionHeight = tabHeight + gap;
        
        // Bottom section: inventory
        int inventoryGridHeight = (slotSize * inventoryVisibleRows) + (gap * (inventoryVisibleRows - 1)) + (padding * 2);
        
        // Total height = top section + tabs + inventory + padding
        int totalHeight = gearColumnHeight + tabSectionHeight + inventoryGridHeight + (padding * 3);
        
        // Position
        UIButton settingsButton = getMenuButton("settings");
        int containerX;
        int containerY;
        
        if (settingsButton != null) {
            containerX = settingsButton.getX() - totalWidth - 10;
            containerY = settingsButton.getY();
        } else {
            containerX = Engine.WIDTH - totalWidth - 70;
            containerY = 100;
        }
        
        // Main container
        inventoryContainer = new UIPanel(containerX, containerY, totalWidth, totalHeight);
        inventoryContainer.setLayout(UIPanel.LayoutType.NONE);
        inventoryContainer.setPadding(0);
        inventoryContainer.setBackgroundColor(new java.awt.Color(20, 20, 30, 230));
        inventoryContainer.setBorderColor(new java.awt.Color(100, 100, 120));
        inventoryContainer.setBorderWidth(2);
        
        int currentY = containerY + padding;
        
        // TOP SECTION - Calculate hero preview width and gear columns
        // Left and right gear columns use `columnWidth`; hero preview takes remaining center space
        int heroWidth = totalWidth - (padding * 2) - (columnWidth * 2) - (gap * 2);
        
        // LEFT GEAR COLUMN
        int leftGearX = containerX + padding;
        int leftGearY = currentY;
        
        addGearSlotToContainer(leftGearX, leftGearY + (slotSize + gap) * 0, columnWidth, slotSize, UIGearSlot.SlotType.EARRINGS);
        addGearSlotToContainer(leftGearX, leftGearY + (slotSize + gap) * 1, columnWidth, slotSize, UIGearSlot.SlotType.TOP_ARMOR);
        addGearSlotToContainer(leftGearX, leftGearY + (slotSize + gap) * 2, columnWidth, slotSize, UIGearSlot.SlotType.PANTS);
        addGearSlotToContainer(leftGearX, leftGearY + (slotSize + gap) * 3, columnWidth, slotSize, UIGearSlot.SlotType.HEAD);
        
        // CENTER HERO PREVIEW
        int heroX = leftGearX + columnWidth + gap;
        int heroY = currentY;
        
        heroPreviewPanel = new UIPanel(heroX, heroY, heroWidth, heroPreviewHeight);
        heroPreviewPanel.setBackgroundColor(new java.awt.Color(30, 30, 40, 180));
        heroPreviewPanel.setBorderColor(new java.awt.Color(80, 80, 100));
        heroPreviewPanel.setBorderWidth(1);
        inventoryContainer.addChild(heroPreviewPanel);
        
        // RIGHT GEAR COLUMN (flush to right edge)
        int rightGearX = containerX + totalWidth - padding - columnWidth;
        int rightGearY = currentY;
        
        addGearSlotToContainer(rightGearX, rightGearY + (slotSize + gap) * 0, columnWidth, slotSize, UIGearSlot.SlotType.TIARA);
        addGearSlotToContainer(rightGearX, rightGearY + (slotSize + gap) * 1, columnWidth, slotSize, UIGearSlot.SlotType.GLOVES);
        addGearSlotToContainer(rightGearX, rightGearY + (slotSize + gap) * 2, columnWidth, slotSize, UIGearSlot.SlotType.SHOES);
        addGearSlotToContainer(rightGearX, rightGearY + (slotSize + gap) * 3, columnWidth, slotSize, UIGearSlot.SlotType.RING_1);
        
        // Move to next section (tabs)
        currentY += gearColumnHeight + padding;
        
        // MIDDLE SECTION - TABS (above inventory)
        createInventoryTabs(containerX, currentY, totalWidth, padding, gap);
        
        // Move to next section (inventory)
        currentY += tabSectionHeight;
        
        // BOTTOM SECTION - SCROLLABLE INVENTORY GRID
        int inventoryGridX = containerX + padding;
        
        inventoryGrid = new UIScrollableInventoryPanel(
            inventoryGridX, currentY,
            totalWidth - (padding * 2), inventoryGridHeight,
            inventoryColumns, inventoryTotalRows, inventoryVisibleRows
        );
        inventoryContainer.addChild(inventoryGrid);
        // Ensure the inventory grid shows the currently selected tab
        inventoryGrid.switchToTab(currentInventoryTab);
        
        panels.add(inventoryContainer);
        
        System.out.println("Inventory system created:");
        System.out.println("  Total size: " + totalWidth + "x" + totalHeight);
        System.out.println("  Layout: [Gear] -> [Tabs] -> [Inventory]");
        System.out.println("  Inventory: 5x10 grid (50 total slots, 4 visible rows)");
    }
    
    private void createInventoryTabs(int containerX, int containerY, int containerWidth, int padding, int gap) {
        String[] tabNames = {"Misc", "Weap", "Arm", "Acc", "Rune"};
        int tabWidth = (containerWidth - (padding * 2) - (gap * 4)) / 5;
        int tabHeight = 24;
        
        inventoryTabs.clear();
        
        for (int i = 0; i < tabNames.length; i++) {
            int tabX = containerX + padding + (i * (tabWidth + gap));
            int tabY = containerY;
            
            UIInventoryTab tab = new UIInventoryTab(tabX, tabY, tabWidth, tabHeight, tabNames[i]);
            final String tabName = tabNames[i];
            tab.setOnClick(() -> switchInventoryTab(tabName));
            
            if (tabNames[i].equals(currentInventoryTab)) {
                tab.setActive(true);
            }
            
            inventoryTabs.add(tab);
            inventoryContainer.addChild(tab);
        }
    }
    
    private void addGearSlotToContainer(int x, int y, int width, int height, UIGearSlot.SlotType slotType) {
        UIGearSlot slot = new UIGearSlot(x, y, width, height, slotType);
        inventoryContainer.addChild(slot);
    }
    
    private void switchInventoryTab(String tabName) {
        currentInventoryTab = tabName;
        
        for (UIInventoryTab tab : inventoryTabs) {
            tab.setActive(tab.getTabName().equals(tabName));
        }
        
        System.out.println("Switched to tab: " + tabName);
        // Update inventory grid to show the selected tab
        if (inventoryGrid != null) {
            inventoryGrid.switchToTab(tabName);
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
        
        // Update tabs
        for (UIInventoryTab tab : inventoryTabs) {
            if (inventoryContainer != null && inventoryContainer.isVisible()) {
                tab.update(delta);
            }
        }
        
        // Update scrollable inventory
        if (inventoryGrid != null && inventoryContainer != null && inventoryContainer.isVisible()) {
            inventoryGrid.update(delta);
        }
    }
    
    public void render(Graphics2D g) {
        for (UIPanel panel : panels) {
            if (panel.isVisible()) {
                panel.render(g);
            }
        }
    }
    
    // Updated to accept pressed state for drag handling
    public void handleMouseMove(int mouseX, int mouseY, boolean pressed) {
        for (UIPanel panel : panels) {
            if (panel.isVisible()) {
                panel.handleMouseMove(mouseX, mouseY, pressed);
            }
        }
        
        // Handle inventory grid hover/drag
        if (inventoryGrid != null && inventoryContainer != null && inventoryContainer.isVisible()) {
            inventoryGrid.handleMouseMove(mouseX, mouseY, pressed);
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
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (inventoryGrid != null && inventoryContainer != null && inventoryContainer.isVisible()) {
            int mouseX = e.getX();
            int mouseY = e.getY();
            
            if (inventoryGrid.contains(mouseX, mouseY)) {
                inventoryGrid.handleScroll(e.getWheelRotation());
            }
        }
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
            toggleInventory();
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
        return inventoryContainer;
    }
    
    public UIInventorySlot getInventorySlot(int index) {
        if (inventoryGrid == null) return null;
        return inventoryGrid.getSlot(index);
    }
    
    public boolean addItemToInventory(Object item) {
        if (inventoryContainer == null) {
            createInventorySystem();
        }
        
        if (inventoryGrid != null) {
            boolean added = inventoryGrid.addItemToCurrentTab(item);
            if (added) {
                System.out.println("Added item to inventory (tab=" + currentInventoryTab + ")");
                return true;
            }
        }

        System.out.println("Inventory full!");
        return false;
    }
    
    public UIGearSlot getGearSlot(UIGearSlot.SlotType slotType) {
        if (inventoryContainer == null) return null;
        
        for (UIComponent component : inventoryContainer.getChildren()) {
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
    
    public UIPanel getHeroPreviewPanel() {
        return heroPreviewPanel;
    }
}