package dev.main;

import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Base class for all UI components
 */
public abstract class UIComponent {
    // Position (relative to parent or screen)
    protected int x, y;
    
    // Size
    protected int width, height;
    
    // Layout properties
    protected int marginTop, marginRight, marginBottom, marginLeft;
    protected int paddingTop, paddingRight, paddingBottom, paddingLeft;
    
    // State
    protected boolean visible = true;
    protected boolean enabled = true;
    protected boolean hovered = false;
    
    // Parent reference
    protected UIPanel parent;
    
    public UIComponent(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        
        // Default margins and padding
        this.marginTop = this.marginRight = this.marginBottom = this.marginLeft = 0;
        this.paddingTop = this.paddingRight = this.paddingBottom = this.paddingLeft = 0;
    }
    
    // Abstract methods to be implemented by subclasses
    public abstract void render(Graphics2D g);
    public abstract void update(float delta);
    
    // Layout methods
    public void setMargin(int top, int right, int bottom, int left) {
        this.marginTop = top;
        this.marginRight = right;
        this.marginBottom = bottom;
        this.marginLeft = left;
    }
    
    public void setMargin(int all) {
        setMargin(all, all, all, all);
    }
    
    public void setPadding(int top, int right, int bottom, int left) {
        this.paddingTop = top;
        this.paddingRight = right;
        this.paddingBottom = bottom;
        this.paddingLeft = left;
    }
    
    public void setPadding(int all) {
        setPadding(all, all, all, all);
    }
    
    // Bounds methods
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
    
    public Rectangle getOuterBounds() {
        return new Rectangle(
            x - marginLeft,
            y - marginTop,
            width + marginLeft + marginRight,
            height + marginTop + marginBottom
        );
    }
    
    public Rectangle getInnerBounds() {
        return new Rectangle(
            x + paddingLeft,
            y + paddingTop,
            width - paddingLeft - paddingRight,
            height - paddingTop - paddingBottom
        );
    }
    
    // Input handling
    public boolean contains(int mouseX, int mouseY) {
        return getBounds().contains(mouseX, mouseY);
    }
    
    public void onMouseEnter() {
        hovered = true;
    }
    
    public void onMouseExit() {
        hovered = false;
    }
    
    /**
     * Called when component is clicked
     * @return true if click was consumed (don't pass to world)
     */
    public boolean onClick() {
        // Override in subclasses
        return false;  // Default: don't consume click
    }
    
    /**
     * Called when component is right-clicked
     * @return true if click was consumed (don't pass to world)
     */
    public boolean onRightClick() {
        // Override in subclasses
        return false;  // Default: don't consume click
    }
    
    // Getters/Setters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public boolean isHovered() { return hovered; }
    
    public void setParent(UIPanel parent) {
        this.parent = parent;
    }
    
    public UIPanel getParent() {
        return parent;
    }
}