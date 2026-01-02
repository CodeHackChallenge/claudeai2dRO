package dev.main;

import java.awt.Color;

public class DamageText implements Component {
    
    public enum Type {
        NORMAL,
        CRITICAL,
        MISS,
        HEAL
    }
    
    public String text;
    public Type type;
    public float worldX;
    public float worldY;
    public float velocityX;
    public float velocityY;
    public float lifetime;
    public float age;
    public Color color;
    
    public DamageText(String text, Type type, float worldX, float worldY) {
        this.text = text;
        this.type = type;
        this.worldX = worldX;
        this.worldY = worldY;
        this.age = 0;
        
        // Set properties based on type
        switch(type) {
            case NORMAL:
                this.color = Color.WHITE;
                this.lifetime = 1.0f;
                this.velocityX = (float)(Math.random() - 0.5) * 30f;  // Random horizontal push
                this.velocityY = -60f;  // Float upward
                break;
                
            case CRITICAL:
                this.color = new Color(255, 140, 0);  // Orange
                this.lifetime = 1.5f;
                this.velocityX = (float)(Math.random() - 0.5) * 40f;
                this.velocityY = -80f;
                break;
                
            case MISS:
                this.color = new Color(200, 200, 200);  // Light gray
                this.lifetime = 1.2f;
                this.velocityX = 0;
                this.velocityY = -50f;  // Float straight up
                break;
                
            case HEAL:
                this.color = new Color(0, 255, 100);  // Green
                this.lifetime = 1.0f;
                this.velocityX = 0;
                this.velocityY = -70f;
                break;
        }
    }
    
    public void update(float delta) {
        age += delta;
        worldX += velocityX * delta;
        worldY += velocityY * delta;
        
        // Slow down over time
        velocityX *= 0.95f;
        velocityY *= 0.98f;
    }
    
    public boolean shouldRemove() {
        return age >= lifetime;
    }
    
    public float getAlpha() {
        // Fade out in last 30% of lifetime
        float fadeStart = lifetime * 0.7f;
        if (age < fadeStart) {
            return 1.0f;
        }
        return 1.0f - ((age - fadeStart) / (lifetime - fadeStart));
    }
}