package dev.main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TileMap {
    
    public static final int TILE_SIZE = 64;
    
    private int width;   // Map width in tiles
    private int height;  // Map height in tiles
    private int[][] tiles;  // 2D array of tile IDs
    
    // Tile textures
    private BufferedImage dirtTexture;
    private BufferedImage grassTexture;
    
    public TileMap(String mapFilePath) {
        loadTileTextures();
        loadMap(mapFilePath);
    }
    /*
    private void loadTileTextures() {
        // Load your tile textures
        dirtTexture = TextureManager.load("/tiles/dirt.png");
        grassTexture = TextureManager.load("/tiles/grass.png");
        
        // TODO: If you don't have textures yet, create placeholder colors
        // See createPlaceholderTile() method below
    }
    */
    private void loadTileTextures() {
        // Try to load real textures, fallback to placeholders
        dirtTexture = TextureManager.load("/tiles/dirt.png");
        grassTexture = TextureManager.load("/tiles/grass.png");
        
        if (dirtTexture == null) {
            dirtTexture = createPlaceholderTile(new Color(139, 90, 43));  // Brown
        }
        if (grassTexture == null) {
            grassTexture = createPlaceholderTile(new Color(34, 139, 34));  // Green
        }
    }
    
    private void loadMap(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            
            // First line: width height
            String[] dimensions = br.readLine().trim().split(" ");
            width = Integer.parseInt(dimensions[0]);
            height = Integer.parseInt(dimensions[1]);
            
            tiles = new int[height][width];
            
            // Read tile data
            for (int row = 0; row < height; row++) {
                String line = br.readLine();
                String[] values = line.trim().split(" ");
                
                for (int col = 0; col < width; col++) {
                    tiles[row][col] = Integer.parseInt(values[col]);
                }
            }
            
            br.close();
            System.out.println("Map loaded: " + width + "x" + height + " tiles");
            
        } catch (IOException e) {
            System.err.println("Failed to load map: " + path);
            e.printStackTrace();
        }
    }
    
    public void render(Graphics2D g, float cameraX, float cameraY) {
        // Calculate which tiles are visible
        int startCol = Math.max(0, (int)(cameraX / TILE_SIZE));
        int endCol = Math.min(width, (int)((cameraX + Engine.WIDTH) / TILE_SIZE) + 1);
        
        int startRow = Math.max(0, (int)(cameraY / TILE_SIZE));
        int endRow = Math.min(height, (int)((cameraY + Engine.HEIGHT) / TILE_SIZE) + 1);
        
        // Only render visible tiles
        for (int row = startRow; row < endRow; row++) {
            for (int col = startCol; col < endCol; col++) {
                
                int tileID = tiles[row][col];
                BufferedImage texture = getTileTexture(tileID);
                
                if (texture != null) {
                    int x = col * TILE_SIZE - (int)cameraX;
                    int y = row * TILE_SIZE - (int)cameraY;
                    
                    g.drawImage(texture, x, y, TILE_SIZE, TILE_SIZE, null);
                }
            }
        }
    }
    
    private BufferedImage getTileTexture(int tileID) {
        switch(tileID) {
            case 0: return dirtTexture;
            case 1: return grassTexture;
            default: return dirtTexture;
        }
    }
    
    // Collision detection
    public boolean isSolid(int tileX, int tileY) {
        if (tileX < 0 || tileX >= width || tileY < 0 || tileY >= height) {
            return true;  // Out of bounds = solid
        }
        
        int tileID = tiles[tileY][tileX];
        // TODO: Define which tiles are solid
        // For now, all tiles are walkable
        return false;
    } 
    
    private BufferedImage createPlaceholderTile(Color color) {
        BufferedImage img = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
        //g.setColor(Color.DARK_GRAY);
        //g.drawRect(0, 0, TILE_SIZE - 1, TILE_SIZE - 1);  // Border
        g.dispose();
        return img;
    }
    public boolean isSolidAtWorldPos(float worldX, float worldY) {
        int tileX = (int)(worldX / TILE_SIZE);
        int tileY = (int)(worldY / TILE_SIZE);
        return isSolid(tileX, tileY);
    }
    
    // Getters
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getWidthInPixels() { return width * TILE_SIZE; }
    public int getHeightInPixels() { return height * TILE_SIZE; }
}