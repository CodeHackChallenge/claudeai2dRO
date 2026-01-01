package dev.main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
///tiles/tilesheet_64x64.png
public class TileMap {
    
    public static final int TILE_SIZE = 64;
    
    // Tile type constants
    public static final int TILE_DIRT = 0;
    public static final int TILE_GRASS = 1;
    public static final int TILE_WATER = 2;
    public static final int TILE_ROCK = 3;
    
    private int width;
    private int height;
    private int[][] tiles;
    
    private BufferedImage tileSheet;
    private int tilesPerRow;
    
    public TileMap(String mapFilePath) {
        loadTileSheet();
        loadMap(mapFilePath);
    }
    
    private void loadTileSheet() {
        tileSheet = TextureManager.load("/tiles/tilesheet_64x64.png");
        
        if (tileSheet != null) {
            tilesPerRow = tileSheet.getWidth() / TILE_SIZE;
            System.out.println("Tile sheet loaded: " + tilesPerRow + " tiles per row");
        } else {
            System.err.println("Failed to load tile sheet!");
        }
    }
    
    private void loadMap(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            
            String[] dimensions = br.readLine().trim().split(" ");
            width = Integer.parseInt(dimensions[0]);
            height = Integer.parseInt(dimensions[1]);
            
            tiles = new int[height][width];
            
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
        if (tileSheet == null) return;
        
        int startCol = Math.max(0, (int)(cameraX / TILE_SIZE));
        int endCol = Math.min(width, (int)((cameraX + Engine.WIDTH) / TILE_SIZE) + 1);
        
        int startRow = Math.max(0, (int)(cameraY / TILE_SIZE));
        int endRow = Math.min(height, (int)((cameraY + Engine.HEIGHT) / TILE_SIZE) + 1);
        
        for (int row = startRow; row < endRow; row++) {
            for (int col = startCol; col < endCol; col++) {
                
                int tileID = tiles[row][col];
                
                int srcX = (tileID % tilesPerRow) * TILE_SIZE;
                int srcY = (tileID / tilesPerRow) * TILE_SIZE;
                
                int destX = col * TILE_SIZE - (int)cameraX;
                int destY = row * TILE_SIZE - (int)cameraY;
                
                g.drawImage(
                    tileSheet,
                    destX, destY, destX + TILE_SIZE, destY + TILE_SIZE,
                    srcX, srcY, srcX + TILE_SIZE, srcY + TILE_SIZE,
                    null
                );
            }
        }
    }
    
    // Check if a specific tile is solid
    public boolean isTileSolid(int tileID) {
        return tileID == TILE_WATER || tileID == TILE_ROCK;
    }
    
    public boolean isSolid(int tileX, int tileY) {
        if (tileX < 0 || tileX >= width || tileY < 0 || tileY >= height) {
            return true;  // Out of bounds = solid
        }
        
        int tileID = tiles[tileY][tileX];
        return isTileSolid(tileID);
    }
    
    public boolean isSolidAtWorldPos(float worldX, float worldY) {
        int tileX = (int)(worldX / TILE_SIZE);
        int tileY = (int)(worldY / TILE_SIZE);
        return isSolid(tileX, tileY);
    }
    
    // Check if a collision box collides with any solid tiles
    public boolean collidesWithTiles(CollisionBox box, float entityX, float entityY) {
        // Get the bounds of the collision box
        float left = box.getLeft(entityX);
        float right = box.getRight(entityX);
        float top = box.getTop(entityY);
        float bottom = box.getBottom(entityY);
        
        // Convert to tile coordinates
        int startTileX = (int)(left / TILE_SIZE);
        int endTileX = (int)(right / TILE_SIZE);
        int startTileY = (int)(top / TILE_SIZE);
        int endTileY = (int)(bottom / TILE_SIZE);
        
        // Check all tiles the box overlaps
        for (int ty = startTileY; ty <= endTileY; ty++) {
            for (int tx = startTileX; tx <= endTileX; tx++) {
                if (isSolid(tx, ty)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getWidthInPixels() { return width * TILE_SIZE; }
    public int getHeightInPixels() { return height * TILE_SIZE; }
}