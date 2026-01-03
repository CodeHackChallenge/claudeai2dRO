package dev.main;
 

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
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
            attackCursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
            
            // To use custom cursor image:
            // Toolkit toolkit = Toolkit.getDefaultToolkit();
            // Image cursorImage = toolkit.getImage("resources/cursors/attack.png");
            // attackCursor = toolkit.createCustomCursor(cursorImage, new Point(16, 16), "attack");
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
        renderer = new Renderer(gameState, this);  // Pass 'this' (Engine) reference
        
        System.out.println("Game initialized!");
    }
    
    public void update(float delta) {
        // Check mouse hover
        handleMouseHover();
        
        // Handle input
        handleInput();
        
        // Update game logic
        gameLogic.update(delta);
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
            
            float worldX = screenX + gameState.getCameraX();
            float worldY = screenY + gameState.getCameraY();
            
            Entity hoveredEntity = gameState.getHoveredEntity();
            
            if (hoveredEntity != null && hoveredEntity.getType() == EntityType.MONSTER) {
                // Check if target is alive
                Stats stats = hoveredEntity.getComponent(Stats.class);
                if (stats != null && stats.hp > 0) {
                    // Attack monster (sets as auto-attack target)
                    gameState.setTargetedEntity(hoveredEntity);
                    gameLogic.playerAttack(hoveredEntity);
                    System.out.println("Auto-attacking " + hoveredEntity.getName());
                }
            } else {
                // Clicked ground - stop auto-attack and move
                gameLogic.stopAutoAttack();
                gameLogic.movePlayerTo(worldX, worldY, shiftPressed);
            }
            
            mouse.resetPressed();
        }
        
        if (mouse.isRightClick()) {
            // Right-click always stops auto-attack and moves
            int screenX = mouse.getX();
            int screenY = mouse.getY();
            
            float worldX = screenX + gameState.getCameraX();
            float worldY = screenY + gameState.getCameraY();
            
            gameLogic.stopAutoAttack();
            gameLogic.movePlayerTo(worldX, worldY, shiftPressed);
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