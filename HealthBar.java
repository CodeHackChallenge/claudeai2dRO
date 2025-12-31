package dev.main;

import java.awt.Color;

public class HealthBar implements Component {
    public int width;
    public int height;
    public int offsetY;  // How far below the sprite to draw
    
    // Colors
    public static final Color HP_GREEN = new Color(50, 205, 50);
    public static final Color HP_ORANGE = new Color(255, 165, 0);
    public static final Color HP_RED = new Color(220, 20, 60);
    public static final Color BG_COLOR = new Color(60, 60, 60);
    
    public HealthBar(int width, int height, int offsetY) {
        this.width = width;
        this.height = height;
        this.offsetY = offsetY;
    }
}