package dev.main;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * UI Button component that displays images
 * Changes image on hover, supports locked/unlocked states
 */
public class UIButton extends UIComponent {
    
	 // ⭐ ADD THIS at the top of the class:
    private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 12);
    
    
    private String id;
    private String label;
    private BufferedImage iconNormal;
    private BufferedImage iconHover;
    private BufferedImage iconLocked;
    private boolean locked;
    
    private String iconPathNormal;
    private String iconPathHover;
    private String iconPathLocked;
    
    // Callback for when button is clicked
    private Runnable onClickCallback;
    
    public UIButton(int x, int y, int width, int height, String id, String label) {
        super(x, y, width, height);
        this.id = id;
        this.label = label;
        this.locked = false;
        this.iconNormal = null;
        this.iconHover = null;
        this.iconLocked = null;
        this.onClickCallback = null;
    }
    
    /**
     * Set icon paths (will be loaded when needed)
     */
    public void setIcons(String normalPath, String hoverPath, String lockedPath) {
        this.iconPathNormal = normalPath;
        this.iconPathHover = hoverPath;
        this.iconPathLocked = lockedPath;
        
        // Load icons
        if (normalPath != null) {
            iconNormal = TextureManager.load(normalPath);
        }
        if (hoverPath != null) {
            iconHover = TextureManager.load(hoverPath);
        }
        if (lockedPath != null) {
            iconLocked = TextureManager.load(lockedPath);
        }
    }
    
    /**
     * Set single icon (same for all states)
     */
    public void setIcon(String iconPath) {
        setIcons(iconPath, iconPath, iconPath);
    }
    
    /**
     * Set click callback
     */
    public void setOnClick(Runnable callback) {
        this.onClickCallback = callback;
    }
    @Override
    public void render(Graphics2D g) {
        if (!visible) return;
        
        // Choose which icon to display
        BufferedImage currentIcon = null;
        
        if (locked && iconLocked != null) {
            currentIcon = iconLocked;
        } else if (hovered && !locked && iconHover != null) {
            currentIcon = iconHover;
        } else if (iconNormal != null) {
            currentIcon = iconNormal;
        }
        
        // Draw icon
        if (currentIcon != null) {
            // ☆ Apply transparency to locked icons
            if (locked) {
                java.awt.AlphaComposite alphaComposite = java.awt.AlphaComposite.getInstance(
                    java.awt.AlphaComposite.SRC_OVER, 0.4f);
                java.awt.Composite oldComposite = g.getComposite();
                g.setComposite(alphaComposite);
                g.drawImage(currentIcon, x, y, width, height, null);
                g.setComposite(oldComposite);
                
                // ☆ Draw small lock icon overlay
                drawLockOverlay(g);
            } else {
                g.drawImage(currentIcon, x, y, width, height, null);
            }
        } else {
            // Fallback: draw colored rectangle if no icon
            if (locked) {
                g.setColor(new java.awt.Color(80, 80, 80, 120));
            } else if (hovered) {
                g.setColor(new java.awt.Color(150, 150, 150, 220));
            } else {
                g.setColor(new java.awt.Color(100, 100, 100, 200));
            }
            g.fillRect(x, y, width, height);
            
            // Draw label
            g.setColor(locked ? new java.awt.Color(150, 150, 150) : java.awt.Color.WHITE);
            g.drawString(label, x + 5, y + height / 2 + 5);
        }
        
        // When drawing label text, replace:
        // Font originalFont = g.getFont();
        // Font labelFont = new Font("Arial", Font.PLAIN, 12);
        // g.setFont(labelFont);
        
        // WITH:
        Font originalFont = g.getFont();
        g.setFont(LABEL_FONT);  // ⭐ Use cached font
        
        // ... draw label text ...
        
        g.setFont(originalFont);
        
    }

    /**
     * ☆ NEW: Draw lock icon overlay for locked buttons
     */
    private void drawLockOverlay(Graphics2D g) {
        int lockSize = width / 3;
        int lockX = x + width - lockSize - 2;
        int lockY = y + 2;
        
        // Draw lock body
        g.setColor(new java.awt.Color(200, 200, 200, 200));
        g.fillRect(lockX + 2, lockY + 6, lockSize - 4, lockSize - 8);
        
        // Draw lock shackle
        g.drawArc(lockX + 3, lockY, lockSize - 6, lockSize - 4, 0, 180);
        
        // Draw keyhole
        g.setColor(new java.awt.Color(80, 80, 80, 200));
        int keyholeX = lockX + lockSize / 2 - 1;
        int keyholeY = lockY + lockSize / 2;
        g.fillOval(keyholeX, keyholeY, 2, 2);
    }
    
    @Override
    public void update(float delta) {
        // Buttons don't need per-frame updates
    }
    
    @Override
    public boolean onClick() {
        if (locked || !enabled) {
            System.out.println(label + " is locked!");
            return true;  // Still consume the click
        }
        
        // Execute callback
        if (onClickCallback != null) {
            onClickCallback.run();
        } else {
            System.out.println("Clicked: " + label);
        }
        
        return true;  // Consume click
    }
    
    // Getters/Setters
    public String getId() {
        return id;
    }
    
    public String getLabel() {
        return label;
    }
    
    public boolean isLocked() {
        return locked;
    }
    
    public void setLocked(boolean locked) {
        this.locked = locked;
        this.enabled = !locked;  // Locked buttons are disabled
    }
    
    public void unlock() {
        setLocked(false);
        setVisible(true);  // Ensure visible when unlocked
    }

    public void lock() {
        setLocked(true);
        setVisible(true);  // ☆ Keep visible even when locked (just dimmed)
    }
}