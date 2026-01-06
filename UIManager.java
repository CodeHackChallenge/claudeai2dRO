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
    
    // Skill bar
    private UIPanel skillBar;
    
    public UIManager(GameState gameState) {
        this.gameState = gameState;
        this.panels = new ArrayList<>();
        
        initializeUI();
    }
    
    private void initializeUI() {
        // Create skill bar at bottom center of screen
        createSkillBar();
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
            slot.setMargin(0);  // No margin needed with gap
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
        // Create some test skills
        Skill fireball = new Skill(
            "fireball",
            "Fireball",
            "Launch a blazing fireball at your enemy",
            Skill.SkillType.ATTACK,
            3.0f,  // 3 second cooldown
            20,    // 20 mana cost
            1      // Level 1 required
        );
        
        Skill heal = new Skill(
            "heal",
            "Heal",
            "Restore health over time",
            Skill.SkillType.HEAL,
            8.0f,
            30,
            1
        );
        
        Skill shield = new Skill(
            "shield",
            "Shield",
            "Create a protective barrier",
            Skill.SkillType.DEFENSE,
            12.0f,
            25,
            3
        );
        
        Skill haste = new Skill(
            "haste",
            "Haste",
            "Increase movement speed",
            Skill.SkillType.BUFF,
            20.0f,
            15,
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
     */
    public void handleClick(int mouseX, int mouseY) {
        for (UIPanel panel : panels) {
            panel.handleClick(mouseX, mouseY);
        }
    }
    
    /**
     * Handle right click
     */
    public void handleRightClick(int mouseX, int mouseY) {
        for (UIPanel panel : panels) {
            panel.handleRightClick(mouseX, mouseY);
        }
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
                    ((UISkillSlot)slots.get(index)).onClick();
                }
            }
            
            // Letter keys Q, E, R, F
            switch (keyCode) {
                case java.awt.event.KeyEvent.VK_Q:
                    if (slots.size() > 4) ((UISkillSlot)slots.get(4)).onClick();
                    break;
                case java.awt.event.KeyEvent.VK_E:
                    if (slots.size() > 5) ((UISkillSlot)slots.get(5)).onClick();
                    break;
                case java.awt.event.KeyEvent.VK_R:
                    if (slots.size() > 6) ((UISkillSlot)slots.get(6)).onClick();
                    break;
                case java.awt.event.KeyEvent.VK_F:
                    if (slots.size() > 7) ((UISkillSlot)slots.get(7)).onClick();
                    break;
            }
        }
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