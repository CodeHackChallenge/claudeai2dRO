package dev.main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel that can contain and layout child UI components
 */
public class UIPanel extends UIComponent {
    
    public enum LayoutType {
        NONE,        // Manual positioning
        HORIZONTAL,  // Left to right
        VERTICAL,    // Top to bottom
        GRID         // Grid layout
    }
    
    private List<UIComponent> children;
    private LayoutType layoutType;
    private int gap;  // Space between children
    
    // Grid layout properties
    private int columns;
    private int rows;
    
    // Visual properties
    private Color backgroundColor;
    private Color borderColor;
    private int borderWidth;
    
    public UIPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.children = new ArrayList<>();
        this.layoutType = LayoutType.NONE;
        this.gap = 0;
        this.columns = 1;
        this.rows = 1;
        
        // Default visual style
        this.backgroundColor = new Color(40, 40, 40, 200);
        this.borderColor = new Color(100, 100, 100, 255);
        this.borderWidth = 2;
    }
    
    /**
     * Add a child component
     */
    public void addChild(UIComponent child) {
        child.setParent(this);
        children.add(child);
        relayout();
    }
    
    /**
     * Remove a child component
     */
    public void removeChild(UIComponent child) {
        child.setParent(null);
        children.remove(child);
        relayout();
    }
    
    /**
     * Clear all children
     */
    public void clearChildren() {
        for (UIComponent child : children) {
            child.setParent(null);
        }
        children.clear();
    }
    
    /**
     * Set layout type and recalculate positions
     */
    public void setLayout(LayoutType layoutType) {
        this.layoutType = layoutType;
        relayout();
    }
    
    /**
     * Set gap between children
     */
    public void setGap(int gap) {
        this.gap = gap;
        relayout();
    }
    
    /**
     * Set grid dimensions (only for GRID layout)
     */
    public void setGridDimensions(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
        if (layoutType == LayoutType.GRID) {
            relayout();
        }
    }
    
    /**
     * Recalculate child positions based on layout
     */
    public void relayout() {
        if (children.isEmpty()) return;
        
        Rectangle innerBounds = getInnerBounds();
        int startX = innerBounds.x;
        int startY = innerBounds.y;
        int availableWidth = innerBounds.width;
        int availableHeight = innerBounds.height;
        
        switch (layoutType) {
            case HORIZONTAL:
                layoutHorizontal(startX, startY, availableWidth);
                break;
                
            case VERTICAL:
                layoutVertical(startX, startY, availableHeight);
                break;
                
            case GRID:
                layoutGrid(startX, startY, availableWidth, availableHeight);
                break;
                
            case NONE:
            default:
                // Manual positioning - do nothing
                break;
        }
    }
    
    private void layoutHorizontal(int startX, int startY, int availableWidth) {
        int currentX = startX;
        
        for (UIComponent child : children) {
            if (!child.isVisible()) continue;
            
            // Apply child's margin
            currentX += child.marginLeft;
            
            child.setPosition(currentX, startY + child.marginTop);
            
            // Move to next position
            currentX += child.getWidth() + child.marginRight + gap;
        }
    }
    
    private void layoutVertical(int startX, int startY, int availableHeight) {
        int currentY = startY;
        
        for (UIComponent child : children) {
            if (!child.isVisible()) continue;
            
            // Apply child's margin
            currentY += child.marginTop;
            
            child.setPosition(startX + child.marginLeft, currentY);
            
            // Move to next position
            currentY += child.getHeight() + child.marginBottom + gap;
        }
    }
    
    private void layoutGrid(int startX, int startY, int availableWidth, int availableHeight) {
        if (columns <= 0 || rows <= 0) return;
        
        // Calculate cell size
        int cellWidth = (availableWidth - (gap * (columns - 1))) / columns;
        int cellHeight = (availableHeight - (gap * (rows - 1))) / rows;
        
        int index = 0;
        for (UIComponent child : children) {
            if (!child.isVisible()) continue;
            if (index >= columns * rows) break;  // Grid is full
            
            int col = index % columns;
            int row = index / columns;
            
            int cellX = startX + (col * (cellWidth + gap)) + child.marginLeft;
            int cellY = startY + (row * (cellHeight + gap)) + child.marginTop;
            
            child.setPosition(cellX, cellY);
            
            // Optionally resize child to fit cell
            // child.setSize(cellWidth - child.marginLeft - child.marginRight,
            //               cellHeight - child.marginTop - child.marginBottom);
            
            index++;
        }
    }
    
    @Override
    public void render(Graphics2D g) {
        if (!visible) return;
        
        // Draw background
        if (backgroundColor != null) {
            g.setColor(backgroundColor);
            g.fillRect(x, y, width, height);
        }
        
        // Draw border
        if (borderColor != null && borderWidth > 0) {
            g.setColor(borderColor);
            g.setStroke(new java.awt.BasicStroke(borderWidth));
            g.drawRect(x, y, width, height);
        }
        
        // Render children
        for (UIComponent child : children) {
            if (child.isVisible()) {
                child.render(g);
            }
        }
    }
    
    @Override
    public void update(float delta) {
        if (!visible) return;
        
        for (UIComponent child : children) {
            if (child.isVisible()) {
                child.update(delta);
            }
        }
    }
    
    /**
     * Handle mouse input for all children
     */
    public void handleMouseMove(int mouseX, int mouseY) {
        for (UIComponent child : children) {
            if (!child.isVisible() || !child.isEnabled()) continue;
            
            boolean contains = child.contains(mouseX, mouseY);
            
            if (contains && !child.isHovered()) {
                child.onMouseEnter();
            } else if (!contains && child.isHovered()) {
                child.onMouseExit();
            }
        }
    }
    
    /**
     * Handle mouse click for children
     * @return true if any child consumed the click
     */
    public boolean handleClick(int mouseX, int mouseY) {
        // Check children in reverse order (top-most first)
        for (int i = children.size() - 1; i >= 0; i--) {
            UIComponent child = children.get(i);
            if (!child.isVisible() || !child.isEnabled()) continue;
            
            if (child.contains(mouseX, mouseY)) { 
                boolean consumed = child.onClick();
                if (consumed) { 
                    return true;  // Click was consumed, stop propagation
                }
            }
        }
        
        // Check if click is on the panel itself (not on children)
        if (this.contains(mouseX, mouseY)) {
            return true;  // Panel consumed the click (don't pass to world)
        }
        
        return false;  // Click not on panel or children
    }
    
    /**
     * Handle right click for children
     * @return true if any child consumed the click
     */
    public boolean handleRightClick(int mouseX, int mouseY) {
        // Check children in reverse order (top-most first)
        for (int i = children.size() - 1; i >= 0; i--) {
            UIComponent child = children.get(i);
            if (!child.isVisible() || !child.isEnabled()) continue;
            
            if (child.contains(mouseX, mouseY)) {
                boolean consumed = child.onRightClick();
                if (consumed) {
                    return true;  // Click was consumed, stop propagation
                }
            }
        }
        
        // Check if click is on the panel itself (not on children)
        if (this.contains(mouseX, mouseY)) {
            return true;  // Panel consumed the click (don't pass to world)
        }
        
        return false;  // Click not on panel or children
    }
    
    // Visual style setters
    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
    }
    
    public void setBorderColor(Color color) {
        this.borderColor = color;
    }
    
    public void setBorderWidth(int width) {
        this.borderWidth = width;
    }
    
    // Getters
    public List<UIComponent> getChildren() {
        return children;
    }
    
    public LayoutType getLayoutType() {
        return layoutType;
    }
}