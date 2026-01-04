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
        player.addComponent(new Stats(1000, 500f, 15, 5));
        player.addComponent(new Combat(1.1f, 0.15f, 0.05f));  // 0.8s cooldown, 15% crit, 5% evasion 
        player.addComponent(new HealthBar(40, 4, 40));
        player.addComponent(new StaminaBar(40, 4, 46));
        player.addComponent(new CollisionBox(-10, -14, 22, 44));
        player.addComponent(new Path());
        player.addComponent(new TargetIndicator()); 
        player.addComponent(new Renderable(RenderLayer.ENTITIES));  // NEW
        
        return player;
    }
    ///sprites/goblin_dark.png
    /**
     * Create a monster entity
     */
    public static Entity createMonster(String monsterType, float x, float y) {
        Entity monster = new Entity(monsterType, EntityType.MONSTER);
        
        monster.addComponent(new Position(x, y));
        
        switch(monsterType) {
          /*  case "Slime":
                monster.addComponent(new Sprite("/sprites/slime.png", 64, 64, 0.2f));
                monster.addComponent(new Stats(50, 50f, 5, 2));
                monster.addComponent(new Combat(1.5f, 0.05f, 0.10f));
                monster.addComponent(new Movement(60f, 120f));
                monster.addComponent(new CollisionBox(-12, -12, 24, 24));
                monster.addComponent(new AI("aggressive", x, y, 128f, 3f));
                monster.addComponent(new NameTag("Slime", -45));
                break;
             */   
            case "Goblin":
                monster.addComponent(new Sprite("/sprites/goblin.png", 64, 64, 0.15f));
                monster.addComponent(new Stats(80, 80f, 10, 4));
                monster.addComponent(new Combat(1.2f, 0.10f, 0.08f));
                monster.addComponent(new Movement(80f, 160f));
                monster.addComponent(new CollisionBox(-10, -14, 20, 28));
                monster.addComponent(new AI("aggressive", x, y, 500f, 4f));
                monster.addComponent(new NameTag("Goblin", -20));
                monster.addComponent(new Alert(-40));  // NEW
                
                break;
            /*    
            case "Poring":
                monster.addComponent(new Sprite("/sprites/poring.png", 64, 64, 0.25f));
                monster.addComponent(new Stats(30, 30f, 3, 1));
                monster.addComponent(new Combat(2.0f, 0.02f, 0.15f));
                monster.addComponent(new Movement(40f, 80f));
                monster.addComponent(new CollisionBox(-12, -12, 24, 24));
                monster.addComponent(new AI("passive", x, y, 96f, 0f));
                monster.addComponent(new NameTag("Poring", -45));
                break;
             */   
            case "GoblinBoss":  // NEW: Boss monster
                monster.addComponent(new Sprite("/sprites/goblin_dark.png", 64, 64, 0.12f));
                monster.addComponent(new Stats(100, 500f, 25, 10));  // High HP and damage
                monster.addComponent(new Combat(1.0f, 0.20f, 0.05f));  // Fast attacks, high crit
                monster.addComponent(new Movement(100f, 200f));  // Fast movement
                monster.addComponent(new CollisionBox(-14, -16, 28, 32));  // Larger hitbox
                monster.addComponent(new AI("aggressive", x, y, 500f, 6f));  // Large aggro range
                monster.addComponent(new NameTag("Goblin Boss", -20));
                monster.addComponent(new Alert(-40));  // NEW
                break;
            //me
            case "BunnyBoss":  // NEW: Boss monster
                monster.addComponent(new Sprite("/sprites/bunny_boss.png", 64, 64, 0.12f));
                monster.addComponent(new Stats(100, 500f, 25, 10));  // High HP and damage
                monster.addComponent(new Combat(1.0f, 0.20f, 0.05f));  // Fast attacks, high crit
                monster.addComponent(new Movement(100f, 200f));  // Fast movement
                monster.addComponent(new CollisionBox(-14, -16, 28, 32));  // Larger hitbox
                monster.addComponent(new AI("aggressive", x, y, 500f, 6f));  // Large aggro range
                monster.addComponent(new NameTag("Bunny Boss", -20));
                monster.addComponent(new Alert(-40));  // NEW
                break;
                //me
            case "Bunny":
                monster.addComponent(new Sprite("/sprites/bunny.png", 64, 64, 0.15f));
                monster.addComponent(new Stats(80, 80f, 10, 4));
                monster.addComponent(new Combat(1.2f, 0.10f, 0.08f));
                monster.addComponent(new Movement(80f, 160f));
                monster.addComponent(new CollisionBox(-10, -14, 20, 28));
                monster.addComponent(new AI("aggressive", x, y, 500f, 4f));
                monster.addComponent(new NameTag("Bunny", -20));
                monster.addComponent(new Alert(-40));  // NEW
            /*    
            default:
                monster.addComponent(new Sprite("/sprites/monster_default.png", 64, 64, 0.2f));
                monster.addComponent(new Stats(40, 40f, 5, 2));
                monster.addComponent(new Combat(1.5f, 0.05f, 0.08f));
                monster.addComponent(new Movement(50f, 100f));
                monster.addComponent(new CollisionBox(-12, -12, 24, 24));
                monster.addComponent(new AI("neutral", x, y, 128f, 2f));
                monster.addComponent(new NameTag("Monster", -45));
                break;
                */
        }
        
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