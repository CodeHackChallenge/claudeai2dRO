package dev.main;

public class EntityFactory { 
    /**
     * Create a player entity with level system
     */
	public static Entity createPlayer(float x, float y) {
	    Entity player = new Entity("Player", EntityType.PLAYER);
	    
	    player.addComponent(new Position(x, y));
	    //player.addComponent(new Sprite("resources/sprites/hero2.png", 64, 64, 0.2f)); //VS Code
        player.addComponent(new Sprite("/sprites/hero2.png", 64, 64, 0.2f)); //eclipse

	    player.addComponent(new Movement(100f, 200f));
	    
	    // ☆ LEVEL 1 BASE STATS:
	    // baseMaxHp=100, baseAttack=10, baseDefense=2, baseAccuracy=0, baseMaxMana=50
	    // Stamina is now calculated automatically (500 base)
	    Stats stats = new Stats(100, 10, 2, 0, 50);  // ☆ Removed stamina parameter
	    player.addComponent(stats);
	    
	    // ADD EXPERIENCE COMPONENT
	    Experience exp = new Experience();
	    exp.hpGrowth = 10;      // +10 HP per level
	    exp.attackGrowth = 2;   // +2 Attack per level
	    exp.defenseGrowth = 1;  // +1 Defense per level
	    exp.accGrowth = 1;      // +1 Accuracy per level
	    exp.manaGrowth = 5;     // +5 mana per level
	    player.addComponent(exp);
	    
	    // ADD SKILL LEVEL COMPONENT
	    SkillLevel skillLevel = new SkillLevel();
	    player.addComponent(skillLevel);
	    
	    // APPLY LEVEL 1 STATS
	    stats.applyLevelStats(exp);
	    
	    // Combat system
	    player.addComponent(new Combat(1.1f, 0.15f, 0.05f));
	    
	    // UI Components
	    player.addComponent(new HealthBar(40, 4, 40));
	    player.addComponent(new StaminaBar(40, 4, 46));
	    player.addComponent(new ManaBar(40, 4, 50));
	    player.addComponent(new XPBar(40, 3, 54));
	    
	    // Collision and movement
	    player.addComponent(new CollisionBox(-10, -14, 22, 44));
	    player.addComponent(new Path());
	    player.addComponent(new TargetIndicator()); 
	    
	    // Rendering
	    player.addComponent(new Renderable(RenderLayer.ENTITIES));
	    
	    // Level-up visual effect
	    player.addComponent(new LevelUpEffect());
	    
	    return player;
	}
    
    /**
     * NEW: Create monster with level and tier scaling
     */
    public static Entity createMonster(String monsterType, float x, float y, int level, MobTier tier) {
        Entity monster = new Entity(monsterType, EntityType.MONSTER);
        
        monster.addComponent(new Position(x, y));
        
        // Calculate stats based on level and tier
        MobStats mobStats = MobStatFactory.create(level, tier);
        
        // Add MonsterLevel component for XP calculation
        monster.addComponent(new MonsterLevel(level, tier));
        
        // Create Stats component with calculated values
        Stats stats = new Stats(mobStats.hp, 50f, mobStats.attack, mobStats.defense);
        stats.accuracy = mobStats.accuracy;
        stats.evasion = mobStats.evasion;
        monster.addComponent(stats);
        
        // Configure monster type-specific properties
        switch(monsterType) {
            case "Goblin":
                //monster.addComponent(new Sprite("resources/sprites/goblin.png", 64, 64, 0.15f)); //VS Code 
                 monster.addComponent(new Sprite("/sprites/goblin.png", 64, 64, 0.15f)); // eclipse
                monster.addComponent(new Combat(1.2f, 0.10f, 0.08f));
                monster.addComponent(new Movement(80f, 160f));
                monster.addComponent(new CollisionBox(-10, -14, 22, 44)); 
                monster.addComponent(new AI("passive", x, y, 500f, 4f));
                monster.addComponent(new NameTag("Goblin", -20));
                monster.addComponent(new Alert(-40));
                monster.addComponent(new Renderable(RenderLayer.ENTITIES));
                break;
                
            case "GoblinBoss":
               // monster.addComponent(new Sprite("resources/sprites/goblin_dark.png", 64, 64, 0.12f)); //VS code
                monster.addComponent(new Sprite("/sprites/goblin_dark.png", 64, 64, 0.12f)); // eclipse
                monster.addComponent(new Combat(1.0f, 0.20f, 0.05f));
                monster.addComponent(new Movement(100f, 200f));
                monster.addComponent(new CollisionBox(-10, -14, 22, 44));
                monster.addComponent(new AI("aggressive", x, y, 250f, 6f));
                monster.addComponent(new NameTag("Goblin Boss", -20));
                monster.addComponent(new Alert(-40));
                monster.addComponent(new Renderable(RenderLayer.ENTITIES, 10));
                break;
                
            case "BunnyBoss":
                //monster.addComponent(new Sprite("resources/sprites/bunny_boss.png", 64, 64, 0.12f)); //vs code
                monster.addComponent(new Sprite("/sprites/bunny_boss.png", 64, 64, 0.12f)); // eclipse
                monster.addComponent(new Combat(1.0f, 0.20f, 0.05f));
                monster.addComponent(new Movement(100f, 200f));
                monster.addComponent(new CollisionBox(-10, -14, 22, 44));
                monster.addComponent(new AI("aggressive", x, y, 500f, 6f));
                monster.addComponent(new NameTag("Bunny Boss", -20));
                monster.addComponent(new Alert(-40));
                monster.addComponent(new Renderable(RenderLayer.ENTITIES, 10));
                break;
                
            case "MinotaurBoss":
                //monster.addComponent(new Sprite("resources/sprites/minotaur_boss.png", 64, 64, 0.12f)); //vs code
                monster.addComponent(new Sprite("/sprites/minotaur_boss.png", 64, 64, 0.12f)); // eclipse
                monster.addComponent(new Combat(1.0f, 0.20f, 0.05f));
                monster.addComponent(new Movement(100f, 200f));
                monster.addComponent(new CollisionBox(-10, -14, 22, 44));
                monster.addComponent(new AI("aggressive", x, y, 250f, 6f));
                monster.addComponent(new NameTag("Minotaur Boss", -20));
                monster.addComponent(new Alert(-40));
                monster.addComponent(new Renderable(RenderLayer.ENTITIES, 10));
                break;
                
            case "Bunny":
                //monster.addComponent(new Sprite("resources/sprites/bunny.png", 64, 64, 0.15f)); //vs code
                monster.addComponent(new Sprite("/sprites/bunny.png", 64, 64, 0.15f)); // eclipse
                monster.addComponent(new Combat(1.2f, 0.10f, 0.08f));
                monster.addComponent(new Movement(80f, 160f));
                monster.addComponent(new CollisionBox(-10, -14, 22, 44));
                monster.addComponent(new AI("passive", x, y, 500f, 4f));
                monster.addComponent(new NameTag("Bunny", -20));
                monster.addComponent(new Alert(-40));
                monster.addComponent(new Renderable(RenderLayer.ENTITIES));
                break;
                
                //me
            case "Ghost":
                //monster.addComponent(new Sprite("resources/sprites/ghost.png", 64, 64, 0.15f)); //vs code
                monster.addComponent(new Sprite("/sprites/ghost.png", 64, 64, 0.15f)); // eclipse
                monster.addComponent(new Combat(1.2f, 0.10f, 0.08f));
                monster.addComponent(new Movement(80f, 160f));
                monster.addComponent(new CollisionBox(-17, -14, 36, 42));
                monster.addComponent(new AI("passive", x, y, 500f, 4f));
                monster.addComponent(new NameTag("Ghost", -20));
                monster.addComponent(new Alert(-40));
                monster.addComponent(new Renderable(RenderLayer.ENTITIES));
                break;
                //me
            case "Orc":
                // monster.addComponent(new Sprite("resources/sprites/ro_orc.png", 95, 129, 0.15f)); //vs code
                 monster.addComponent(new Sprite("/sprites/ro_orc.png", 95, 129, 0.15f)); // eclipse
                monster.addComponent(new Combat(1.2f, 0.10f, 0.08f));
                monster.addComponent(new Movement(80f, 160f));
                monster.addComponent(new CollisionBox(-17, -14, 36, 42));
                monster.addComponent(new AI("passive", x, y, 500f, 4f));
                monster.addComponent(new NameTag("Orc", -20));
                monster.addComponent(new Alert(-40));
                monster.addComponent(new Renderable(RenderLayer.ENTITIES));
                break;
            default:
                // Default monster setup
                //monster.addComponent(new Sprite("resources/sprites/goblin.png", 64, 64, 0.15f)); //VS Code
                 monster.addComponent(new Sprite("/sprites/goblin.png", 64, 64, 0.15f)); // eclipse 
                monster.addComponent(new Combat(1.5f, 0.05f, 0.08f));
                monster.addComponent(new Movement(50f, 100f));
                monster.addComponent(new CollisionBox(-12, -12, 24, 24));
                monster.addComponent(new AI("neutral", x, y, 128f, 2f));
                monster.addComponent(new NameTag("Monster", -45));
                monster.addComponent(new Renderable(RenderLayer.ENTITIES));
                break;
        }
        
        monster.addComponent(new HealthBar(40, 4, 40));
        monster.addComponent(new Path());
        
        return monster;
    }
    
    /**
     * OLD: Backwards compatibility - creates level 1 NORMAL tier monster
     */
    public static Entity createMonster(String monsterType, float x, float y) {
        return createMonster(monsterType, x, y, 1, MobTier.NORMAL);
    }
    
    /**
     * Create NPC entity (reserved for later)
     */
    public static Entity createNPC(String npcName, float x, float y) {
        Entity npc = new Entity(npcName, EntityType.NPC);
        // TODO: Implement NPC creation
        return npc;
    }
     
    /**
     * Create a simple tree environment entity
     */
    public static Entity createTree(float x, float y) {
        Entity tree = new Entity("Tree", EntityType.ENVIRONMENT);

        tree.addComponent(new Position(x, y));
       // tree.addComponent(new Sprite("resources/sprites/tree.png", 481, 513, 0f)); //VS Code
        tree.addComponent(new Sprite("/sprites/willow.png", 481, 513, 0f)); //eclipse
        tree.addComponent(new Renderable(RenderLayer.ENTITIES, 80));
        NameTag nt = new NameTag("Tree", -40);
        nt.show();
        tree.addComponent(nt);

        return tree;
    }
    
}