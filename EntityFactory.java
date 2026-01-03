package dev.main;
 
//player.addComponent(new CollisionBox(-10, 3, 22, 28));

public class EntityFactory {
    
    /**
     * Create a player entity
     */
    public static Entity createPlayer(float x, float y) {
        Entity player = new Entity("Player", EntityType.PLAYER);
        
        player.addComponent(new Position(x, y));
        player.addComponent(new Sprite("/sprites/hero2.png", 64, 64, 0.2f));
        player.addComponent(new Movement(100f, 200f));
        player.addComponent(new Stats(100, 100f, 15, 5));
        player.addComponent(new Combat(0.8f, 0.15f, 0.05f));  // 0.8s cooldown, 15% crit, 5% evasion 
        player.addComponent(new HealthBar(40, 4, 40));
        player.addComponent(new StaminaBar(40, 4, 46));
        player.addComponent(new CollisionBox(-10, 3, 22, 28));
        player.addComponent(new Path());
        player.addComponent(new TargetIndicator()); 
        
        return player;
    }
    
    /**
     * Create a monster entity
     */
    public static Entity createMonster(String monsterType, float x, float y) {
        Entity monster = new Entity(monsterType, EntityType.MONSTER);
        
        // Position
        monster.addComponent(new Position(x, y));
        
        // Monster-specific stats based on type
        switch(monsterType) {
            case "Slime":
                monster.addComponent(new Sprite("/sprites/slime.png", 64, 64, 0.2f));
                monster.addComponent(new Stats(50, 50f, 5, 2));
                monster.addComponent(new Combat(1.5f, 0.05f, 0.10f));  // 1.5s cooldown, 5% crit, 10% evasion
                
                monster.addComponent(new Movement(60f, 120f));  // Slower than player
                monster.addComponent(new CollisionBox(-12, -12, 24, 24));
                monster.addComponent(new AI("aggressive", x, y, 128f, 3f));  // 3 tiles detection
                monster.addComponent(new NameTag("Slime", -45));
                
                break;
                
            case "Goblin":
                monster.addComponent(new Sprite("/sprites/goblin.png", 64, 64, 0.2f)); //0.15
                monster.addComponent(new Stats(80, 80f, 8, 4));
                monster.addComponent(new Combat(1.2f, 0.10f, 0.08f));
                
                monster.addComponent(new Movement(80f, 160f));
                monster.addComponent(new CollisionBox(-10, -14, 20, 28));
                monster.addComponent(new AI("aggressive", x, y, 600f, 4f));  // 4 tiles detection
                monster.addComponent(new NameTag("Goblin", -45));
                
                break;
                
            case "Poring":  // Passive creature
                monster.addComponent(new Sprite("/sprites/poring.png", 64, 64, 0.25f));
                monster.addComponent(new Stats(30, 30f, 3, 1));
                monster.addComponent(new Combat(2.0f, 0.02f, 0.15f));
                monster.addComponent(new Movement(40f, 80f));
                monster.addComponent(new CollisionBox(-12, -12, 24, 24));
                monster.addComponent(new AI("passive", x, y, 96f, 0f));  // Doesn't detect/chase
                monster.addComponent(new NameTag("Poring", -45));
                
                break;
                
            default:
                // Default monster
                monster.addComponent(new Sprite("/sprites/monster_default.png", 64, 64, 0.2f));
                monster.addComponent(new Stats(40, 40f, 5, 2));
                monster.addComponent(new Combat(1.5f, 0.05f, 0.08f));
                monster.addComponent(new Movement(50f, 100f));
                monster.addComponent(new CollisionBox(-12, -12, 24, 24));
                monster.addComponent(new AI("neutral", x, y, 128f, 2f));
                monster.addComponent(new NameTag("Monster", -45));
                break;
        }
        
        // Common components for all monsters
        monster.addComponent(new HealthBar(40, 4, 40));
        monster.addComponent(new Path());
        
        return monster;
    }
    
    /**
     * Create NPC entity (reserved for later)
     */
    public static Entity createNPC(String npcName, float x, float y) {
        Entity npc = new Entity(npcName, EntityType.NPC);
        
        // TODO: Implement NPC creation
        
        return npc;
    }
}