package dev.main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * UI component representing a skill slot
 */
public class UISkillSlot extends UIComponent {
    
    private Skill skill;  // Can be null (empty slot)
    private String keyBinding;  // e.g., "1", "Q", "E"
    private boolean showCooldown;
    private boolean showKeybind;
    
    // Visual properties
    private Color emptyColor;
    private Color hoverColor;
    private Color cooldownColor;
    private Color keybindBgColor;
    
    public UISkillSlot(int x, int y, int size) {
        super(x, y, size, size);
        
        this.skill = null;
        this.keyBinding = "";
        this.showCooldown = true;
        this.showKeybind = true;
        
        // Default colors
        this.emptyColor = new Color(60, 60, 60, 200);
        this.hoverColor = new Color(100, 100, 100, 255);
        this.cooldownColor = new Color(0, 0, 0, 150);
        this.keybindBgColor = new Color(0, 0, 0, 180);
    }
    
    public UISkillSlot(int x, int y, int size, String keyBinding) {
        this(x, y, size);
        this.keyBinding = keyBinding;
    }
    
    @Override
    public void render(Graphics2D g) {
        if (!visible) return;
        
        // Draw slot background
        if (skill == null) {
            // Empty slot
            g.setColor(hovered ? hoverColor : emptyColor);
        } else {
            // Filled slot - use skill color
            g.setColor(skill.getIconColor());
        }
        g.fillRect(x, y, width, height);
        
        // Draw border
        g.setColor(hovered ? Color.WHITE : new Color(100, 100, 100));
        g.setStroke(new BasicStroke(2));
        g.drawRect(x, y, width, height);
        
        if (skill != null) {
            // Draw skill icon (if available)
            if (skill.getIconPath() != null) {
                BufferedImage icon = TextureManager.load(skill.getIconPath());
                if (icon != null) {
                    g.drawImage(icon, x, y, width, height, null);
                }
            }
            
            // Draw cooldown overlay
            if (showCooldown && !skill.isReady()) {
                drawCooldownOverlay(g);
            }
            
            // Draw cooldown text
            if (!skill.isReady()) {
                drawCooldownText(g);
            }
        } else {
            // Draw empty slot indicator
            g.setColor(new Color(150, 150, 150, 100));
            int size = width / 3;
            int centerX = x + width / 2 - size / 2;
            int centerY = y + height / 2 - size / 2;
            g.fillRect(centerX + size / 3, centerY, size / 3, size);
            g.fillRect(centerX, centerY + size / 3, size, size / 3);
        }
        
        // Draw keybind
        if (showKeybind && !keyBinding.isEmpty()) {
            drawKeybind(g);
        }
        
        // Draw hover effect
        if (hovered && skill != null) {
            g.setColor(new Color(255, 255, 255, 50));
            g.fillRect(x, y, width, height);
        }
    }
    
    private void drawCooldownOverlay(Graphics2D g) {
        float progress = skill.getCooldownProgress();
        int overlayHeight = (int)(height * progress);
        
        g.setColor(cooldownColor);
        g.fillRect(x, y, width, overlayHeight);
    }
    
    private void drawCooldownText(Graphics2D g) {
        Font originalFont = g.getFont();
        Font cooldownFont = new Font("Arial", Font.BOLD, 16);
        g.setFont(cooldownFont);
        
        String cooldownText = String.format("%.1f", skill.getRemainingCooldown());
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(cooldownText);
        int textHeight = fm.getHeight();
        
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height + textHeight) / 2 - 4;
        
        // Shadow
        g.setColor(Color.BLACK);
        g.drawString(cooldownText, textX + 1, textY + 1);
        
        // Text
        g.setColor(Color.WHITE);
        g.drawString(cooldownText, textX, textY);
        
        g.setFont(originalFont);
    }
    
    private void drawKeybind(Graphics2D g) {
        Font originalFont = g.getFont();
        Font keybindFont = new Font("Arial", Font.BOLD, 12);
        g.setFont(keybindFont);
        
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(keyBinding);
        int textHeight = fm.getHeight();
        
        int bgSize = Math.max(textWidth, textHeight) + 4;
        int bgX = x + 2;
        int bgY = y + height - bgSize - 2;
        
        // Background
        g.setColor(keybindBgColor);
        g.fillRect(bgX, bgY, bgSize, bgSize);
        
        // Border
        g.setColor(new Color(200, 200, 200));
        g.drawRect(bgX, bgY, bgSize, bgSize);
        
        // Text
        int textX = bgX + (bgSize - textWidth) / 2;
        int textY = bgY + (bgSize + textHeight / 2) / 2;
        
        g.setColor(Color.WHITE);
        g.drawString(keyBinding, textX, textY);
        
        g.setFont(originalFont);
    }
    
    @Override
    public void update(float delta) {
        if (skill != null) {
            skill.update(delta);
        }
    }
    
    @Override
    public void onClick() {
        if (skill != null && skill.isReady()) {
            useSkill();
        }
    }
    
    private void useSkill() {
        if (skill != null && skill.use()) {
            System.out.println("Used skill: " + skill.getName());
            // TODO: Trigger actual skill effect
        }
    }
    
    /**
     * Get tooltip text for this slot
     */
    public String getTooltipText() {
        if (skill == null) {
            return "Empty Slot" + (keyBinding.isEmpty() ? "" : " [" + keyBinding + "]");
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(skill.getName()).append("\n");
        sb.append(skill.getDescription()).append("\n");
        sb.append("─────────────\n");
        sb.append("Type: ").append(skill.getType()).append("\n");
        sb.append("Cooldown: ").append(skill.getCooldown()).append("s\n");
        sb.append("Mana: ").append(skill.getManaCost()).append("\n");
        sb.append("Level Req: ").append(skill.getLevelRequired());
        
        if (!keyBinding.isEmpty()) {
            sb.append("\n\nHotkey: [").append(keyBinding).append("]");
        }
        
        return sb.toString();
    }
    
    // Getters/Setters
    public Skill getSkill() {
        return skill;
    }
    
    public void setSkill(Skill skill) {
        this.skill = skill;
    }
    
    public String getKeyBinding() {
        return keyBinding;
    }
    
    public void setKeyBinding(String keyBinding) {
        this.keyBinding = keyBinding;
    }
    
    public void setShowCooldown(boolean show) {
        this.showCooldown = show;
    }
    
    public void setShowKeybind(boolean show) {
        this.showKeybind = show;
    }
}