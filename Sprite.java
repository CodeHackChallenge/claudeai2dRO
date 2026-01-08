package dev.main;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * OPTIMIZED: Cached current animation to eliminate HashMap lookups every frame
 */
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
    
    // Run
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
    private float frameDuration;
    
    // Current animation
    private String currentAnimation;
    private boolean loopAnimation;
    private Animation cachedAnimation;  // ⭐ NEW: Cache current animation
    
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
        this.loopAnimation = true;
        this.cachedAnimation = null;  // ⭐ NEW
        
        setupAnimations();
        
        // Start with idle animation
        this.currentAnimation = "idle_down";
        this.cachedAnimation = animations.get(this.currentAnimation);  // ⭐ NEW: Cache initial
    }
    
    private void setupAnimations() {
        // Idle animations
        animations.put("idle_down", new Animation(24, 2));
        animations.put("idle_up", new Animation(22, 2));
        animations.put("idle_left", new Animation(23, 2));
        animations.put("idle_right", new Animation(25, 2));
        animations.put("idle_down_left", new Animation(23, 2));
        animations.put("idle_down_right", new Animation(25, 2));
        animations.put("idle_up_left", new Animation(23, 2));
        animations.put("idle_up_right", new Animation(25, 2));
        
        // Walk animations
        animations.put(ANIM_WALK_DOWN, new Animation(10, 9));
        animations.put(ANIM_WALK_UP, new Animation(8, 9));
        animations.put(ANIM_WALK_LEFT, new Animation(9, 9));
        animations.put(ANIM_WALK_RIGHT, new Animation(11, 9));
        animations.put(ANIM_WALK_DOWN_LEFT, new Animation(9, 9));
        animations.put(ANIM_WALK_DOWN_RIGHT, new Animation(11, 9));
        animations.put(ANIM_WALK_UP_LEFT, new Animation(9, 9));
        animations.put(ANIM_WALK_UP_RIGHT, new Animation(11, 9));
        
        animations.put(ANIM_ATTACK, new Animation(16, 6));
        
        // Run animations
        animations.put("run_down", new Animation(40, 8));
        animations.put("run_up", new Animation(38, 8));
        animations.put("run_left", new Animation(39, 8));
        animations.put("run_right", new Animation(41, 8));
        animations.put("run_down_left", new Animation(39, 8));
        animations.put("run_down_right", new Animation(41, 8));
        animations.put("run_up_left", new Animation(39, 8));
        animations.put("run_up_right", new Animation(41, 8));
        
        // Attack animations
        animations.put("attack_down", new Animation(14, 6));
        animations.put("attack_up", new Animation(12, 6));
        animations.put("attack_left", new Animation(13, 6));
        animations.put("attack_right", new Animation(15, 6));
        animations.put("attack_down_left", new Animation(13, 6));
        animations.put("attack_down_right", new Animation(15, 6));
        animations.put("attack_up_left", new Animation(13, 6));
        animations.put("attack_up_right", new Animation(15, 6));
        
        animations.put("dead", new Animation(20, 6));
    }
    
    // ⭐ OPTIMIZED: Use cached animation instead of HashMap lookup
    public void update(float delta) {
        if (cachedAnimation == null) return;  // ⭐ Use cache
        
        animationTimer += delta;
        
        if (animationTimer >= frameDuration) {
            animationTimer -= frameDuration;
            
            if (loopAnimation) {
                currentFrame = (currentFrame + 1) % cachedAnimation.frameCount;  // ⭐ Use cache
            } else {
                if (currentFrame < cachedAnimation.frameCount - 1) {  // ⭐ Use cache
                    currentFrame++;
                }
            }
        }
    }
    
    // ⭐ OPTIMIZED: Use cached animation
    public void renderAtPixel(Graphics2D g, int screenX, int screenY) {
        if (spriteSheet == null || cachedAnimation == null) return;  // ⭐ Use cache
        
        int srcX = currentFrame * frameWidth;
        int srcY = cachedAnimation.row * frameHeight;  // ⭐ Use cache
        
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
        if (spriteSheet == null || cachedAnimation == null) return;  // ⭐ Use cache
        
        int srcX = currentFrame * frameWidth;
        int srcY = cachedAnimation.row * frameHeight;  // ⭐ Use cache
        
        int destX = (int)Math.round(x - cameraX - frameWidth / 2f);
        int destY = (int)Math.round(y - cameraY - frameHeight / 2f);
        
        g.drawImage(
            spriteSheet,
            destX, destY, destX + frameWidth, destY + frameHeight,
            srcX, srcY, srcX + frameWidth, srcY + frameHeight,
            null
        );
    }
    
    // ⭐ OPTIMIZED: Cache animation on change
    public void setAnimation(String animationName) {
        if (!animationName.equals(currentAnimation)) {
            this.currentAnimation = animationName;
            this.cachedAnimation = animations.get(animationName);  // ⭐ Cache lookup
            this.currentFrame = 0;
            this.animationTimer = 0;
            this.loopAnimation = !animationName.equals("dead");
        }
    }
    
    public boolean isAnimationFinished() {
        if (cachedAnimation == null) return true;  // ⭐ Use cache
        return !loopAnimation && currentFrame >= cachedAnimation.frameCount - 1;  // ⭐ Use cache
    }
    
    public String getCurrentAnimation() {
        return currentAnimation;
    }
    
    public int getCurrentFrame() {
        return currentFrame;
    }
}