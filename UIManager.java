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
    private GameLogic gameLogic;  // NEW: Reference to game logic
    
    // Skill bar
    private UIPanel skillBar;
    
    public UIManager(GameState gameState) {
        this.gameState = gameState;
        this.gameLogic = null;  // Set later via setGameLogic()
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
        // Create skill bar at bottom center of screen
        createSkillBar();
    }
    
    /**
     * Create the skill bar with 8 skill slots
     */
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
            slot.setMargin(0);  // No margin needed with gap
            
            // ☆ NEW: Set UI manager reference and slot index
            slot.setUIManager(this, i);
            
            skillBar.addChild(slot);
        }
        
        // Add some example skills for testing
        addExampleSkills();
        
        panels.add(skillBar);
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
            3.0f,   // 3 second cooldown
            12,     // ☆ 12% of max mana base cost
            1       // Level 1 required
        );
        
        Skill heal = new Skill(
            "heal",
            "Heal",
            "Restore health over time",
            Skill.SkillType.HEAL,
            8.0f,
            12,     // ☆ 12% of max mana base cost
            1
        ); 
        
        Skill shield = new Skill(
            "shield",
            "Shield",
            "Create a protective barrier",
            Skill.SkillType.DEFENSE,
            12.0f,
            12,     // ☆ 12% of max mana base cost
            3
        );
        
        Skill haste = new Skill(
            "haste",
            "Haste",
            "Increase movement speed",
            Skill.SkillType.BUFF,
            20.0f,
            12,     // ☆ 12% of max mana base cost
            2
        );
        
        // Assign skills to slots
        List<UIComponent> slots = skillBar.getChildren();
        if (slots.size() >= 4) {
            ((UISkillSlot)slots.get(0)).setSkill(fireball);
            ((UISkillSlot)slots.get(1)).setSkill(heal);
            ((UISkillSlot)slots.get(2)).setSkill(shield);
            ((UISkillSlot)slots.get(4)).setSkill(haste);  // Q key
        }
    }
    
    /**
     * Update all UI components
     */
    public void update(float delta) {
        for (UIPanel panel : panels) {
            panel.update(delta);
        }
    }
    
    /**
     * Render all UI panels
     */
    public void render(Graphics2D g) {
        for (UIPanel panel : panels) {
            panel.render(g);
        }
    }
    
    /**
     * Handle mouse movement
     */
    public void handleMouseMove(int mouseX, int mouseY) {
        for (UIPanel panel : panels) {
            panel.handleMouseMove(mouseX, mouseY);
        }
    }
    
    /**
     * Handle mouse click
     * @return true if UI consumed the click (don't pass to world)
     */
    public boolean handleClick(int mouseX, int mouseY) {
        // Check panels in reverse order (top-most first)
        for (int i = panels.size() - 1; i >= 0; i--) {
            UIPanel panel = panels.get(i);
            if (!panel.isVisible()) continue;
            
            if (panel.handleClick(mouseX, mouseY)) {  
            	
                return true;  // UI consumed the click
            }
        }
        
        return false;  // Click not on any UI
    }
    
    /**
     * Handle right click
     * @return true if UI consumed the click (don't pass to world)
     */
    public boolean handleRightClick(int mouseX, int mouseY) {
        // Check panels in reverse order (top-most first)
        for (int i = panels.size() - 1; i >= 0; i--) {
            UIPanel panel = panels.get(i);
            if (!panel.isVisible()) continue;
            
            if (panel.handleRightClick(mouseX, mouseY)) {
                return true;  // UI consumed the click
            }
        }
        
        return false;  // Click not on any UI
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