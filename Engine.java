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
    
    public void update(float delta) {
        // Check mouse hover
        handleMouseHover();
        
        // Handle input
        handleInput();
        
        // Update game logic
        gameLogic.update(delta);
        
        // ★ NEW: Update UI
        gameState.getUIManager().update(delta);
    }
    
    private void handleMouseHover() {
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
    }
    
    private void handleInput() {
        if (mouse.isLeftClick()) {
            int screenX = mouse.getX();
            int screenY = mouse.getY();
            
            // ★ NEW: Check UI clicks first - if consumed, don't process world clicks
            boolean uiConsumedClick = gameState.getUIManager().handleClick(screenX, screenY);
            
            if (!uiConsumedClick) {
                // UI didn't consume the click, process world click
                float worldX = screenX + gameState.getCameraX();
                float worldY = screenY + gameState.getCameraY();
                
                Entity hoveredEntity = gameState.getHoveredEntity();
                
                if (hoveredEntity != null && hoveredEntity.getType() == EntityType.MONSTER) {
                    Stats stats = hoveredEntity.getComponent(Stats.class);
                    if (stats != null && stats.hp > 0) {
                        gameState.setTargetedEntity(hoveredEntity);
                        gameLogic.playerAttack(hoveredEntity);
                        System.out.println("Auto-attacking " + hoveredEntity.getName());
                    }
                } else {
                    gameLogic.stopAutoAttack();
                    gameLogic.movePlayerTo(worldX, worldY, shiftPressed);
                }
            }
            
            mouse.resetPressed();
        }
        
        if (mouse.isRightClick()) {
            int screenX = mouse.getX();
            int screenY = mouse.getY();
            
            // ★ NEW: Check UI right clicks - if consumed, don't process world clicks
            boolean uiConsumedClick = gameState.getUIManager().handleRightClick(screenX, screenY);
            
            if (!uiConsumedClick) {
                // UI didn't consume the click, process world click
                float worldX = screenX + gameState.getCameraX();
                float worldY = screenY + gameState.getCameraY();
                
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
        
        // ★ NEW: Handle skill hotkeys
        gameState.getUIManager().handleKeyPress(e.getKeyCode());
        
        // Debug keys
        if (e.getKeyCode() == KeyEvent.VK_D) {
            Entity player = gameState.getPlayer();
            Stats stats = player.getComponent(Stats.class);
            if (stats != null) {
                stats.hp -= 10;
                if (stats.hp < 0) stats.hp = 0;
                System.out.println("HP: " + stats.hp + "/" + stats.maxHp);
            }
        }
        
        if (e.getKeyCode() == KeyEvent.VK_H) {
            Entity player = gameState.getPlayer();
            Stats stats = player.getComponent(Stats.class);
            if (stats != null) {
                stats.hp = stats.maxHp;
                System.out.println("HP: " + stats.hp + "/" + stats.maxHp + " (HEALED)");
            }
        }
     // NEW: F4 to print all monster states
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
                        
                        Path path = entity.getComponent(Path.class);
                        if (path != null && path.isFollowing) {
                            System.out.println("  Path: " + path.waypoints.size() + " waypoints, at #" + path.currentWaypoint);
                        } else {
                            System.out.println("  Path: NONE");
                        }
                    }
                }
            }
            System.out.println("======================\n");
        }
        
     // Add XP for testing (press X)
        if (e.getKeyCode() == KeyEvent.VK_X) {
            Entity player = gameState.getPlayer();
            Experience exp = player.getComponent(Experience.class);
            Stats stats = player.getComponent(Stats.class);
            
            if (exp != null && stats != null) {
                // Add 100 XP for testing
                int xpGain = 100;
                System.out.println("DEBUG: Adding " + xpGain + " XP");
                
                int levelsGained = exp.addExperience(xpGain);
                
                if (levelsGained > 0) {
                    // ★ Full heal on level up
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
                        
                        // ★ Add heal text
                        DamageText healText = new DamageText(
                            "FULLY HEALED!",
                            DamageText.Type.HEAL,
                            pos.x,
                            pos.y - 20
                        );
                        gameState.addDamageText(healText);
                    }
                    
                    System.out.println("LEVEL UP to " + exp.level + "!");
                    System.out.println("HP: " + stats.hp + "/" + stats.maxHp + " (FULL!) | " +
                                     "Stamina: " + (int)stats.stamina + "/" + (int)stats.maxStamina + " (FULL!)");
                    System.out.println("ATK: " + stats.attack + " | DEF: " + stats.defense + 
                                     " | ACC: " + stats.accuracy);
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
                System.out.println("\n═══════════════════════════════");
                System.out.println("       PLAYER STATS");
                System.out.println("═══════════════════════════════");
                System.out.println("Level:      " + exp.level);
                System.out.println("XP:         " + (int)exp.currentXP + "/" + (int)exp.xpToNextLevel + 
                                 " (" + (int)(exp.getXPProgress() * 100) + "%)");
                System.out.println("───────────────────────────────");
                System.out.println("HP:         " + stats.hp + "/" + stats.maxHp);
                System.out.println("Stamina:    " + (int)stats.stamina + "/" + (int)stats.maxStamina);
                System.out.println("Attack:     " + stats.attack);
                System.out.println("Defense:    " + stats.defense);
                System.out.println("Accuracy:   " + stats.accuracy);
                System.out.println("Evasion:    " + stats.evasion);
                System.out.println("───────────────────────────────");
                System.out.println("Magic ATK:  " + stats.magicAttack);
                System.out.println("Magic DEF:  " + stats.magicDefense);
                System.out.println("───────────────────────────────");
                System.out.println("Fire Res:   " + stats.fireResistance + "%");
                System.out.println("Light Res:  " + stats.lightningResistance + "%");
                System.out.println("Poison Res: " + stats.poisonResistance + "%");
                System.out.println("───────────────────────────────");
                System.out.println("Silence:    " + stats.silenceResistance + "%");
                System.out.println("Blind:      " + stats.blindResistance + "%");
                System.out.println("Curse:      " + stats.curseResistance + "%");
                System.out.println("═══════════════════════════════\n");
            }
        }
     
     // ★ NEW: Full heal command (press F)
        if (e.getKeyCode() == KeyEvent.VK_F) {
            Entity player = gameState.getPlayer();
            Stats stats = player.getComponent(Stats.class);
            Position pos = player.getComponent(Position.class);
            
            if (stats != null) {
                stats.fullHeal();
                System.out.println("DEBUG: Full heal!");
                System.out.println("HP: " + stats.hp + "/" + stats.maxHp);
                System.out.println("Stamina: " + (int)stats.stamina + "/" + (int)stats.maxStamina);
                
                // Visual feedback
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
  // Show tier comparison (press T)
     if (e.getKeyCode() == KeyEvent.VK_T) {
         Entity player = gameState.getPlayer();
         Experience exp = player.getComponent(Experience.class);
         Stats playerStats = player.getComponent(Stats.class);
         
         if (exp != null && playerStats != null) {
             int playerLevel = exp.level;
             
             System.out.println("\n═══════════════════════════════════════════════════════════");
             System.out.println("          TIER COMPARISON - LEVEL " + playerLevel);
             System.out.println("═══════════════════════════════════════════════════════════");
             System.out.println("PLAYER:");
             System.out.println("  HP: " + playerStats.maxHp + " | ATK: " + playerStats.attack + 
                              " | DEF: " + playerStats.defense + " | ACC: " + playerStats.accuracy + 
                              " | EVA: " + playerStats.evasion);
             System.out.println("───────────────────────────────────────────────────────────");
             
             // Show each tier at player's level
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
                 
                 // Calculate player vs mob hit chances
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
             System.out.println("═══════════════════════════════════════════════════════════\n");
         }
     }

     // Spawn test monster (press M)
     if (e.getKeyCode() == KeyEvent.VK_M) {
         Entity player = gameState.getPlayer();
         Position playerPos = player.getComponent(Position.class);
         Experience exp = player.getComponent(Experience.class);
         
         if (playerPos != null && exp != null) {
             // Spawn a NORMAL tier goblin at player level, near player
             float spawnX = playerPos.x + 100;
             float spawnY = playerPos.y;
             int playerLevel = exp.level;
             
             gameState.spawnMonster("Goblin", spawnX, spawnY, playerLevel, MobTier.NORMAL);
             System.out.println("Spawned Lv" + playerLevel + " NORMAL Goblin for testing");
         }
     }

     // Spawn ELITE test monster (press E)
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

     // Spawn MINIBOSS test monster (press B)
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
            gameState.getUIManager().handleMouseMove(mouse.getX(), mouse.getY());
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