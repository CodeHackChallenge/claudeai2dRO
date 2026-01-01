package dev.main;

public class Movement implements Component {
    public float speed;
    
    public float targetX;
    public float targetY;
    public boolean isMoving;
    
    public int direction;
    public int lastDirection;
    
    // Direction constants
    public static final int DIR_EAST = 0;
    public static final int DIR_SOUTH_EAST = 1;
    public static final int DIR_SOUTH = 2;
    public static final int DIR_SOUTH_WEST = 3;
    public static final int DIR_WEST = 4;
    public static final int DIR_NORTH_WEST = 5;
    public static final int DIR_NORTH = 6;
    public static final int DIR_NORTH_EAST = 7;
    
    public Movement(float speed) {
        this.speed = speed;
        this.isMoving = false;
        this.direction = DIR_SOUTH;
        this.lastDirection = DIR_SOUTH;
    }
    
    public void setTarget(float x, float y) {
        this.targetX = x;
        this.targetY = y;
        this.isMoving = true;
    }
    
    public void stopMoving() {
        this.isMoving = false;
        this.lastDirection = this.direction;  // Remember exact 8-way direction
    }
}
