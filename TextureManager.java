package dev.main;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class TextureManager {

    private static final Map<String, BufferedImage> cache = new HashMap<>();

    // Load and cache an image
    public static BufferedImage load(String path) {
        if (cache.containsKey(path)) {
            return cache.get(path);
        }

        try {
            BufferedImage img = ImageIO.read(TextureManager.class.getResourceAsStream(path));
            cache.put(path, img);
            return img;
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Failed to load texture: " + path);
            return null;
        }
    }

    // Optional: clear cache (useful for dev reloads)
    public static void clear() {
        cache.clear();
    }
}