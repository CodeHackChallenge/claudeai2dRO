package dev.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
 

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class TileMapMaker extends JFrame {
    private static final int TILE_SIZE = 64;
    private int mapWidth = 50;
    private int mapHeight = 50;
    
    private int[][] tileMap;
    private MapPanel mapPanel;
    private JScrollPane scrollPane;
    private int currentTile = 0; // 0 = walkable, 1 = solid
    private BufferedImage referenceImage;
    
    public TileMapMaker() {
        setTitle("2D Tile Map Maker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Show dialog to set map dimensions
        showDimensionsDialog();
        
        // Initialize tile map
        tileMap = new int[mapHeight][mapWidth];
        
        // Create map panel
        mapPanel = new MapPanel();
        scrollPane = new JScrollPane(mapPanel);
        scrollPane.setPreferredSize(new Dimension(800, 600));
        add(scrollPane, BorderLayout.CENTER);
        
        // Create control panel
        JPanel controlPanel = new JPanel();
        
        JButton walkableBtn = new JButton("Walkable (0)");
        walkableBtn.setBackground(Color.GREEN);
        walkableBtn.addActionListener(e -> currentTile = 0);
        
        JButton solidBtn = new JButton("Solid (1)");
        solidBtn.setBackground(Color.RED);
        solidBtn.addActionListener(e -> currentTile = 1);
        
        JButton loadImageBtn = new JButton("Load Reference Image");
        loadImageBtn.addActionListener(e -> loadReferenceImage());
        
        JButton clearImageBtn = new JButton("Clear Reference");
        clearImageBtn.addActionListener(e -> {
            referenceImage = null;
            mapPanel.repaint();
        });
        
        JButton saveBtn = new JButton("Save Map");
        saveBtn.addActionListener(e -> saveMap());
        
        JButton loadBtn = new JButton("Load Map");
        loadBtn.addActionListener(e -> loadMap());
        
        JButton clearBtn = new JButton("Clear All");
        clearBtn.addActionListener(e -> {
            for (int i = 0; i < mapHeight; i++) {
                for (int j = 0; j < mapWidth; j++) {
                    tileMap[i][j] = 0;
                }
            }
            mapPanel.repaint();
        });
        
        JButton resizeBtn = new JButton("Resize Map");
        resizeBtn.addActionListener(e -> resizeMap());
        
        controlPanel.add(walkableBtn);
        controlPanel.add(solidBtn);
        controlPanel.add(loadImageBtn);
        controlPanel.add(clearImageBtn);
        controlPanel.add(saveBtn);
        controlPanel.add(loadBtn);
        controlPanel.add(clearBtn);
        controlPanel.add(resizeBtn);
        
        add(controlPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private void showDimensionsDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField widthField = new JTextField(String.valueOf(mapWidth), 10);
        JTextField heightField = new JTextField(String.valueOf(mapHeight), 10);
        
        panel.add(new JLabel("Map Width:"));
        panel.add(widthField);
        panel.add(new JLabel("Map Height:"));
        panel.add(heightField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Set Map Dimensions", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                int newWidth = Integer.parseInt(widthField.getText().trim());
                int newHeight = Integer.parseInt(heightField.getText().trim());
                
                if (newWidth > 0 && newWidth <= 200 && newHeight > 0 && newHeight <= 200) {
                    mapWidth = newWidth;
                    mapHeight = newHeight;
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Dimensions must be between 1 and 200", 
                        "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    mapWidth = 50;
                    mapHeight = 50;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid number format. Using default 50x50", 
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
                mapWidth = 50;
                mapHeight = 50;
            }
        }
    }
    
    private void resizeMap() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField widthField = new JTextField(String.valueOf(mapWidth), 10);
        JTextField heightField = new JTextField(String.valueOf(mapHeight), 10);
        
        panel.add(new JLabel("Map Width:"));
        panel.add(widthField);
        panel.add(new JLabel("Map Height:"));
        panel.add(heightField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Resize Map", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                int newWidth = Integer.parseInt(widthField.getText().trim());
                int newHeight = Integer.parseInt(heightField.getText().trim());
                
                if (newWidth > 0 && newWidth <= 200 && newHeight > 0 && newHeight <= 200) {
                    // Create new tile map with new dimensions
                    int[][] newTileMap = new int[newHeight][newWidth];
                    
                    // Copy existing data
                    for (int i = 0; i < Math.min(mapHeight, newHeight); i++) {
                        for (int j = 0; j < Math.min(mapWidth, newWidth); j++) {
                            newTileMap[i][j] = tileMap[i][j];
                        }
                    }
                    
                    mapWidth = newWidth;
                    mapHeight = newHeight;
                    tileMap = newTileMap;
                    
                    // Update panel
                    mapPanel.setPreferredSize(new Dimension(mapWidth * TILE_SIZE, mapHeight * TILE_SIZE));
                    mapPanel.revalidate();
                    mapPanel.repaint();
                    
                    JOptionPane.showMessageDialog(this, "Map resized successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Dimensions must be between 1 and 200", 
                        "Invalid Input", JOptionPane.WARNING_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid number format", 
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    private void loadReferenceImage() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Image files", "jpg", "jpeg", "png", "gif", "bmp");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                referenceImage = ImageIO.read(fileChooser.getSelectedFile());
                mapPanel.repaint();
                JOptionPane.showMessageDialog(this, 
                    "Reference image loaded! It will be displayed behind the grid.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error loading image: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void saveMap() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text files", "txt");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }
            
            try (PrintWriter writer = new PrintWriter(file)) {
                // Write width and height as first line
                writer.println(mapWidth + " " + mapHeight);
                
                // Write tile data
                for (int i = 0; i < mapHeight; i++) {
                    for (int j = 0; j < mapWidth; j++) {
                        writer.print(tileMap[i][j]);
                        if (j < mapWidth - 1) writer.print(" ");
                    }
                    writer.println();
                }
                JOptionPane.showMessageDialog(this, "Map saved successfully!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error saving map: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void loadMap() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text files", "txt");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader reader = new BufferedReader(
                    new FileReader(fileChooser.getSelectedFile()))) {
                
                // Read width and height from first line
                String firstLine = reader.readLine();
                if (firstLine != null) {
                    String[] dimensions = firstLine.trim().split("\\s+");
                    int width = Integer.parseInt(dimensions[0]);
                    int height = Integer.parseInt(dimensions[1]);
                    
                    // Resize map to match loaded dimensions
                    mapWidth = width;
                    mapHeight = height;
                    tileMap = new int[mapHeight][mapWidth];
                    
                    // Read tile data
                    String line;
                    int row = 0;
                    while ((line = reader.readLine()) != null && row < height) {
                        String[] tokens = line.trim().split("\\s+");
                        for (int col = 0; col < Math.min(tokens.length, width); col++) {
                            tileMap[row][col] = Integer.parseInt(tokens[col]);
                        }
                        row++;
                    }
                    
                    // Update panel
                    mapPanel.setPreferredSize(new Dimension(mapWidth * TILE_SIZE, mapHeight * TILE_SIZE));
                    mapPanel.revalidate();
                    mapPanel.repaint();
                    
                    JOptionPane.showMessageDialog(this, "Map loaded successfully!");
                }
            } catch (IOException | NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error loading map: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private class MapPanel extends JPanel {
        public MapPanel() {
            setPreferredSize(new Dimension(mapWidth * TILE_SIZE, mapHeight * TILE_SIZE));
            
            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    handleMouseEvent(e);
                }
                
                @Override
                public void mouseDragged(MouseEvent e) {
                    handleMouseEvent(e);
                }
                
                private void handleMouseEvent(MouseEvent e) {
                    int col = e.getX() / TILE_SIZE;
                    int row = e.getY() / TILE_SIZE;
                    
                    if (row >= 0 && row < mapHeight && col >= 0 && col < mapWidth) {
                        tileMap[row][col] = currentTile;
                        repaint();
                    }
                }
            };
            
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // Draw reference image if loaded
            if (referenceImage != null) {
                g.drawImage(referenceImage, 0, 0, 
                    mapWidth * TILE_SIZE, mapHeight * TILE_SIZE, this);
            }
            
            // Draw tiles
            for (int row = 0; row < mapHeight; row++) {
                for (int col = 0; col < mapWidth; col++) {
                    int x = col * TILE_SIZE;
                    int y = row * TILE_SIZE;
                    
                    if (tileMap[row][col] == 0) {
                        g.setColor(new Color(0, 255, 0, 128)); // Semi-transparent green
                    } else {
                        g.setColor(new Color(255, 0, 0, 128)); // Semi-transparent red
                    }
                    g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                }
            }
            
            // Draw grid
            g.setColor(Color.BLACK);
            for (int i = 0; i <= mapHeight; i++) {
                g.drawLine(0, i * TILE_SIZE, mapWidth * TILE_SIZE, i * TILE_SIZE);
            }
            for (int i = 0; i <= mapWidth; i++) {
                g.drawLine(i * TILE_SIZE, 0, i * TILE_SIZE, mapHeight * TILE_SIZE);
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TileMapMaker().setVisible(true);
        });
    }
}
