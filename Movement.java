package dev.main;

public class Movement implements Component {
    public float speed;  // pixels per second
    
    public float targetX;
    public float targetY;
    public boolean isMoving;
    
    public int direction;  // 0-7 for 8 directions (0=right, clockwise)
    
    public Movement(float speed) {
        this.speed = speed;
        this.isMoving = false;
        this.direction = 0;
    }
    
    public void setTarget(float x, float y) {
        this.targetX = x;
        this.targetY = y;
        this.isMoving = true;
    }
    
    public void stopMoving() {
        this.isMoving = false;
    }
}
