package dev.main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;

public class Renderer {
    private GameState gameState;
    private Engine engine;
    
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
        
        // Render target indicators
        for (Entity entity : gameState.getEntities()) {
            TargetIndicator indicator = entity.getComponent(TargetIndicator.class);
            if (indicator != null && indicator.active) {
                int screenX = (int)Math.round(indicator.worldX - cameraX);
                int screenY = (int)Math.round(indicator.worldY - cameraY);
                DiamondRenderer.renderDiamond(g, screenX, screenY, indicator.pulseScale, 1.0f);
            }
        }
        
        // Render entities
        for (Entity entity : gameState.getEntities()) {
            Position pos = entity.getComponent(Position.class);
            Sprite sprite = entity.getComponent(Sprite.class);
            
            if (pos != null && sprite != null) {
                int spriteScreenX = (int)Math.round(pos.x - cameraX);
                int spriteScreenY = (int)Math.round(pos.y - cameraY);
                
                sprite.renderAtPixel(g, spriteScreenX, spriteScreenY);
                
                // Don't show UI for dead entities
                Dead dead = entity.getComponent(Dead.class);
                boolean isDead = dead != null;
                
                if (!isDead) {
                    // Draw name tag
                    NameTag nameTag = entity.getComponent(NameTag.class);
                    if (nameTag != null && nameTag.visible) {
                        drawNameTag(g, spriteScreenX, spriteScreenY, nameTag);
                    }
                    
                    // Draw health bar
                    Stats stats = entity.getComponent(Stats.class);
                    HealthBar hpBar = entity.getComponent(HealthBar.class);
                    
                    if (stats != null && hpBar != null) {
                        drawHealthBar(g, spriteScreenX, spriteScreenY, stats, hpBar);
                    }
                    
                    // Draw stamina bar (player only)
                    if (entity.getType() == EntityType.PLAYER) {
                        StaminaBar staminaBar = entity.getComponent(StaminaBar.class);
                        if (stats != null && staminaBar != null) {
                            drawStaminaBar(g, spriteScreenX, spriteScreenY, stats, staminaBar);
                        }
                    }
                }
                
                // DEBUG
                if (engine.isDebugMode()) {
                    CollisionBox box = entity.getComponent(CollisionBox.class);
                    if (box != null) {
                        drawCollisionBox(g, pos, box, cameraX, cameraY);
                    }
                }
            }
        }
        // Render floating damage texts
        drawDamageTexts(g, cameraX, cameraY);
        
        // DEBUG
        if (engine.isDebugMode()) {
            if (map != null) {
                drawTileGrid(g, map, cameraX, cameraY);
            }
            
            for (Entity entity : gameState.getEntities()) {
                drawDebugPath(g, entity, cameraX, cameraY);
                if (entity.getType() == EntityType.MONSTER) {
                    drawDebugAI(g, entity, cameraX, cameraY);
                }
            }
        }
    }
    
    private void drawNameTag(Graphics2D g, int spriteX, int spriteY, NameTag tag) {
        Font originalFont = g.getFont();
        Font nameFont = new Font("Arial", Font.BOLD, 12);
        g.setFont(nameFont);
        
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(tag.displayName);
        
        int textX = spriteX - textWidth / 2;
        int textY = (int)(spriteY + tag.offsetY);
        
        // Draw shadow
        g.setColor(Color.BLACK);
        g.drawString(tag.displayName, textX + 1, textY + 1);
        
        // Draw text
        g.setColor(Color.WHITE);
        g.drawString(tag.displayName, textX, textY);
        
        g.setFont(originalFont);
    }
    
    private void drawDamageTexts(Graphics2D g, float cameraX, float cameraY) {
        Font originalFont = g.getFont();
        for (DamageText dt : gameState.getDamageTexts()) {
            int screenX = (int)(dt.worldX - cameraX);
            int screenY = (int)(dt.worldY - cameraY);
            
            // Choose font size based on type
            int fontSize = dt.type == DamageText.Type.CRITICAL ? 20 : 16;
            Font damageFont = new Font("Arial", Font.BOLD, fontSize);
            g.setFont(damageFont);
            
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(dt.text);
            
            int textX = screenX - textWidth / 2;
            int textY = screenY;
            
            // Calculate alpha
            int alpha = (int)(dt.getAlpha() * 255);
            
            // Draw shadow
            g.setColor(new Color(0, 0, 0, alpha));
            g.drawString(dt.text, textX + 2, textY + 2);
            
            // Draw text with color
            Color textColor = new Color(
                dt.color.getRed(),
                dt.color.getGreen(),
                dt.color.getBlue(),
                alpha
            );
            g.setColor(textColor);
            g.drawString(dt.text, textX, textY);
        }
        
        g.setFont(originalFont);
    } 
     
    private void drawHealthBar(Graphics2D g, int spriteX, int spriteY, Stats hp, HealthBar bar) {
        Stroke originalStroke = g.getStroke();
        
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
        g.setStroke(new BasicStroke(1f));
        g.drawRect(barX, barY, bar.width, bar.height);
        
        g.setStroke(originalStroke);
    }
    
    private void drawStaminaBar(Graphics2D g, int spriteX, int spriteY, Stats stats, StaminaBar bar) {
        Stroke originalStroke = g.getStroke();
        
        int barX = spriteX - bar.width / 2;
        int barY = spriteY + bar.offsetY;
        
        float pct = stats.stamina / stats.maxStamina;
        pct = Math.max(0f, Math.min(1f, pct));
        
        int filledWidth = (int)(bar.width * pct);
        
        g.setColor(StaminaBar.BG_COLOR);
        g.fillRect(barX, barY, bar.width, bar.height);
        
        g.setColor(StaminaBar.STAMINA_COLOR);
        g.fillRect(barX, barY, filledWidth, bar.height);
        
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1f));
        g.drawRect(barX, barY, bar.width, bar.height);
        
        g.setStroke(originalStroke);
    }
    
    private void drawCollisionBox(Graphics2D g, Position pos, CollisionBox box, float cameraX, float cameraY) {
        int boxX = (int)Math.round(box.getLeft(pos.x) - cameraX);
        int boxY = (int)Math.round(box.getTop(pos.y) - cameraY);
        int boxW = (int)box.width;
        int boxH = (int)box.height;
        
        g.setColor(new Color(255, 0, 0, 150));
        g.setStroke(new BasicStroke(2));
        g.drawRect(boxX, boxY, boxW, boxH);
        
        int centerX = (int)Math.round(pos.x - cameraX);
        int centerY = (int)Math.round(pos.y - cameraY);
        g.setColor(Color.YELLOW);
        g.fillOval(centerX - 3, centerY - 3, 6, 6);
    }
    
    private void drawTileGrid(Graphics2D g, TileMap map, float cameraX, float cameraY) {
        int startCol = Math.max(0, (int)(cameraX / TileMap.TILE_SIZE));
        int endCol = Math.min(map.getWidth(), (int)((cameraX + Engine.WIDTH) / TileMap.TILE_SIZE) + 1);
        
        int startRow = Math.max(0, (int)(cameraY / TileMap.TILE_SIZE));
        int endRow = Math.min(map.getHeight(), (int)((cameraY + Engine.HEIGHT) / TileMap.TILE_SIZE) + 1);
        
        g.setColor(new Color(255, 255, 255, 100));
        g.setStroke(new BasicStroke(1));
        
        for (int col = startCol; col <= endCol; col++) {
            int x = (int)(col * TileMap.TILE_SIZE - cameraX);
            g.drawLine(x, 0, x, Engine.HEIGHT);
        }
        
        for (int row = startRow; row <= endRow; row++) {
            int y = (int)(row * TileMap.TILE_SIZE - cameraY);
            g.drawLine(0, y, Engine.WIDTH, y);
        }
        
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
    
    private void drawDebugPath(Graphics2D g, Entity entity, float cameraX, float cameraY) {
        Path path = entity.getComponent(Path.class);
        
        if (path != null && path.waypoints != null) {
            g.setColor(new Color(0, 255, 255, 200));
            g.setStroke(new BasicStroke(3));
            
            for (int i = 0; i < path.waypoints.size() - 1; i++) {
                int[] current = path.waypoints.get(i);
                int[] next = path.waypoints.get(i + 1);
                
                int x1 = (int)((current[0] * TileMap.TILE_SIZE + TileMap.TILE_SIZE / 2f) - cameraX);
                int y1 = (int)((current[1] * TileMap.TILE_SIZE + TileMap.TILE_SIZE / 2f) - cameraY);
                int x2 = (int)((next[0] * TileMap.TILE_SIZE + TileMap.TILE_SIZE / 2f) - cameraX);
                int y2 = (int)((next[1] * TileMap.TILE_SIZE + TileMap.TILE_SIZE / 2f) - cameraY);
                
                g.drawLine(x1, y1, x2, y2);
            }
            
            g.setColor(Color.CYAN);
            for (int i = 0; i < path.waypoints.size(); i++) {
                int[] waypoint = path.waypoints.get(i);
                int x = (int)((waypoint[0] * TileMap.TILE_SIZE + TileMap.TILE_SIZE / 2f) - cameraX);
                int y = (int)((waypoint[1] * TileMap.TILE_SIZE + TileMap.TILE_SIZE / 2f) - cameraY);
                
                if (i == path.currentWaypoint) {
                    g.setColor(Color.YELLOW);
                    g.fillOval(x - 5, y - 5, 10, 10);
                    g.setColor(Color.CYAN);
                } else {
                    g.fillOval(x - 3, y - 3, 6, 6);
                }
            }
        }
    }
    
    private void drawDebugAI(Graphics2D g, Entity entity, float cameraX, float cameraY) {
        AI ai = entity.getComponent(AI.class);
        Position pos = entity.getComponent(Position.class);
        
        if (ai == null || pos == null) return;
        
        Stroke originalStroke = g.getStroke();
        
        // Draw home position
        int homeScreenX = (int)(ai.homeX - cameraX);
        int homeScreenY = (int)(ai.homeY - cameraY);
        g.setColor(new Color(0, 255, 0, 100));
        g.fillOval(homeScreenX - 5, homeScreenY - 5, 10, 10);
        
        // Draw roam radius
        int roamRadius = (int)ai.roamRadius;
        g.setColor(new Color(255, 255, 0, 80));
        g.setStroke(new BasicStroke(2));
        g.drawOval(homeScreenX - roamRadius, homeScreenY - roamRadius, roamRadius * 2, roamRadius * 2);
        
        // Draw detection range
        int detectionRadius = (int)(ai.detectionRange * TileMap.TILE_SIZE);
        int screenX = (int)(pos.x - cameraX);
        int screenY = (int)(pos.y - cameraY);
        
        Color detectionColor = ai.currentState == AI.State.CHASING 
            ? new Color(255, 0, 0, 100)
            : new Color(100, 100, 255, 80);
        g.setColor(detectionColor);
        g.drawOval(screenX - detectionRadius, screenY - detectionRadius, detectionRadius * 2, detectionRadius * 2);
        
        // Draw state text
        g.setColor(Color.WHITE);
        g.drawString(ai.currentState.toString(), screenX - 20, screenY - 40);
        
        g.setStroke(originalStroke);
    }
}