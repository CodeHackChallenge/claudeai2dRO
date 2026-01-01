package dev.main;

import java.awt.Color;
import java.awt.Graphics2D;

public class Renderer {
    private GameState gameState;
    
    public Renderer(GameState gameState) {
        this.gameState = gameState;
    }
    
    public void render(Graphics2D g) {
        float cameraX = gameState.getCameraX();
        float cameraY = gameState.getCameraY();
        
        // Render tile map FIRST (background layer)
        TileMap map = gameState.getMap();
        if (map != null) {
            map.render(g, cameraX, cameraY);
        }
        
        // Render entities - sprites and health bar together
        for (Entity entity : gameState.getEntities()) {
            Position pos = entity.getComponent(Position.class);
            Sprite sprite = entity.getComponent(Sprite.class);
            
            if (pos != null && sprite != null) {
                // Calculate pixel-perfect screen position ONCE
                int spriteScreenX = (int)Math.round(pos.x - cameraX);
                int spriteScreenY = (int)Math.round(pos.y - cameraY);
                
                // Render sprites at this position
                sprite.renderAtPixel(g, spriteScreenX, spriteScreenY);
                
                // Render health bar using the SAME pixel position
                Stats stats = entity.getComponent(Stats.class);
                HealthBar hpBar = entity.getComponent(HealthBar.class);
                
                if (stats != null && hpBar != null) {
                    drawHealthBarAtPixel(g, spriteScreenX, spriteScreenY, stats, hpBar);
                }
            }
        }
    }
    
    private void drawHealthBarAtPixel(Graphics2D g, int spriteX, int spriteY, Stats hp, HealthBar bar) {
        // Calculate bar position relative to sprites's pixel position
        int barX = spriteX - bar.width / 2;
        int barY = spriteY + bar.offsetY;
        
        // HP percentage
        float pct = (float) hp.hp / hp.maxHp;
        pct = Math.max(0f, Math.min(1f, pct));
        
        // ⭐ Minimum visible sliver if alive
        if (hp.hp > 0 && pct < 0.10f) {
            pct = 0.10f;
        }
        
        int filledWidth = (int)(bar.width * pct);
        
        // Color thresholds
        Color hpColor =
            pct > 0.50f ? HealthBar.HP_GREEN :
            pct > 0.25f ? HealthBar.HP_ORANGE :
                          HealthBar.HP_RED;
        
        // Background
        g.setColor(HealthBar.BG_COLOR);
        g.fillRect(barX, barY, bar.width, bar.height);
        
        // Fill
        g.setColor(hpColor);
        g.fillRect(barX, barY, filledWidth, bar.height);
        
        // Border
        g.setColor(Color.BLACK);
        g.drawRect(barX, barY, bar.width, bar.height);
    }
}