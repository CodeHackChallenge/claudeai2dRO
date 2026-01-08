package dev.main;
 

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Engine extends Canvas implements Runnable, KeyListener {

    // Display constants
    public static final int SPRITE_SIZE = 64;
    private static final int SCALE = 2;
    
    public static final int WIDTH = 640 * SCALE;
    public static final int HEIGHT = 320 * SCALE;
    
    // Game loop constants
    public static final int UPS = 60;
    public static final int FPS = 120;

    // Engine state
    private boolean isRunning = false;
    private Thread thread;
    private BufferStrategy bufferStrategy;
    
    // Input
    private MouseInput mouse;
    
    // Game systems - MMO-ready separation
    private GameState gameState;
    private GameLogic gameLogic;
    private Renderer renderer;  // NEW: Separate rendering logic
    
    private boolean shiftPressed = false; //what is this for?
    private boolean debugMode = false;  // NEW: Debug visualization toggle
    
    private Cursor defaultCursor;
    private Cursor attackCursor;
    
    public Engine() {
        // Window setup
        JFrame window = new JFrame("RO-Style 2D Game v.1");
        JPanel panel = (JPanel) window.getContentPane();
        panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        panel.setLayout(null);
        panel.add(this);

        setBounds(0, 0, WIDTH, HEIGHT);
        setIgnoreRepaint(true);

        window.pack();
        //window.setResizable(false);
        window.setVisible(true);
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Setup cursors
        defaultCursor = Cursor.getDefaultCursor();
        // Create attack cursor (you can replace with custom image)
        try {
            // Placeholder: Use built-in crosshair cursor
           // attackCursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
            
            // To use custom cursor image:
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Image cursorImage = toolkit.getImage("resources/icon/sword.png"); // /sprites/hero2.png
             attackCursor = toolkit.createCustomCursor(cursorImage, new Point(16, 16), "attack");
        } catch (Exception e) {
            attackCursor = defaultCursor;
        }
        
        // Input
        mouse = new MouseInput();
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        addKeyListener(this);  // Add key listener
        
        requestFocus();

        // Buffer strategy
        createBufferStrategy(2);
        bufferStrategy = getBufferStrategy();

        // Initialize game
        gameSetup();
        
        
    }

    private void gameSetup() { 
        // Initialize game systems
        gameState = new GameState();
        gameLogic = new GameLogic(gameState);
        renderer = new Renderer(gameState, this);
        
        // ☆ NEW: Connect UI Manager to GameLogic for skill execution
        gameState.setGameLogic(gameLogic);
        
        System.out.println("Game initialized!");
    }
    
 // ⭐ Call handleMouseHover() in update() ONLY when mouse moved
    public void update(float delta) {
        // ⭐ Only check mouse hover when needed
        if (mouse.hasMoved()) {
            handleMouseHover();
        }
        
        // Handle input
        handleInput();
        
        // Update game logic
        gameLogic.update(delta);
        
        // Update UI
        gameState.getUIManager().update(delta);
    }
    
 // ⭐ Also optimize handleMouseHover() - only check when mouse moves
    private void handleMouseHover() {
        // ⭐ NEW: Only check when mouse actually moved
        if (!mouse.hasMoved()) {
            return;
        }
        
        int screenX = mouse.getX();
        int screenY = mouse.getY();
        
        float worldX = screenX + gameState.getCameraX();
        float worldY = screenY + gameState.getCameraY();
        
        // Check if hovering over any monster
        Entity hoveredMonster = null;
        
        for (Entity entity : gameState.getEntities()) {
            if (entity.getType() != EntityType.MONSTER) continue;
            
            Position pos = entity.getComponent(Position.class);
            CollisionBox box = entity.getComponent(CollisionBox.class);
            
            if (pos != null && box != null) {
                // Check if mouse is within collision box
                float left = box.getLeft(pos.x);
                float right = box.getRight(pos.x);
                float top = box.getTop(pos.y);
                float bottom = box.getBottom(pos.y);
                
                if (worldX >= left && worldX <= right && worldY >= top && worldY <= bottom) {
                    hoveredMonster = entity;
                    break;
                }
            }
        }
        
        // Update hover state
        gameState.setHoveredEntity(hoveredMonster);
        
        // Change cursor
        if (hoveredMonster != null) {
            setCursor(attackCursor);
        } else {
            setCursor(defaultCursor);
        }
        
        // ⭐ Reset movement flag
        mouse.resetMoved();
    }
    
    private void handleInput() {
        if (mouse.isLeftClick()) {
            int screenX = mouse.getX();
            int screenY = mouse.getY();
            
            // ☆ Check UI clicks first
            boolean uiConsumedClick = gameState.getUIManager().handleClick(screenX, screenY);
            
            if (!uiConsumedClick) {
                float worldX = screenX + gameState.getCameraX();
                float worldY = screenY + gameState.getCameraY();
                
                Entity hoveredEntity = gameState.getHoveredEntity();
                
                // ☆ Check if clicking a monster
                if (hoveredEntity != null && hoveredEntity.getType() == EntityType.MONSTER) {
                    Stats stats = hoveredEntity.getComponent(Stats.class);
                    if (stats != null && stats.hp > 0) {
                        gameState.setTargetedEntity(hoveredEntity);
                        
                        // ☆ Attack the monster (this will clear old path internally)
                        gameLogic.playerAttack(hoveredEntity);
                        
                        System.out.println("Attacking " + hoveredEntity.getName());
                    }
                } else {
                    // ☆ Clicking empty ground - stop auto-attack and move to location
                    gameLogic.stopAutoAttack();
                    gameLogic.movePlayerTo(worldX, worldY, shiftPressed);
                }
            }
            
            mouse.resetPressed();
        }
        
        if (mouse.isRightClick()) {
            int screenX = mouse.getX();
            int screenY = mouse.getY();
            
            // ☆ Check UI right clicks
            boolean uiConsumedClick = gameState.getUIManager().handleRightClick(screenX, screenY);
            
            if (!uiConsumedClick) {
                float worldX = screenX + gameState.getCameraX();
                float worldY = screenY + gameState.getCameraY();
                
                // ☆ Right-click always stops auto-attack and moves
                gameLogic.stopAutoAttack();
                gameLogic.movePlayerTo(worldX, worldY, shiftPressed);
            }
            
            mouse.resetPressed();
        }
    }
    // KeyListener implementation
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            shiftPressed = true;
        }
        
        // Toggle debug mode with F3
        if (e.getKeyCode() == KeyEvent.VK_F3) {
            debugMode = !debugMode;
            System.out.println("Debug mode: " + (debugMode ? "ON" : "OFF"));
        }
        
        // ☆ Handle skill hotkeys AND inventory key
        gameState.getUIManager().handleKeyPress(e.getKeyCode());
        
        // ☆ NEW: Test unlocking menu buttons (press U)
        if (e.getKeyCode() == KeyEvent.VK_U) {
        	//String[] buttonIds = {"stats", "character", "gear", "quest", "skilltree", "message", "trade", "world", "settings"};
            String[] buttonIds = {"settings","gear", "quest", "skilltree", "stats", "character", "trade", "message", "world"};
            boolean unlocked = false;  
            
            for (String id : buttonIds) {
                UIButton button = gameState.getUIManager().getMenuButton(id);
                if (button != null && button.isLocked()) {
                    gameState.getUIManager().unlockMenuButton(id);
                    unlocked = true;
                    break;  // Unlock one at a time
                }
            }
            
            if (!unlocked) {
                System.out.println("DEBUG: All menu buttons already unlocked!");
            }
        }
        
        // ☆ NEW: Lock all buttons again (press L)
        if (e.getKeyCode() == KeyEvent.VK_L) {
            String[] buttonIds = {"settings","gear", "quest", "skilltree", "stats", "character", "trade", "message", "world"};
            
            for (String id : buttonIds) {
                gameState.getUIManager().lockMenuButton(id);
            }
            
            System.out.println("DEBUG: Locked all menu buttons except Inventory");
        }
        // ☆ NEW: Add test item to inventory (press G for "Get item")
        if (e.getKeyCode() == KeyEvent.VK_G) {
            boolean added = gameState.getUIManager().addItemToInventory("TestItem");
            if (added) {
                System.out.println("DEBUG: Added test item to inventory");
            } else {
                System.out.println("DEBUG: Inventory is full!");
            }
        }
        // ☆ NEW: Clear all items from inventory (press C for "Clear")
        if (e.getKeyCode() == KeyEvent.VK_C) {
            UIPanel inventoryPanel = gameState.getUIManager().getInventoryPanel();
            if (inventoryPanel != null) {
                List<UIComponent> slots = inventoryPanel.getChildren();
                int cleared = 0;
                for (UIComponent component : slots) {
                    if (component instanceof UIInventorySlot) {
                        UIInventorySlot slot = (UIInventorySlot) component;
                        if (!slot.isEmpty()) {
                            slot.removeItem();
                            cleared++;
                        }
                    }
                }
                System.out.println("DEBUG: Cleared " + cleared + " items from inventory");
            }
        } 
        // Debug: Damage player (press D)
        if (e.getKeyCode() == KeyEvent.VK_D) {
            Entity player = gameState.getPlayer();
            Stats stats = player.getComponent(Stats.class);
            if (stats != null) {
                stats.hp -= 10;
                if (stats.hp < 0) stats.hp = 0;
                System.out.println("HP: " + stats.hp + "/" + stats.maxHp);
            }
        }
        
        // Debug: Heal player (press H)
        if (e.getKeyCode() == KeyEvent.VK_H) {
            Entity player = gameState.getPlayer();
            Stats stats = player.getComponent(Stats.class);
            if (stats != null) {
                stats.hp = stats.maxHp;
                System.out.println("HP: " + stats.hp + "/" + stats.maxHp + " (HEALED)");
            }
        }
        
        // Full heal command (press F)
        if (e.getKeyCode() == KeyEvent.VK_F) {
            Entity player = gameState.getPlayer();
            Stats stats = player.getComponent(Stats.class);
            Position pos = player.getComponent(Position.class);
            
            if (stats != null) {
                stats.fullHeal();
                System.out.println("DEBUG: Full heal!");
                System.out.println("HP: " + stats.hp + "/" + stats.maxHp);
                System.out.println("Mana: " + stats.mana + "/" + stats.maxMana);
                System.out.println("Stamina: " + (int)stats.stamina + "/" + (int)stats.maxStamina);
                
                if (pos != null) {
                    DamageText healText = new DamageText(
                        "HEALED!",
                        DamageText.Type.HEAL,
                        pos.x,
                        pos.y - 30
                    );
                    gameState.addDamageText(healText);
                }
            }
        }
        
        // Add XP for testing (press X)
        if (e.getKeyCode() == KeyEvent.VK_X) {
            Entity player = gameState.getPlayer();
            Experience exp = player.getComponent(Experience.class);
            Stats stats = player.getComponent(Stats.class);
            
            if (exp != null && stats != null) {
                int xpGain = 100;
                System.out.println("DEBUG: Adding " + xpGain + " XP");
                
                int levelsGained = exp.addExperience(xpGain);
                
                if (levelsGained > 0) {
                    stats.applyLevelStats(exp, true);
                    
                    LevelUpEffect levelUpEffect = player.getComponent(LevelUpEffect.class);
                    if (levelUpEffect != null) {
                        levelUpEffect.trigger(exp.level);
                    }
                    
                    Position pos = player.getComponent(Position.class);
                    if (pos != null) {
                        DamageText levelText = new DamageText(
                            "LEVEL UP! " + exp.level,
                            DamageText.Type.HEAL,
                            pos.x,
                            pos.y - 40
                        );
                        gameState.addDamageText(levelText);
                        
                        DamageText healText = new DamageText(
                            "FULLY HEALED!",
                            DamageText.Type.HEAL,
                            pos.x,
                            pos.y - 20
                        );
                        gameState.addDamageText(healText);
                    }
                    
                    System.out.println("LEVEL UP to " + exp.level + "!");
                    System.out.println("HP: " + stats.hp + "/" + stats.maxHp + " (FULL!)");
                    System.out.println("Mana: " + stats.mana + "/" + stats.maxMana + " (FULL!)");
                    System.out.println("Stamina: " + (int)stats.stamina + "/" + (int)stats.maxStamina + " (FULL!)");
                }
                
                System.out.println("XP: " + (int)exp.currentXP + "/" + (int)exp.xpToNextLevel);
            }
        }
        
        // Show stats (press S)
        if (e.getKeyCode() == KeyEvent.VK_S) {
            Entity player = gameState.getPlayer();
            Experience exp = player.getComponent(Experience.class);
            Stats stats = player.getComponent(Stats.class);
            
            if (exp != null && stats != null) {
                System.out.println("\n╔═══════════════════════════════════╗");
                System.out.println("       PLAYER STATS");
                System.out.println("╠═══════════════════════════════════╣");
                System.out.println("Level:      " + exp.level);
                System.out.println("XP:         " + (int)exp.currentXP + "/" + (int)exp.xpToNextLevel + 
                                 " (" + (int)(exp.getXPProgress() * 100) + "%)");
                System.out.println("───────────────────────────────────");
                System.out.println("HP:         " + stats.hp + "/" + stats.maxHp);
                System.out.println("Mana:       " + stats.mana + "/" + stats.maxMana + 
                                 " (+" + String.format("%.1f", stats.manaRegenRate) + "/s)");
                System.out.println("Stamina:    " + (int)stats.stamina + "/" + (int)stats.maxStamina);
                System.out.println("  Max Bonus:    " + (int)(stats.maxStaminaBonus * 100) + "%");
                System.out.println("  Regen Bonus:  " + (int)(stats.staminaRegenBonus * 100) + "%");
                System.out.println("  Cost Reduc:   " + (int)(stats.staminaCostReduction * 100) + "%");
                System.out.println("───────────────────────────────────");
                System.out.println("Attack:     " + stats.attack);
                System.out.println("Defense:    " + stats.defense);
                System.out.println("Accuracy:   " + stats.accuracy);
                System.out.println("Evasion:    " + stats.evasion);
                System.out.println("╚═══════════════════════════════════╝\n");
            }
        }
        
        // Test stamina bonuses (press N)
        if (e.getKeyCode() == KeyEvent.VK_N) {
            Entity player = gameState.getPlayer();
            Stats stats = player.getComponent(Stats.class);
            
            if (stats != null) {
                if (stats.maxStaminaBonus == 0f) {
                    stats.maxStaminaBonus = 0.5f;
                    stats.calculateMaxStamina();
                    System.out.println("DEBUG: Added +50% Max Stamina bonus");
                    System.out.println("Max Stamina: " + (int)stats.maxStamina);
                } else if (stats.staminaRegenBonus == 0f) {
                    stats.staminaRegenBonus = 0.5f;
                    System.out.println("DEBUG: Added +50% Stamina Regen bonus");
                } else if (stats.staminaCostReduction == 0f) {
                    stats.staminaCostReduction = 0.3f;
                    System.out.println("DEBUG: Added 30% Stamina Cost Reduction");
                } else {
                    stats.maxStaminaBonus = 0f;
                    stats.staminaRegenBonus = 0f;
                    stats.staminaCostReduction = 0f;
                    stats.calculateMaxStamina();
                    System.out.println("DEBUG: Reset all stamina bonuses");
                    System.out.println("Max Stamina: " + (int)stats.maxStamina);
                }
            }
        }
        
        // Print monster states (press F4)
        if (e.getKeyCode() == KeyEvent.VK_F4) {
            System.out.println("\n=== MONSTER STATES ===");
            for (Entity entity : gameState.getEntities()) {
                if (entity.getType() == EntityType.MONSTER) {
                    AI ai = entity.getComponent(AI.class);
                    Position pos = entity.getComponent(Position.class);
                    Movement move = entity.getComponent(Movement.class);
                    
                    if (ai != null && pos != null) {
                        float distHome = (float)Math.sqrt(
                            Math.pow(pos.x - ai.homeX, 2) + 
                            Math.pow(pos.y - ai.homeY, 2)
                        );
                        
                        String movingStatus = (move != null && move.isMoving) ? "MOVING" : "STOPPED";
                        
                        System.out.println(entity.getName() + " [ID:" + entity.getID() + "]:");
                        System.out.println("  State: " + ai.currentState);
                        System.out.println("  Position: (" + (int)pos.x + ", " + (int)pos.y + ")");
                        System.out.println("  Home: (" + (int)ai.homeX + ", " + (int)ai.homeY + ")");
                        System.out.println("  Dist from home: " + (int)distHome);
                        System.out.println("  Movement: " + movingStatus);
                        
                        Path pathComp = entity.getComponent(Path.class);
                        if (pathComp != null && pathComp.isFollowing) {
                            System.out.println("  Path: " + pathComp.waypoints.size() + " waypoints, at #" + pathComp.currentWaypoint);
                        } else {
                            System.out.println("  Path: NONE");
                        }
                    }
                }
            }
            System.out.println("======================\n");
        }
        
        // Spawn test monsters (M, E, B keys)
        if (e.getKeyCode() == KeyEvent.VK_M) {
            Entity player = gameState.getPlayer();
            Position playerPos = player.getComponent(Position.class);
            Experience exp = player.getComponent(Experience.class);
            
            if (playerPos != null && exp != null) {
                float spawnX = playerPos.x + 100;
                float spawnY = playerPos.y;
                int playerLevel = exp.level;
                
                gameState.spawnMonster("Goblin", spawnX, spawnY, playerLevel, MobTier.NORMAL);
                System.out.println("Spawned Lv" + playerLevel + " NORMAL Goblin for testing");
            }
        }
        
        if (e.getKeyCode() == KeyEvent.VK_E) {
            Entity player = gameState.getPlayer();
            Position playerPos = player.getComponent(Position.class);
            Experience exp = player.getComponent(Experience.class);
            
            if (playerPos != null && exp != null) {
                float spawnX = playerPos.x + 100;
                float spawnY = playerPos.y;
                int playerLevel = exp.level;
                
                gameState.spawnMonster("Goblin", spawnX, spawnY, playerLevel, MobTier.ELITE);
                System.out.println("Spawned Lv" + playerLevel + " ELITE Goblin for testing");
            }
        }
        
        if (e.getKeyCode() == KeyEvent.VK_B) {
            Entity player = gameState.getPlayer();
            Position playerPos = player.getComponent(Position.class);
            Experience exp = player.getComponent(Experience.class);
            
            if (playerPos != null && exp != null) {
                float spawnX = playerPos.x + 100;
                float spawnY = playerPos.y;
                int playerLevel = exp.level;
                
                gameState.spawnMonster("GoblinBoss", spawnX, spawnY, playerLevel, MobTier.MINIBOSS);
                System.out.println("Spawned Lv" + playerLevel + " MINIBOSS for testing");
            }
        }
        
        // Show tier comparison (press T)
        if (e.getKeyCode() == KeyEvent.VK_T) {
            Entity player = gameState.getPlayer();
            Experience exp = player.getComponent(Experience.class);
            Stats playerStats = player.getComponent(Stats.class);
            
            if (exp != null && playerStats != null) {
                int playerLevel = exp.level;
                
                System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
                System.out.println("          TIER COMPARISON - LEVEL " + playerLevel);
                System.out.println("╠═══════════════════════════════════════════════════════════╣");
                System.out.println("PLAYER:");
                System.out.println("  HP: " + playerStats.maxHp + " | ATK: " + playerStats.attack + 
                                 " | DEF: " + playerStats.defense + " | ACC: " + playerStats.accuracy + 
                                 " | EVA: " + playerStats.evasion);
                System.out.println("───────────────────────────────────────────────────────────");
                
                MobTier[] tiers = {MobTier.TRASH, MobTier.NORMAL, MobTier.ELITE, MobTier.MINIBOSS};
                
                for (MobTier tier : tiers) {
                    MobStats mobStats = MobStatFactory.create(playerLevel, tier);
                    MonsterLevel mobLevel = new MonsterLevel(playerLevel, tier);
                    int xpReward = mobLevel.calculateXPReward();
                    
                    System.out.println(tier + " MOB:");
                    System.out.println("  HP: " + mobStats.hp + " | ATK: " + mobStats.attack + 
                                     " | DEF: " + mobStats.defense + " | ACC: " + mobStats.accuracy + 
                                     " | EVA: " + mobStats.evasion);
                    System.out.println("  XP Reward: " + xpReward);
                    
                    float playerHitChance = playerStats.getHitChance(
                        new Stats(mobStats.hp, 50f, mobStats.attack, mobStats.defense)
                    );
                    Stats tempMobStats = new Stats(mobStats.hp, 50f, mobStats.attack, mobStats.defense);
                    tempMobStats.accuracy = mobStats.accuracy;
                    tempMobStats.evasion = mobStats.evasion;
                    float mobHitChance = tempMobStats.getHitChance(playerStats);
                    
                    System.out.println("  Player Hit Rate: " + (int)(playerHitChance * 100) + "% | " +
                                     "Mob Hit Rate: " + (int)(mobHitChance * 100) + "%");
                    System.out.println("───────────────────────────────────────────────────────────");
                }
                System.out.println("╚═══════════════════════════════════════════════════════════╝\n");
            }
        }
    }
    
    public boolean isDebugMode() {
        return debugMode;
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            shiftPressed = false;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}

    public void render() {
        Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
        
        // Clear screen
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, WIDTH, HEIGHT);
         
        // Render game world
        renderer.render(g);
        
        // 
  
        g.dispose();
        bufferStrategy.show();
    }
    
         
    @Override
    public void run() {
        final double timePerUpdate = 1_000_000_000.0 / UPS;
        final double timePerFrame = 1_000_000_000.0 / FPS;

        long previousTime = System.nanoTime();
        double deltaU = 0;
        double deltaF = 0;

        long timer = System.currentTimeMillis();
        int frames = 0;
        int updates = 0;

        while (isRunning) {
            long currentTime = System.nanoTime();
            long elapsed = currentTime - previousTime;
            previousTime = currentTime;

            deltaU += elapsed / timePerUpdate;
            deltaF += elapsed / timePerFrame;

            // Fixed update loop - game logic runs at exactly UPS rate
            while (deltaU >= 1) { 
                update(1f / UPS);
                updates++;
                deltaU--;
            }

            // Render loop - renders as fast as FPS allows
            if (deltaF >= 1) {
                render();
                frames++;
                deltaF--;
            }

            // ★ NEW: Update UI hover states
            // ⭐ Only check UI hover when mouse actually moved
            if (mouse.hasMoved()) {
                gameState.getUIManager().handleMouseMove(mouse.getX(), mouse.getY());
                mouse.resetMoved();
            }

            // Prevent CPU maxing
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Debug output
            if (System.currentTimeMillis() - timer >= 1000) {
                timer += 1000;
               // System.out.println("FPS: " + frames + " | UPS: " + updates);
                frames = 0;
                updates = 0;
            }
        }
    }

    public void start() {
        if (!isRunning) {
            isRunning = true;
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (isRunning) {
            isRunning = false;
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Getters
    public int getWidth() { return WIDTH; }
    public int getHeight() { return HEIGHT; }
    public MouseInput getMouse() { return mouse; }
    public GameState getGameState() { return gameState; }
    
    public static void main(String[] args) {
        new Engine().start();
    }
}
 
/*
## Key Changes I Made

1. **Added `Renderer` class reference** - Separates rendering from Engine
2. **Added `handleInput()` method** - Converts mouse clicks to game commands
3. **World coordinate conversion** - Accounts for camera position
4. **Proper initialization** - Actually creates GameState/GameLogic instances
5. **Moved debug grid to separate method** - Cleaner code

## Architecture Summary

Your refactored Engine now follows this clean separation:
```
Engine (Orchestrator)
  ↓
  ├─→ GameState (Data)
  ├─→ GameLogic (Rules) ──→ modifies GameState
  ├─→ Renderer (Display) ──→ reads GameState
  └─→ MouseInput (Input) ──→ converted to commands for GameLogic

*/