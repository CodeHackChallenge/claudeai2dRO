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
     
	private UIDialogueBoxEnhanced enhancedDialogueBox;
	
	private static final String[] TAB_NAMES = {"Misc", "Weap", "Arm", "Acc", "Rune"};
	
	private UIDialogueBox dialogueBox;
	private UIQuestPanel questPanel;
	
    private List<UIPanel> panels;
    private UIComponent hoveredComponent;
    private GameState gameState;
    private GameLogic gameLogic;
    
    private UIPanel skillBar;
    private UIPanel verticalMenu;
    private UIPanel inventoryContainer;
    private UIScrollableInventoryPanel inventoryGrid;
    private UIPanel heroPreviewPanel;
    private UITooltipPanel tooltipPanel;
    
    private String currentInventoryTab = "Misc";
    private List<UIInventoryTab> inventoryTabs;
    
    private UIStatsPanel statsPanel;
    private boolean isStatsVisible = false;
    
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
         createDialogueBox();
         createEnhancedDialogueBox();  // NEW
         createStatsPanel();
    }
    
    private void createEnhancedDialogueBox() {
         int width = 600;
         int height = 450;
         int x = (Engine.WIDTH - width) / 2;
         int y = (Engine.HEIGHT - height) / 2;
         
         enhancedDialogueBox = new UIDialogueBoxEnhanced(x, y, width, height);
         enhancedDialogueBox.setVisible(false);
         enhancedDialogueBox.setPlayer(gameState.getPlayer());
         
         enhancedDialogueBox.setOnClose(() -> {
             System.out.println("Dialogue closed");
         });
    }
   
    public UIDialogueBoxEnhanced getEnhancedDialogueBox() {
         return enhancedDialogueBox;
    }
    
    private void createStatsPanel() {
        int width = 300;
        int height = 400;
        int menuX = Engine.WIDTH - 58;  // From vertical menu
        int x = menuX - width - 10;
        int y = 50;
        statsPanel = new UIStatsPanel(x, y, width, height, gameState);
    }
    
    /**
     * ⭐ NEW: Create dialogue box (hidden by default)
     */
    private void createDialogueBox() {
        int width = 500;
        int height = 400;
        int x = (Engine.WIDTH - width) / 2;
        int y = (Engine.HEIGHT - height) / 2;
        
        dialogueBox = new UIDialogueBox(x, y, width, height);
        dialogueBox.setVisible(false);
        
        // Set callbacks
        dialogueBox.setOnAccept(() -> handleQuestAccept());
        dialogueBox.setOnDecline(() -> handleQuestDecline());
        dialogueBox.setOnClose(() -> handleDialogueClose());
        
        // Don't add to panels list yet - we'll handle it separately
    }
    /**
     * ⭐ NEW: Show dialogue with NPC
     */
    public void showDialogue(String npcName, String dialogue) {
        dialogueBox.showDialogue(npcName, dialogue);
    }
    /**
     * ⭐ NEW: Show quest offer from NPC
     */
    public void showQuestOffer(String npcName, Quest quest) {
        dialogueBox.showQuestOffer(npcName, quest);
    }

    /**
     * ⭐ NEW: Show quest completion
     */
    public void showQuestComplete(String npcName, Quest quest) {
        dialogueBox.showQuestComplete(npcName, quest);
    }

    /**
     * Show a confirmation dialog. Callback runs on confirm/cancel.
     */
    public void showConfirmation(String title, String message, String confirmLabel, String cancelLabel,
                                 Runnable onConfirm, Runnable onCancel) {
        dialogueBox.showConfirmation(title, message, confirmLabel, cancelLabel, onConfirm, onCancel);
    }

    /**
     * ⭐ NEW: Handle quest acceptance
     */
    private void handleQuestAccept() {
        Quest quest = dialogueBox.getOfferedQuest();
        if (quest != null) {
            quest.accept();
            System.out.println("Quest accepted: " + quest.getName());
            
            // Add quest to player's quest log
            Entity player = gameState.getPlayer();
            QuestLog questLog = player.getComponent(QuestLog.class);
            if (questLog != null) {
                questLog.addQuest(quest);
            }
            
            // ⭐ NEW: Unlock quest button on first quest
            unlockQuestButton();
            
            // ⭐ NEW: Update quest indicator
            updateQuestIndicator();
        }
        
        dialogueBox.close();
    }
    /**
     * ⭐ NEW: Handle quest decline
     */
    private void handleQuestDecline() {
        Quest quest = dialogueBox.getOfferedQuest();
        if (quest != null) {
            System.out.println("Quest declined: " + quest.getName());
        }
        
        dialogueBox.close();
    }

    /**
     * ⭐ NEW: Handle dialogue close
     */
    private void handleDialogueClose() {
        Quest quest = dialogueBox.getOfferedQuest();
        
        // If closing a completed quest, claim rewards
        if (quest != null && quest.isCompleted()) {
            Entity player = gameState.getPlayer();
            quest.claimRewards(player);
            
            // Award XP
            Experience exp = player.getComponent(Experience.class);
            if (exp != null && quest.getExpReward() > 0) {
                int levelsGained = exp.addExperience(quest.getExpReward());
                
                if (levelsGained > 0) {
                    Stats stats = player.getComponent(Stats.class);
                    if (stats != null) {
                        stats.applyLevelStats(exp, true);
                    }
                }
            }
            
            // ⭐ NEW: Remove from quest log and update indicator
            QuestLog questLog = player.getComponent(QuestLog.class);
            if (questLog != null) {
                questLog.completeQuest(quest.getId());
            }
            
            updateQuestIndicator();
            
            System.out.println("Quest rewards claimed!");
        }
        
        dialogueBox.close();
    }
    /**
     * ⭐ NEW: Get dialogue box
     */
    public UIDialogueBox getDialogueBox() {
        return dialogueBox;
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
            
            // ⭐ Special button handlers
            if (buttonIds[i].equals("inventory")) {
                button.setLocked(false);
                button.setVisible(true);
                button.setOnClick(() -> toggleInventory());
            } else if (buttonIds[i].equals("rune")) {
                button.setLocked(true);
                button.setVisible(true);
                button.setOnClick(() -> toggleInventory());
            } 
            // ⭐ NEW: Quest button handler
            else if (buttonIds[i].equals("quest")) {
                button.setLocked(true);  // Initially locked
                button.setVisible(true);
                button.setOnClick(() -> toggleQuestPanel());
            } 
            // Stats button handler
            else if (buttonIds[i].equals("stats")) {
                button.setLocked(true);  // Initially locked
                button.setVisible(true);
                button.setOnClick(() -> toggleStatsPanel());
            }
            else {
                button.setLocked(true);
                button.setVisible(true);
            }
            
            verticalMenu.addChild(button);
        }
        
        panels.add(verticalMenu);
    }
    /**
     * Toggle quest panel visibility
     */
    private void toggleQuestPanel() {
        if (questPanel == null) {
            createQuestPanel();
        }
        
        boolean newVisibility = !questPanel.isVisible();
        questPanel.setVisible(newVisibility);
        
        if (newVisibility) {
            questPanel.refreshQuestList();
        }
        
        System.out.println("Quest Panel " + (newVisibility ? "opened" : "closed"));
    }
    
    /**
     * Toggle stats panel visibility
     */
    public void toggleStatsPanel() {
        isStatsVisible = !isStatsVisible;
        statsPanel.setVisible(isStatsVisible);
        System.out.println("Stats Panel " + (isStatsVisible ? "opened" : "closed"));
    }
    /**
     * Create quest panel
     */
    private void createQuestPanel() {
        int width = 700;
        int height = 500;
        
        // Position next to settings button
        UIButton settingsButton = getMenuButton("settings");
        int panelX;
        int panelY;
        
        if (settingsButton != null) {
            panelX = settingsButton.getX() - width - 10;
            panelY = settingsButton.getY();
        } else {
            panelX = Engine.WIDTH - width - 70;
            panelY = 100;
        }
        
        questPanel = new UIQuestPanel(panelX, panelY, width, height, gameState);
        questPanel.setVisible(false);
        
        // Don't add to panels list - handle separately like dialogueBox
        
        System.out.println("Quest panel created");
    }

    /**
     * ⭐ NEW: Unlock quest button when first quest is accepted
     */
    public void unlockQuestButton() {
        UIButton questButton = getMenuButton("quest");
        if (questButton != null && questButton.isLocked()) {
            questButton.unlock();
            if (verticalMenu != null) {
                verticalMenu.relayout();
            }
            System.out.println("Quest button unlocked!");
        }
    }

    /**
     * ⭐ NEW: Show quest indicator (called when quest is accepted/updated/completed)
     */
    public void updateQuestIndicator() {
        // This could add a visual indicator on the quest button
        // For now, just refresh the panel if it's open
        if (questPanel != null && questPanel.isVisible()) {
            questPanel.refreshQuestList();
        }
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
        int gearColumnHeight = (slotSize * 6) + (gap * 4);
        int heroPreviewHeight = gearColumnHeight + 6;
        
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
        
        // ⭐ START BATCH - Add all children at once
        inventoryContainer.beginBatch();
        
        // LEFT GEAR COLUMN
        int leftGearX = containerX + padding;
        int leftGearY = currentY;
        
        addGearSlotToContainer(leftGearX, leftGearY + (slotSize + gap) * 0, columnWidth, slotSize, UIGearSlot.SlotType.HEAD);
        addGearSlotToContainer(leftGearX, leftGearY + (slotSize + gap) * 1, columnWidth, slotSize, UIGearSlot.SlotType.TOP_ARMOR);
        addGearSlotToContainer(leftGearX, leftGearY + (slotSize + gap) * 2, columnWidth, slotSize, UIGearSlot.SlotType.PANTS);
        addGearSlotToContainer(leftGearX, leftGearY + (slotSize + gap) * 3, columnWidth, slotSize, UIGearSlot.SlotType.GLOVES);
        addGearSlotToContainer(leftGearX, leftGearY + (slotSize + gap) * 4, columnWidth, slotSize, UIGearSlot.SlotType.SHOES);
        addGearSlotToContainer(leftGearX, leftGearY + (slotSize + gap) * 5, columnWidth, slotSize, UIGearSlot.SlotType.WEAPON);
        
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
        
        addGearSlotToContainer(rightGearX, rightGearY + (slotSize + gap) * 0, columnWidth, slotSize, UIGearSlot.SlotType.EARRINGS);
        addGearSlotToContainer(rightGearX, rightGearY + (slotSize + gap) * 1, columnWidth, slotSize, UIGearSlot.SlotType.NECKLACE);
        addGearSlotToContainer(rightGearX, rightGearY + (slotSize + gap) * 2, columnWidth, slotSize, UIGearSlot.SlotType.BRACELET);
        addGearSlotToContainer(rightGearX, rightGearY + (slotSize + gap) * 3, columnWidth, slotSize, UIGearSlot.SlotType.RING_1);
        addGearSlotToContainer(rightGearX, rightGearY + (slotSize + gap) * 4, columnWidth, slotSize, UIGearSlot.SlotType.RING_2);
        addGearSlotToContainer(rightGearX, rightGearY + (slotSize + gap) * 5, columnWidth, slotSize, UIGearSlot.SlotType.SPECIAL);
        
        
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
            inventoryColumns, inventoryTotalRows, inventoryVisibleRows,
            this
        );
        inventoryContainer.addChild(inventoryGrid);
        // Ensure the inventory grid shows the currently selected tab
        inventoryGrid.switchToTab(currentInventoryTab);
        
        // ⭐ END BATCH - Single relayout for all components
        inventoryContainer.endBatch();

        panels.add(inventoryContainer);
        
        System.out.println("Inventory system created:");
        System.out.println("  Total size: " + totalWidth + "x" + totalHeight);
        System.out.println("  Layout: [Gear] -> [Tabs] -> [Inventory]");
        System.out.println("  Inventory: 5x10 grid (50 total slots, 4 visible rows)");
    }
    
    private void createInventoryTabs(int containerX, int containerY, int containerWidth, int padding, int gap) {
        //String[] tabNames = {"Misc", "Weap", "Arm", "Acc", "Rune"};
        int tabWidth = (containerWidth - (padding * 2) - (gap * 4)) / 5;
        int tabHeight = 24;
        
        inventoryTabs.clear();
        
        for (int i = 0; i < TAB_NAMES.length; i++) {
            int tabX = containerX + padding + (i * (tabWidth + gap));
            int tabY = containerY;
            
            UIInventoryTab tab = new UIInventoryTab(tabX, tabY, tabWidth, tabHeight, TAB_NAMES[i]);
            final String tabName = TAB_NAMES[i];
            tab.setOnClick(() -> switchInventoryTab(tabName));
            
            if (TAB_NAMES[i].equals(currentInventoryTab)) {
                tab.setActive(true);
            }
            
            inventoryTabs.add(tab);
            inventoryContainer.addChild(tab);
        }
    }
    
    private void addGearSlotToContainer(int x, int y, int width, int height, UIGearSlot.SlotType slotType) {
        UIGearSlot slot = new UIGearSlot(x, y, width, height, slotType, this);
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
        
        // ⭐ NEW: Update dialogue box
        if (dialogueBox != null && dialogueBox.isVisible()) {
            dialogueBox.update(delta);
        }
        // ⭐ NEW: Update quest panel
        if (questPanel != null && questPanel.isVisible()) {
            questPanel.update(delta);
        }
    }
    
    public void render(Graphics2D g) {
        for (UIPanel panel : panels) {
            if (panel.isVisible()) {
                panel.render(g);
            }
        }
        
        // ⭐ NEW: Render quest panel (after regular panels, before dialogue)
        if (questPanel != null && questPanel.isVisible()) {
            questPanel.render(g);
        }
        
        // Render stats panel
        if (statsPanel != null && statsPanel.isVisible()) {
            statsPanel.render(g);
        }
        
        // ⭐ Render dialogue box on top of everything
        if (dialogueBox != null && dialogueBox.isVisible()) {
            dialogueBox.render(g);
        }
        // NEW: Render enhanced dialogue box
        if (enhancedDialogueBox != null && enhancedDialogueBox.isVisible()) {
              enhancedDialogueBox.render(g);
        }
        
        // Render tooltip on top of everything
        if (tooltipPanel != null && tooltipPanel.isVisible()) {
            tooltipPanel.render(g);
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
        
        // Handle enhanced dialogue box hover first (on-top)
        if (enhancedDialogueBox != null && enhancedDialogueBox.isVisible()) {
            enhancedDialogueBox.handleMouseMove(mouseX, mouseY);
            return; // enhanced box is on top; consumed for hover
        }

        // Handle legacy dialogue box hover
        if (dialogueBox != null && dialogueBox.isVisible()) {
            dialogueBox.handleMouseMove(mouseX, mouseY);
        }
        
        // ⭐ NEW: Handle quest panel hover
        if (questPanel != null && questPanel.isVisible()) {
            questPanel.handleMouseMove(mouseX, mouseY);
        }
        
        // Handle tooltips
        updateTooltips(mouseX, mouseY);
    }
    
    private void updateTooltips(int mouseX, int mouseY) {
        String tooltipText = null;
        
        // Check inventory slots
        if (inventoryGrid != null && inventoryContainer != null && inventoryContainer.isVisible()) {
            if (inventoryContainer.contains(mouseX, mouseY)) {
                UIInventorySlot slot = inventoryGrid.getHoveredSlot(mouseX, mouseY);
                if (slot != null) {
                    tooltipText = slot.getTooltipText();
                }
            }
        }
        
        // Check gear slots
        for (UIPanel panel : panels) {
            if (panel.isVisible() && panel.contains(mouseX, mouseY)) {
                // Check if it's a gear slot container
                for (UIComponent child : panel.getChildren()) {
                    if (child instanceof UIGearSlot && child.contains(mouseX, mouseY)) {
                        tooltipText = child.getTooltipText();
                        break;
                    }
                }
                if (tooltipText != null) break;
            }
        }
        
        if (tooltipText != null) {
            showTooltip(tooltipText, mouseX, mouseY);
        } else {
            hideTooltip();
        }
    }
    
    public boolean handleClick(int mouseX, int mouseY) {
    	 // NEW: Check enhanced dialogue box first
    	 if (enhancedDialogueBox != null && enhancedDialogueBox.isVisible()) {
    	     if (enhancedDialogueBox.handleClick(mouseX, mouseY)) {
    	             return true;
    	     }
    	 }
        // Check dialogue box first (highest priority)
        if (dialogueBox != null && dialogueBox.isVisible()) {
            if (dialogueBox.handleClick(mouseX, mouseY)) {
                return true;
            }
        }
        
        // ⭐ NEW: Check quest panel
        if (questPanel != null && questPanel.isVisible()) {
            if (questPanel.handleClick(mouseX, mouseY)) {
                return true;
            }
        }
        
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
        
        // ⭐ NEW: Quest panel scroll
        if (questPanel != null && questPanel.isVisible()) {
            int mouseX = e.getX();
            int mouseY = e.getY();
            
            if (questPanel.contains(mouseX, mouseY)) {
                questPanel.handleScroll(e.getWheelRotation());
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
        
        // Inventory toggle
        if (keyCode == java.awt.event.KeyEvent.VK_I) {
            toggleInventory();
        }
        
        // ⭐ NEW: Quest panel toggle (J key)
        if (keyCode == java.awt.event.KeyEvent.VK_J) {
            // Check if quest button is unlocked
            UIButton questButton = getMenuButton("quest");
            if (questButton != null && !questButton.isLocked()) {
                toggleQuestPanel();
            }
        }
        
        // Stats panel toggle (S key)
        if (keyCode == java.awt.event.KeyEvent.VK_S) {
            UIButton statsButton = getMenuButton("stats");
            if (statsButton != null && !statsButton.isLocked()) {
                toggleStatsPanel();
            }
        }
        
        // Close UI panels one at a time (ESC key)
        if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
            // Close dialogue boxes first (highest priority)
            if (enhancedDialogueBox != null && enhancedDialogueBox.isVisible()) {
                enhancedDialogueBox.setVisible(false);
            } else if (dialogueBox != null && dialogueBox.isVisible()) {
                dialogueBox.setVisible(false);
            }
            // Then stats panel
            else if (isStatsVisible) {
                toggleStatsPanel();
            }
            // Then quest panel
            else if (questPanel != null && questPanel.isVisible()) {
                toggleQuestPanel();
            }
            // Then inventory
            else if (inventoryContainer != null && inventoryContainer.isVisible()) {
                toggleInventory();
            }
            // Finally hide tooltip
            else {
                hideTooltip();
            }
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
    
    public UIScrollableInventoryPanel getInventoryGrid() {
        return inventoryGrid;
    }
    
    public UIInventorySlot getInventorySlot(int index) {
        if (inventoryGrid == null) return null;
        return inventoryGrid.getSlot(index);
    }
    
    public boolean addItemToInventory(Item item) {
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
    
    public boolean equipItem(UIGearSlot.SlotType slotType, Item item) {
        UIGearSlot slot = getGearSlot(slotType);
        if (slot == null) return false;
        Item oldItem = slot.equipItem(item);
        if (oldItem != null) {
            addItemToInventory(oldItem);
            applyItemStats(oldItem, false);
        }
        if (item != null) {
            applyItemStats(item, true);
        }
        return true;
    }
    
    public Item unequipItem(UIGearSlot.SlotType slotType) {
        UIGearSlot slot = getGearSlot(slotType);
        if (slot == null) return null;
        Item item = slot.unequipItem();
        if (item != null) {
            addItemToInventory(item);
            applyItemStats(item, false);
        }
        return item;
    }
    
    private void applyItemStats(Item item, boolean add) {
        if (item == null) return;
        Entity player = gameState.getPlayer();
        Stats stats = player.getComponent(Stats.class);
        if (stats == null) return;
        int multiplier = add ? 1 : -1;
        stats.attack += multiplier * item.getAttackBonus();
        stats.defense += multiplier * item.getDefenseBonus();
        stats.magicAttack += multiplier * item.getMagicAttackBonus();
        stats.magicDefense += multiplier * item.getMagicDefenseBonus();
    }
    
    public UIPanel getHeroPreviewPanel() {
        return heroPreviewPanel;
    }
    
    // Tooltip methods
    public void showTooltip(String text, int mouseX, int mouseY) {
        if (tooltipPanel != null) {
            panels.remove(tooltipPanel);
        }
        
        tooltipPanel = UIExamples.createTooltip(text, mouseX, mouseY);
        panels.add(tooltipPanel);
    }
    
    public void hideTooltip() {
        if (tooltipPanel != null) {
            panels.remove(tooltipPanel);
            tooltipPanel = null;
        }
    }
}