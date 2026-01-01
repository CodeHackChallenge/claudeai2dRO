package dev.main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class Renderer {
	
    private GameState gameState;
    private Engine engine;  // NEW: Reference to engine for debug mode
    
    public Renderer(GameState gameState, Engine engine) {
        this.gameState = gameState;
        this.engine = engine;
    }
    
    public void render(Graphics2D g) {
        float cameraX = gameState.getCameraX();
        float cameraY = gameState.getCameraY();
        
        // Render tile map
        TileMap map = gameState.getMap();
        if (map != null) {
            map.render(g, cameraX, cameraY);
        }
        
        // Render entities
        for (Entity entity : gameState.getEntities()) {
            Position pos = entity.getComponent(Position.class);
            Sprite sprite = entity.getComponent(Sprite.class);
            
            if (pos != null && sprite != null) {
                int spriteScreenX = (int)Math.round(pos.x - cameraX);
                int spriteScreenY = (int)Math.round(pos.y - cameraY);
                
                sprite.renderAtPixel(g, spriteScreenX, spriteScreenY);
                
                // Draw health bar
                Stats stats = entity.getComponent(Stats.class);
                HealthBar hpBar = entity.getComponent(HealthBar.class);
                
                if (stats != null && hpBar != null) {
                    drawHealthBar(g, spriteScreenX, spriteScreenY, stats, hpBar);
                }
                
                // Draw stamina bar
                StaminaBar staminaBar = entity.getComponent(StaminaBar.class);
                
                if (stats != null && staminaBar != null) {
                    drawStaminaBar(g, spriteScreenX, spriteScreenY, stats, staminaBar);
                }
                
                // DEBUG: Draw collision box
                if (engine.isDebugMode()) {
                    CollisionBox box = entity.getComponent(CollisionBox.class);
                    if (box != null) {
                        drawCollisionBox(g, pos, box, cameraX, cameraY);
                    }
                }
            }
        }
        
        // DEBUG: Draw tile grid
        if (engine.isDebugMode() && map != null) {
            drawTileGrid(g, map, cameraX, cameraY);
        }
    }

    private void drawCollisionBox(Graphics2D g, Position pos, CollisionBox box, float cameraX, float cameraY) {
        int boxX = (int)Math.round(box.getLeft(pos.x) - cameraX);
        int boxY = (int)Math.round(box.getTop(pos.y) - cameraY);
        int boxW = (int)box.width;
        int boxH = (int)box.height;
        
        // Draw red rectangle for collision box
        g.setColor(new Color(255, 0, 0, 150));
        g.setStroke(new BasicStroke(2));
        g.drawRect(boxX, boxY, boxW, boxH);
        
        // Draw center point
        int centerX = (int)Math.round(pos.x - cameraX);
        int centerY = (int)Math.round(pos.y - cameraY);
        g.setColor(Color.YELLOW);
        g.fillOval(centerX - 3, centerY - 3, 6, 6);
    }
    
    private void drawTileGrid(Graphics2D g, TileMap map, float cameraX, float cameraY) {
        // Calculate visible tile range
        int startCol = Math.max(0, (int)(cameraX / TileMap.TILE_SIZE));
        int endCol = Math.min(map.getWidth(), (int)((cameraX + Engine.WIDTH) / TileMap.TILE_SIZE) + 1);
        
        int startRow = Math.max(0, (int)(cameraY / TileMap.TILE_SIZE));
        int endRow = Math.min(map.getHeight(), (int)((cameraY + Engine.HEIGHT) / TileMap.TILE_SIZE) + 1);
        
        g.setColor(new Color(255, 255, 255, 100));
        g.setStroke(new BasicStroke(1));
        
        // Draw vertical lines
        for (int col = startCol; col <= endCol; col++) {
            int x = (int)(col * TileMap.TILE_SIZE - cameraX);
            g.drawLine(x, 0, x, Engine.HEIGHT);
        }
        
        // Draw horizontal lines
        for (int row = startRow; row <= endRow; row++) {
            int y = (int)(row * TileMap.TILE_SIZE - cameraY);
            g.drawLine(0, y, Engine.WIDTH, y);
        }
        
        // Highlight solid tiles
        g.setColor(new Color(255, 0, 0, 80));
        for (int row = startRow; row < endRow; row++) {
            for (int col = startCol; col < endCol; col++) {
                if (map.isSolid(col, row)) {
                    int x = (int)(col * TileMap.TILE_SIZE - cameraX);
                    int y = (int)(row * TileMap.TILE_SIZE - cameraY);
                    g.fillRect(x, y, TileMap.TILE_SIZE, TileMap.TILE_SIZE);
                }
            }
        }
    }
    
    private void drawHealthBar(Graphics2D g, int spriteX, int spriteY, Stats hp, HealthBar bar) {
        int barX = spriteX - bar.width / 2;
        int barY = spriteY + bar.offsetY;
        
        float pct = (float) hp.hp / hp.maxHp;
        pct = Math.max(0f, Math.min(1f, pct));
        
        if (hp.hp > 0 && pct < 0.10f) {
            pct = 0.10f;
        }
        
        int filledWidth = (int)(bar.width * pct);
        
        Color hpColor =
            pct > 0.50f ? HealthBar.HP_GREEN :
            pct > 0.25f ? HealthBar.HP_ORANGE :
                          HealthBar.HP_RED;
        
        g.setColor(HealthBar.BG_COLOR);
        g.fillRect(barX, barY, bar.width, bar.height);
        
        g.setColor(hpColor);
        g.fillRect(barX, barY, filledWidth, bar.height);
        
        g.setColor(Color.BLACK);
        g.drawRect(barX, barY, bar.width, bar.height);
    }

    private void drawStaminaBar(Graphics2D g, int spriteX, int spriteY, Stats stats, StaminaBar bar) {
        int barX = spriteX - bar.width / 2;
        int barY = spriteY + bar.offsetY;
        
        float pct = stats.stamina / stats.maxStamina;
        pct = Math.max(0f, Math.min(1f, pct));
        
        int filledWidth = (int)(bar.width * pct);
        
        // Background
        g.setColor(StaminaBar.BG_COLOR);
        g.fillRect(barX, barY, bar.width, bar.height);
        
        // Fill
        g.setColor(StaminaBar.STAMINA_COLOR);
        g.fillRect(barX, barY, filledWidth, bar.height);
        
        // Border
        g.setColor(Color.BLACK);
        g.drawRect(barX, barY, bar.width, bar.height);
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