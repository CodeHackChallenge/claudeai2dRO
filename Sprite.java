package dev.main;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Sprite implements Component {
    private BufferedImage spriteSheet;
    private int frameWidth;
    private int frameHeight;
    
    // Animation state
    private int currentFrame;
    private int totalFrames;
    private float animationTimer;
    private float frameDuration;  // seconds per frame
    
    // Current animation row (for different animations on same sheet)
    private int currentRow;
    
    public Sprite(String spriteSheetPath, int frameWidth, int frameHeight, int totalFrames, float frameDuration) {
        this.spriteSheet = TextureManager.load(spriteSheetPath);
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.totalFrames = totalFrames;
        this.frameDuration = frameDuration;
        this.currentFrame = 0;
        this.currentRow = 0;
        this.animationTimer = 0;
    }
    
    public void update(float delta) {
        animationTimer += delta;
        
        if (animationTimer >= frameDuration) {
            animationTimer -= frameDuration;
            currentFrame = (currentFrame + 1) % totalFrames;
        }
    }
    
    // NEW: Render at exact pixel position (no float calculations)
    public void renderAtPixel(Graphics2D g, int screenX, int screenY) {
        if (spriteSheet == null) return;
        
        // Calculate source rectangle (which frame to draw from spritesheet)
        int srcX = currentFrame * frameWidth;
        int srcY = currentRow * frameHeight;
        
        // Calculate destination centered on the given pixel
        int destX = screenX - frameWidth / 2;
        int destY = screenY - frameHeight / 2;
        
        g.drawImage(
            spriteSheet,
            destX, destY, destX + frameWidth, destY + frameHeight,  // destination
            srcX, srcY, srcX + frameWidth, srcY + frameHeight,      // source
            null
        );
    }
    
    // OLD: Keep this for backward compatibility or remove if not used elsewhere
    public void render(Graphics2D g, float x, float y, float cameraX, float cameraY) {
        if (spriteSheet == null) return;
        
        int srcX = currentFrame * frameWidth;
        int srcY = currentRow * frameHeight;
        
        int destX = (int)Math.round(x - cameraX - frameWidth / 2f);
        int destY = (int)Math.round(y - cameraY - frameHeight / 2f);
        
        g.drawImage(
            spriteSheet,
            destX, destY, destX + frameWidth, destY + frameHeight,
            srcX, srcY, srcX + frameWidth, srcY + frameHeight,
            null
        );
    }
    
    // Change animation row (for walk, attack, etc.)
    public void setAnimationRow(int row) {
        if (this.currentRow != row) {
            this.currentRow = row;
            this.currentFrame = 0;
            this.animationTimer = 0;
        }
    }
    
    public int getCurrentFrame() {
        return currentFrame;
    }
    
    public int getCurrentRow() {
        return currentRow;
    }
}