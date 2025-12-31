package dev.main;

import java.util.HashMap;
import java.util.Map;

public class Entity {

    private static int nextID = 0;  // Auto-incrementing ID generator
    
    public final int ID;
    private String name;
    
    // Stores components by their type
    private final Map<Class<? extends Component>, Component> components = new HashMap<>();

    // Constructor with auto-generated ID
    public Entity() {
        this.ID = nextID++;
        this.name = "Entity_" + ID;
    }
    
    // Constructor with auto-generated ID and custom name
    public Entity(String name) {
        this.ID = nextID++;
        this.name = name;
    }

    // Constructor with specific ID (useful for network sync later)
    public Entity(int id, String name) {
        this.ID = id;
        this.name = name;
        
        // Update nextID if this ID is higher (for MMO sync)
        if (id >= nextID) {
            nextID = id + 1;
        }
    }

    // Component management
    public <T extends Component> void addComponent(T component) {
        components.put(component.getClass(), component);
    }

    public <T extends Component> T getComponent(Class<T> type) {
        return type.cast(components.get(type));
    }

    public <T extends Component> void removeComponent(Class<T> type) {
        components.remove(type);
    }

    public <T extends Component> boolean hasComponent(Class<T> type) {
        return components.containsKey(type);
    }

    // Getters
    public int getID() {
        return ID;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    // Useful for debugging
    @Override
    public String toString() {
        return name + " (ID: " + ID + ")";
    }
}