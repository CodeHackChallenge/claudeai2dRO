package dev.main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        
        // === LAYER 0: GROUND ===
        renderGround(g, cameraX, cameraY);
        
        // === LAYER 1: GROUND_DECOR ===
        renderGroundDecor(g, cameraX, cameraY);
        
        // === LAYER 2: ENTITIES (sorted by depth) ===
        renderEntities(g, cameraX, cameraY);
        
        // === LAYER 3: EFFECTS ===
        renderEffects(g, cameraX, cameraY);
        
        // === LAYER 4: UI_WORLD (world-space UI) ===
        renderWorldUI(g, cameraX, cameraY);
        
        // === LAYER 5: UI_SCREEN ===
        renderScreenUI(g, cameraX, cameraY);
        
        // === DEBUG (always on top) ===
        if (engine.isDebugMode()) {
            renderDebug(g, cameraX, cameraY);
        }
    }
    
    // ===================================================================
    // LAYER 0: GROUND
    // ===================================================================
    private void renderGround(Graphics2D g, float cameraX, float cameraY) {
        TileMap map = gameState.getMap();
        if (map != null) {
            map.render(g, cameraX, cameraY);
        }
    }
    
    // ===================================================================
    // LAYER 1: GROUND_DECOR
    // ===================================================================
    private void renderGroundDecor(Graphics2D g, float cameraX, float cameraY) {
        // Target indicators (diamond on ground)
        for (Entity entity : gameState.getEntities()) {
            TargetIndicator indicator = entity.getComponent(TargetIndicator.class);
            if (indicator != null && indicator.active) {
                int screenX = (int)Math.round(indicator.worldX - cameraX);
                int screenY = (int)Math.round(indicator.worldY - cameraY);
                DiamondRenderer.renderDiamond(g, screenX, screenY, indicator.pulseScale, 1.0f);
            }
        }
    }
    
    // ===================================================================
    // LAYER 2: ENTITIES (depth sorted)
    // ===================================================================
    private void renderEntities(Graphics2D g, float cameraX, float cameraY) {
        // Collect all renderable entities
        List<RenderObject> renderObjects = new ArrayList<>();
        
        for (Entity entity : gameState.getEntities()) {
            Position pos = entity.getComponent(Position.class);
            Renderable renderable = entity.getComponent(Renderable.class);
            Sprite sprite = entity.getComponent(Sprite.class);
            
            if (pos != null && renderable != null && sprite != null) {
                // Only add if on ENTITIES layer
                if (renderable.layer == RenderLayer.ENTITIES) {
                    renderObjects.add(new RenderObject(entity, pos, renderable));
                }
            }
        }
        
        // Sort by depth (Y position)
        Collections.sort(renderObjects);
        
        // Render in sorted order
        for (RenderObject ro : renderObjects) {
            Entity entity = ro.entity;
            Position pos = ro.position;
            
            int spriteScreenX = (int)Math.round(pos.x - cameraX);
            int spriteScreenY = (int)Math.round(pos.y - cameraY);
            
            // Draw sprite
            Sprite sprite = entity.getComponent(Sprite.class);
            if (sprite != null) {
                sprite.renderAtPixel(g, spriteScreenX, spriteScreenY);
            }
        }
    }
    
    // ===================================================================
    // LAYER 3: EFFECTS
    // ===================================================================
    private void renderEffects(Graphics2D g, float cameraX, float cameraY) {
        // Render floating damage texts
        drawDamageTexts(g, cameraX, cameraY);
        
        // TODO: Particle effects, spell animations, etc.
    }
    
    // ===================================================================
    // LAYER 4: UI_WORLD (world-space UI elements)
    // ===================================================================
 // Update the renderWorldUI method to include XP bar and level display:
    private void renderWorldUI(Graphics2D g, float cameraX, float cameraY) {
        // Collect entities for UI rendering (same depth sorting)
        List<RenderObject> renderObjects = new ArrayList<>();
        
        for (Entity entity : gameState.getEntities()) {
            Position pos = entity.getComponent(Position.class);
            Renderable renderable = entity.getComponent(Renderable.class);
            
            if (pos != null && renderable != null) {
                if (renderable.layer == RenderLayer.ENTITIES) {
                    renderObjects.add(new RenderObject(entity, pos, renderable));
                }
            }
        }
        
        Collections.sort(renderObjects);
        
        // Render UI elements in same order as entities
        for (RenderObject ro : renderObjects) {
            Entity entity = ro.entity;
            Position pos = ro.position;
            
            int spriteScreenX = (int)Math.round(pos.x - cameraX);
            int spriteScreenY = (int)Math.round(pos.y - cameraY);
            
            Dead dead = entity.getComponent(Dead.class);
            boolean isDead = dead != null;
            
            if (!isDead) {
                // Alert (exclamation point)
                Alert alert = entity.getComponent(Alert.class);
                if (alert != null) {
                    drawAlert(g, spriteScreenX, spriteScreenY, alert);
                }
                
                // Name tag
                NameTag nameTag = entity.getComponent(NameTag.class);
                if (nameTag != null && nameTag.visible) {
                    drawNameTag(g, spriteScreenX, spriteScreenY, nameTag);
                }
                
                // Health bar
                Stats stats = entity.getComponent(Stats.class);
                HealthBar hpBar = entity.getComponent(HealthBar.class);
                
                if (stats != null && hpBar != null) {
                    drawHealthBar(g, spriteScreenX, spriteScreenY, stats, hpBar);
                }
                
                // Player-specific UI
                if (entity.getType() == EntityType.PLAYER) {
                    // Stamina bar
                    StaminaBar staminaBar = entity.getComponent(StaminaBar.class);
                    if (stats != null && staminaBar != null) {
                        drawStaminaBar(g, spriteScreenX, spriteScreenY, stats, staminaBar);
                    }
                    
                    // ★ NEW: XP bar
                    Experience exp = entity.getComponent(Experience.class);
                    if (exp != null) {
                        drawXPBar(g, spriteScreenX, spriteScreenY, exp);
                        drawLevelBadge(g, spriteScreenX, spriteScreenY, exp.level);
                    }
                    
                    // ★ NEW: Level-up effect
                    LevelUpEffect levelUpEffect = entity.getComponent(LevelUpEffect.class);
                    if (levelUpEffect != null && levelUpEffect.active) {
                        drawLevelUpEffect(g, spriteScreenX, spriteScreenY, levelUpEffect);
                    }
                }
            }
        }
    }
    
    // ===================================================================
    // LAYER 5: UI_SCREEN
    // ===================================================================
    private void renderScreenUI(Graphics2D g, float cameraX, float cameraY) {
        // TODO: Screen-space UI like minimap, inventory, hotbar, etc.
        // These don't move with camera
    }
    
    // ===================================================================
    // DEBUG RENDERING (always on top)
    // ===================================================================
    private void renderDebug(Graphics2D g, float cameraX, float cameraY) {
        TileMap map = gameState.getMap();
        
        // Tile grid
        if (map != null) {
            drawTileGrid(g, map, cameraX, cameraY);
        }
        
        // Spawn points
        drawDebugSpawnPoints(g, cameraX, cameraY);
        
        // Per-entity debug info
        for (Entity entity : gameState.getEntities()) {
            Position pos = entity.getComponent(Position.class);
            
            if (pos != null) {
                int screenX = (int)Math.round(pos.x - cameraX);
                int screenY = (int)Math.round(pos.y - cameraY);
                
                // Collision box
                CollisionBox box = entity.getComponent(CollisionBox.class);
                if (box != null) {
                    drawCollisionBox(g, pos, box, cameraX, cameraY);
                }
                
                // Path
                drawDebugPath(g, entity, cameraX, cameraY);
                
                // AI info (monsters only)
                if (entity.getType() == EntityType.MONSTER) {
                    drawDebugAI(g, entity, cameraX, cameraY);
                }
                
                // Depth value
                Renderable renderable = entity.getComponent(Renderable.class);
                if (renderable != null) {
                    g.setColor(Color.CYAN);
                    g.drawString("Y:" + (int)pos.y, screenX + 10, screenY);
                }
            }
        }
    }
    
    // ===================================================================
    // HELPER DRAWING METHODS
    // ===================================================================
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
 // In Renderer.drawDebugAI()
    private void drawDebugAI(Graphics2D g, Entity entity, float cameraX, float cameraY) {
        AI ai = entity.getComponent(AI.class);
        Position pos = entity.getComponent(Position.class);
        Movement movement = entity.getComponent(Movement.class);
        
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
        
        // ⭐ Draw haste indicator
        if (movement != null && movement.isHasted) {
            g.setColor(new Color(255, 255, 0, 200));
            g.setStroke(new BasicStroke(3));
            g.drawOval(screenX - 20, screenY - 20, 40, 40);
            g.drawString("HASTE", screenX - 20, screenY - 50);
        }
        
        // Draw state text
        g.setColor(Color.WHITE);
        String stateText = ai.currentState.toString();
        if (movement != null && movement.isHasted) {
            stateText += " (HASTE)";
        }
        g.drawString(stateText, screenX - 30, screenY - 40);
        
        g.setStroke(originalStroke);
    }
    
    private void drawDebugSpawnPoints(Graphics2D g, float cameraX, float cameraY) {
        Font originalFont = g.getFont();
        Font timerFont = new Font("Arial", Font.BOLD, 12);
        g.setFont(timerFont);
        
        for (SpawnPoint sp : gameState.getSpawnPoints()) {
            int screenX = (int)(sp.x - cameraX);
            int screenY = (int)(sp.y - cameraY);
            
            // Draw spawn point marker
            if (sp.isOccupied) {
                // Green = occupied
                g.setColor(new Color(0, 255, 0, 150));
            } else {
                // Red = waiting for respawn
                g.setColor(new Color(255, 0, 0, 150));
            }
            g.fillOval(screenX - 8, screenY - 8, 16, 16);
            
            // Draw border
            g.setColor(Color.WHITE);
            g.drawOval(screenX - 8, screenY - 8, 16, 16);
            
            // Draw respawn timer if waiting
            if (!sp.isOccupied) {
                float timeLeft = sp.respawnDelay - sp.respawnTimer;
                String timerText = String.format("%.1fs", timeLeft);
                
                FontMetrics fm = g.getFontMetrics();
                int textWidth = fm.stringWidth(timerText);
                
                // Draw shadow
                g.setColor(Color.BLACK);
                g.drawString(timerText, screenX - textWidth/2 + 1, screenY + 20 + 1);
                
                // Draw timer text
                g.setColor(Color.YELLOW);
                g.drawString(timerText, screenX - textWidth/2, screenY + 20);
            }
            
            // Draw spawn type
            String typeText = sp.monsterType;
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(typeText);
            
            g.setColor(Color.BLACK);
            g.drawString(typeText, screenX - textWidth/2 + 1, screenY - 15 + 1);
            
            g.setColor(Color.WHITE);
            g.drawString(typeText, screenX - textWidth/2, screenY - 15);
        }
        
        g.setFont(originalFont);
    }
    private void drawNameTag(Graphics2D g, int spriteX, int spriteY, NameTag tag) {
        Font originalFont = g.getFont();
        Font nameFont = new Font("Arial", Font.BOLD, 12);
        g.setFont(nameFont);
        
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(tag.displayName);
        
        int textX = spriteX - textWidth / 2;
        int textY = (int)(spriteY + tag.offsetY);
        
        // Shadow
        g.setColor(Color.BLACK);
        g.drawString(tag.displayName, textX + 1, textY + 1);
        
        // Text
        g.setColor(Color.WHITE);
        g.drawString(tag.displayName, textX, textY);
        
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
    
    /**
     * Draw XP bar (similar to health/stamina bar)
     */
    private void drawXPBar(Graphics2D g, int spriteX, int spriteY, Experience exp) {
        Stroke originalStroke = g.getStroke();
        
        int barWidth = 40;
        int barHeight = 3;
        int offsetY = 52; // Below stamina bar
        
        int barX = spriteX - barWidth / 2;
        int barY = spriteY + offsetY;
        
        float pct = exp.getXPProgress();
        int filledWidth = (int)(barWidth * pct);
        
        // Background
        g.setColor(new Color(40, 40, 40));
        g.fillRect(barX, barY, barWidth, barHeight);
        
        // XP fill (gold color)
        g.setColor(new Color(255, 215, 0));
        g.fillRect(barX, barY, filledWidth, barHeight);
        
        // Border
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1f));
        g.drawRect(barX, barY, barWidth, barHeight);
        
        g.setStroke(originalStroke);
    }

    /**
     * Draw player level badge
     */
    private void drawLevelBadge(Graphics2D g, int spriteX, int spriteY, int level) {
        Font originalFont = g.getFont();
        Font levelFont = new Font("Arial", Font.BOLD, 10);
        g.setFont(levelFont);
        
        String levelText = "Lv" + level;
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(levelText);
        
        int badgeX = spriteX - 25;
        int badgeY = spriteY - 35;
        
        // Badge background (circle)
        g.setColor(new Color(0, 0, 0, 180));
        g.fillOval(badgeX - 12, badgeY - 8, 24, 16);
        
        // Badge border
        g.setColor(new Color(255, 215, 0));
        g.setStroke(new BasicStroke(2));
        g.drawOval(badgeX - 12, badgeY - 8, 24, 16);
        
        // Level text
        g.setColor(Color.WHITE);
        g.drawString(levelText, badgeX - textWidth/2, badgeY + 4);
        
        g.setFont(originalFont);
    }

    /**
     * Draw level-up effect (glowing aura)
     */
    private void drawLevelUpEffect(Graphics2D g, int spriteX, int spriteY, LevelUpEffect effect) {
        if (!effect.active) return;
        
        float alpha = effect.getAlpha();
        int alphaVal = (int)(alpha * 200);
        
        Font originalFont = g.getFont();
        Font levelUpFont = new Font("Arial", Font.BOLD, 20);
        g.setFont(levelUpFont);
        
        // Expanding circle effect
        int radius = (int)(30 + (1 - alpha) * 20);
        g.setColor(new Color(255, 255, 0, alphaVal / 2));
        g.fillOval(spriteX - radius, spriteY - radius, radius * 2, radius * 2);
        
        // Outer glow
        g.setColor(new Color(255, 215, 0, alphaVal));
        g.setStroke(new BasicStroke(3));
        g.drawOval(spriteX - radius, spriteY - radius, radius * 2, radius * 2);
        
        // "LEVEL UP!" text
        String text = "LEVEL " + effect.newLevel;
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        
        int textY = spriteY - 50 - (int)((1 - alpha) * 20); // Floats up
        
        // Shadow
        g.setColor(new Color(0, 0, 0, alphaVal));
        g.drawString(text, spriteX - textWidth/2 + 2, textY + 2);
        
        // Text
        g.setColor(new Color(255, 215, 0, alphaVal));
        g.drawString(text, spriteX - textWidth/2, textY);
        
        g.setFont(originalFont);
    }
    
    private void drawAlert(Graphics2D g, int spriteX, int spriteY, Alert alert) {
        if (!alert.active) return;
        
        Stroke originalStroke = g.getStroke();
        Font originalFont = g.getFont();
        
        int alertX = spriteX;
        int alertY = (int)(spriteY + alert.offsetY + alert.bounceOffset);
        
        Font alertFont = new Font("Arial", Font.BOLD, 24);
        g.setFont(alertFont);
        
        String exclamation = "!";
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(exclamation);
        int textHeight = fm.getHeight();
        
        int textX = alertX - textWidth / 2;
        int textY = alertY + textHeight / 4;
       /* 
        // Background circle
        int circleSize = 20;
        g.setColor(new Color(255, 255, 255, 200));
        g.fillOval(alertX - circleSize/2, alertY - circleSize/2, circleSize, circleSize);
        
        // Border
        g.setColor(new Color(255, 0, 0, 255));
        g.setStroke(new BasicStroke(2));
        g.drawOval(alertX - circleSize/2, alertY - circleSize/2, circleSize, circleSize);
        */
        // Shadow
        g.setColor(new Color(0, 0, 0, 150));
        g.drawString(exclamation, textX + 1, textY + 1);
        
        // Exclamation
        g.setColor(new Color(255, 0, 0, 255));
        g.drawString(exclamation, textX, textY);
        
        g.setStroke(originalStroke);
        g.setFont(originalFont);
    }
    
    private void drawDamageTexts(Graphics2D g, float cameraX, float cameraY) {
        Font originalFont = g.getFont();
        
        for (DamageText dt : gameState.getDamageTexts()) {
            int screenX = (int)(dt.worldX - cameraX);
            int screenY = (int)(dt.worldY - cameraY);
            
            int fontSize = dt.type == DamageText.Type.CRITICAL ? 20 : 16;
            Font damageFont = new Font("Arial", Font.BOLD, fontSize);
            g.setFont(damageFont);
            
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(dt.text);
            
            int textX = screenX - textWidth / 2;
            int textY = screenY;
            
            int alpha = (int)(dt.getAlpha() * 255);
            
            g.setColor(new Color(0, 0, 0, alpha));
            g.drawString(dt.text, textX + 2, textY + 2);
            
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
    
    // ... Include all your existing debug drawing methods:
    // drawCollisionBox, drawTileGrid, drawDebugPath, drawDebugAI, drawDebugSpawnPoints
    
    // (Copy them from your existing Renderer class - they stay the same)
}