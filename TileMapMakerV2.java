import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class TileMapMaker extends JFrame {
    private static final int TILE_SIZE = 64;
    private static final int MAP_WIDTH = 50;
    private static final int MAP_HEIGHT = 50;
    
    private int[][] tileMap;
    private MapPanel mapPanel;
    private int currentTile = 0; // 0 = walkable, 1 = solid
    private BufferedImage referenceImage;
    
    public TileMapMaker() {
        setTitle("2D Tile Map Maker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Initialize tile map
        tileMap = new int[MAP_HEIGHT][MAP_WIDTH];
        
        // Create map panel
        mapPanel = new MapPanel();
        JScrollPane scrollPane = new JScrollPane(mapPanel);
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
            for (int i = 0; i < MAP_HEIGHT; i++) {
                for (int j = 0; j < MAP_WIDTH; j++) {
                    tileMap[i][j] = 0;
                }
            }
            mapPanel.repaint();
        });
        
        controlPanel.add(walkableBtn);
        controlPanel.add(solidBtn);
        controlPanel.add(loadImageBtn);
        controlPanel.add(clearImageBtn);
        controlPanel.add(saveBtn);
        controlPanel.add(loadBtn);
        controlPanel.add(clearBtn);
        
        add(controlPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
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
                writer.println(MAP_WIDTH + " " + MAP_HEIGHT);
                
                // Write tile data
                for (int i = 0; i < MAP_HEIGHT; i++) {
                    for (int j = 0; j < MAP_WIDTH; j++) {
                        writer.print(tileMap[i][j]);
                        if (j < MAP_WIDTH - 1) writer.print(" ");
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
                    
                    // Read tile data
                    String line;
                    int row = 0;
                    while ((line = reader.readLine()) != null && row < Math.min(height, MAP_HEIGHT)) {
                        String[] tokens = line.trim().split("\\s+");
                        for (int col = 0; col < Math.min(tokens.length, Math.min(width, MAP_WIDTH)); col++) {
                            tileMap[row][col] = Integer.parseInt(tokens[col]);
                        }
                        row++;
                    }
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
            setPreferredSize(new Dimension(MAP_WIDTH * TILE_SIZE, MAP_HEIGHT * TILE_SIZE));
            
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
                    
                    if (row >= 0 && row < MAP_HEIGHT && col >= 0 && col < MAP_WIDTH) {
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
                    MAP_WIDTH * TILE_SIZE, MAP_HEIGHT * TILE_SIZE, this);
            }
            
            // Draw tiles
            for (int row = 0; row < MAP_HEIGHT; row++) {
                for (int col = 0; col < MAP_WIDTH; col++) {
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
            for (int i = 0; i <= MAP_HEIGHT; i++) {
                g.drawLine(0, i * TILE_SIZE, MAP_WIDTH * TILE_SIZE, i * TILE_SIZE);
            }
            for (int i = 0; i <= MAP_WIDTH; i++) {
                g.drawLine(i * TILE_SIZE, 0, i * TILE_SIZE, MAP_HEIGHT * TILE_SIZE);
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TileMapMaker().setVisible(true);
        });
    }
}
