package dev.main;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class Sprite implements Component {
	// Idle animation constants
	public static final String ANIM_IDLE_DOWN = "idle_down";
	public static final String ANIM_IDLE_UP = "idle_up";
	public static final String ANIM_IDLE_LEFT = "idle_left";
	public static final String ANIM_IDLE_RIGHT = "idle_right";
	public static final String ANIM_IDLE_DOWN_LEFT = "idle_down_left";
	public static final String ANIM_IDLE_DOWN_RIGHT = "idle_down_right";
	public static final String ANIM_IDLE_UP_LEFT = "idle_up_left";
	public static final String ANIM_IDLE_UP_RIGHT = "idle_up_right";
	//run
	public static final String ANIM_RUN_DOWN = "run_down";
	public static final String ANIM_RUN_UP = "run_up";
	public static final String ANIM_RUN_LEFT = "run_left";
	public static final String ANIM_RUN_RIGHT = "run_right";
	public static final String ANIM_RUN_DOWN_LEFT = "run_down_left";
	public static final String ANIM_RUN_DOWN_RIGHT = "run_down_right";
	public static final String ANIM_RUN_UP_LEFT = "run_up_left";
	public static final String ANIM_RUN_UP_RIGHT = "run_up_right";
	
    private BufferedImage spriteSheet;
    private int frameWidth;
    private int frameHeight;
    
    // Animation state
    private int currentFrame;
    private float animationTimer;
    private float frameDuration;  // seconds per frame
    
    // Current animation
    private String currentAnimation;
    private boolean loopAnimation;  // NEW: Control looping
    // Animation definitions
    private Map<String, Animation> animations;
    
    // Animation names (constants for easy reference)
    public static final String ANIM_IDLE = "idle";
    public static final String ANIM_WALK_DOWN = "walk_down";
    public static final String ANIM_WALK_UP = "walk_up";
    public static final String ANIM_WALK_LEFT = "walk_left";
    public static final String ANIM_WALK_RIGHT = "walk_right";
    public static final String ANIM_WALK_DOWN_LEFT = "walk_down_left";
    public static final String ANIM_WALK_DOWN_RIGHT = "walk_down_right";
    public static final String ANIM_WALK_UP_LEFT = "walk_up_left";
    public static final String ANIM_WALK_UP_RIGHT = "walk_up_right";
    public static final String ANIM_ATTACK = "attack";
    public static final String ANIM_DEAD = "dead";
        
    // Inner class to hold animation data
    private static class Animation {
        int row;
        int frameCount;
        
        Animation(int row, int frameCount) {
            this.row = row;
            this.frameCount = frameCount;
        }
    }
    
    public Sprite(String spriteSheetPath, int frameWidth, int frameHeight, float frameDuration) {
        this.spriteSheet = TextureManager.load(spriteSheetPath);
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.frameDuration = frameDuration;
        this.currentFrame = 0;
        this.animationTimer = 0;
        this.animations = new HashMap<>();
        this.loopAnimation = true;  // Most animations loop
        
        // Setup animations - ADJUST THESE TO MATCH YOUR SPRITE SHEET
        setupAnimations();
        
        // Start with idle animation
        this.currentAnimation = "idle_down";
    }
    
    private void setupAnimations() {
        // Define your animations: (row, frameCount)
        // ADJUST THESE NUMBERS TO MATCH YOUR SPRITE SHEET LAYOUT
        
        // Idle animations - 2 frames each, 8 directions (rows 0-7)      
        animations.put("idle_down", new Animation(24, 2));   // Row 0, 2 frames
        animations.put("idle_up", new Animation(22, 2));     // Row 1, 2 frames
        animations.put("idle_left", new Animation(23, 2));   // Row 2, 2 frames
        animations.put("idle_right", new Animation(25, 2));  // Row 3, 2 frames
        animations.put("idle_down_left", new Animation(23, 2));      // Southwest
        animations.put("idle_down_right", new Animation(25, 2));     // Southeast
        animations.put("idle_up_left", new Animation(23, 2));        // Northwest
        animations.put("idle_up_right", new Animation(25, 2));       // Northeas
        
        
        // Walk animations - 5 frames each, 8 directions (rows 8-15)
        animations.put(ANIM_WALK_DOWN, new Animation(10, 9));
        animations.put(ANIM_WALK_UP, new Animation(8, 9));
        animations.put(ANIM_WALK_LEFT, new Animation(9, 9));
        animations.put(ANIM_WALK_RIGHT, new Animation(11, 9));
        animations.put(ANIM_WALK_DOWN_LEFT, new Animation(9, 9));
        animations.put(ANIM_WALK_DOWN_RIGHT, new Animation(11, 9));
        animations.put(ANIM_WALK_UP_LEFT, new Animation(9, 9));
        animations.put(ANIM_WALK_UP_RIGHT, new Animation(11, 9));
        
        
        
        // Run animations - 8 directions, adjust frame count to your sheet
        animations.put("run_down", new Animation(40, 8));
        animations.put("run_up", new Animation(38, 8));
        animations.put("run_left", new Animation(39, 8));
        animations.put("run_right", new Animation(41, 8));
        animations.put("run_down_left", new Animation(39, 8));
        animations.put("run_down_right", new Animation(41, 8));
        animations.put("run_up_left", new Animation(39, 8));
        animations.put("run_up_right", new Animation(41, 8));
        // Attack animation - different frame count (example: 6 frames, row 16)
        animations.put(ANIM_ATTACK, new Animation(16, 6));
        
     // Attack animations - 8 directions (adjust row numbers to your sheet)
        animations.put("attack_down", new Animation(24, 6));      // Row 24, 6 frames
        animations.put("attack_up", new Animation(25, 6));
        animations.put("attack_left", new Animation(26, 6));
        animations.put("attack_right", new Animation(27, 6));
        animations.put("attack_down_left", new Animation(28, 6));
        animations.put("attack_down_right", new Animation(29, 6));
        animations.put("attack_up_left", new Animation(30, 6));
        animations.put("attack_up_right", new Animation(31, 6));
        // Death animation - single animation (adjust row and frame count)
        animations.put("dead", new Animation(32, 8));  // Row 32, 8 frames death animation
        
        
    }
    
    public void update(float delta) {
        Animation anim = animations.get(currentAnimation);
        if (anim == null) return;
        
        animationTimer += delta;
        
        if (animationTimer >= frameDuration) {
            animationTimer -= frameDuration;
            
            if (loopAnimation) {
                currentFrame = (currentFrame + 1) % anim.frameCount;
            } else {
                // Don't loop - stop at last frame
                if (currentFrame < anim.frameCount - 1) {
                    currentFrame++;
                }
            }
        }
    }
    
    public void renderAtPixel(Graphics2D g, int screenX, int screenY) {
        if (spriteSheet == null) return;
        
        Animation anim = animations.get(currentAnimation);
        if (anim == null) return;
        
        int srcX = currentFrame * frameWidth;
        int srcY = anim.row * frameHeight;
        
        int destX = screenX - frameWidth / 2;
        int destY = screenY - frameHeight / 2;
        
        g.drawImage(
            spriteSheet,
            destX, destY, destX + frameWidth, destY + frameHeight,
            srcX, srcY, srcX + frameWidth, srcY + frameHeight,
            null
        );
    }
    
    public void render(Graphics2D g, float x, float y, float cameraX, float cameraY) {
        if (spriteSheet == null) return;
        
        Animation anim = animations.get(currentAnimation);
        if (anim == null) return;
        
        int srcX = currentFrame * frameWidth;
        int srcY = anim.row * frameHeight;
        
        int destX = (int)Math.round(x - cameraX - frameWidth / 2f);
        int destY = (int)Math.round(y - cameraY - frameHeight / 2f);
        
        g.drawImage(
            spriteSheet,
            destX, destY, destX + frameWidth, destY + frameHeight,
            srcX, srcY, srcX + frameWidth, srcY + frameHeight,
            null
        );
    }
    
    // Set animation by name
    public void setAnimation(String animationName) {
        if (!animationName.equals(currentAnimation)) {
            this.currentAnimation = animationName;
            this.currentFrame = 0;
            this.animationTimer = 0;
            
            // Death animation doesn't loop
            this.loopAnimation = !animationName.equals("dead");
        }
    }
    
    public boolean isAnimationFinished() {
        Animation anim = animations.get(currentAnimation);
        if (anim == null) return true;
        
        return !loopAnimation && currentFrame >= anim.frameCount - 1;
    }
    
    public String getCurrentAnimation() {
        return currentAnimation;
    }
    
    public int getCurrentFrame() {
        return currentFrame;
    }
}