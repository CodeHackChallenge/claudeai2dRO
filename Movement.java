package dev.main;

public class Movement implements Component {
    public float walkSpeed;
    public float runSpeed;
    public float currentSpeed;
    
    public float targetX;
    public float targetY;
    public boolean isMoving;
    public boolean isRunning;  // NEW: Track running state
    
    public int direction;
    public int lastDirection;
    
    // Stamina cost
    public float staminaCostPerSecond = 15f;  // Running drains 15 stamina/sec
    
    // Direction constants
    public static final int DIR_EAST = 0;
    public static final int DIR_SOUTH_EAST = 1;
    public static final int DIR_SOUTH = 2;
    public static final int DIR_SOUTH_WEST = 3;
    public static final int DIR_WEST = 4;
    public static final int DIR_NORTH_WEST = 5;
    public static final int DIR_NORTH = 6;
    public static final int DIR_NORTH_EAST = 7;
    
    public Movement(float walkSpeed, float runSpeed) {
        this.walkSpeed = walkSpeed;
        this.runSpeed = runSpeed;
        this.currentSpeed = walkSpeed;
        this.isMoving = false;
        this.isRunning = false;
        this.direction = DIR_SOUTH;
        this.lastDirection = DIR_SOUTH;
    }
    
    public void setTarget(float x, float y, boolean run) {
        this.targetX = x;
        this.targetY = y;
        this.isMoving = true;
        this.isRunning = run;
        this.currentSpeed = run ? runSpeed : walkSpeed;
    }
    
    public void stopMoving() {
        this.isMoving = false;
        this.isRunning = false;
        this.currentSpeed = walkSpeed;
        this.lastDirection = this.direction;
    }
    
    public void stopRunning() {
        this.isRunning = false;
        this.currentSpeed = walkSpeed;
    }
}
