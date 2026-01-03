package dev.main;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GameLogic {
    
    private GameState state;
    private float cameraLerpSpeed = 5f;
    
    public GameLogic(GameState state) {
        this.state = state;
    } 
    
    public void update(float delta) {
        state.incrementGameTime(delta);
        
        Entity player = state.getPlayer();
        Position playerPos = player.getComponent(Position.class);
        
        for (Entity entity : state.getEntities()) {
            EntityType entityType = entity.getType();
            
            Combat combat = entity.getComponent(Combat.class);
            if (combat != null) {
                combat.update(delta);
            }
            
            if (entityType == EntityType.PLAYER) {
                updatePlayer(entity, delta);
            } else if (entityType == EntityType.MONSTER) {
                updateMonster(entity, playerPos, delta);
            }
            
            Sprite sprite = entity.getComponent(Sprite.class);
            if (sprite != null) {
                sprite.update(delta);
            }
            
            TargetIndicator indicator = entity.getComponent(TargetIndicator.class);
            if (indicator != null) {
                indicator.update(delta);
            }
        }
        
        state.updateDamageTexts(delta);
        state.removeMarkedEntities();
        updateCamera(delta);
    }
    /**
     * Player attacks target entity (called from click or auto-attack)
     */
    public void playerAttack(Entity target) {
        Entity player = state.getPlayer();
        Combat playerCombat = player.getComponent(Combat.class);
        Position playerPos = player.getComponent(Position.class);
        Position targetPos = target.getComponent(Position.class);
        Movement playerMovement = player.getComponent(Movement.class);
        
        if (playerCombat == null) return;
        if (playerPos == null || targetPos == null) return;
        
        // Set as auto-attack target
        state.setAutoAttackTarget(target);
        
        // Check distance
        float distance = distance(playerPos.x, playerPos.y, targetPos.x, targetPos.y);
        
        if (distance <= 80f && playerCombat.canAttack()) {
            // In range and can attack - do it now
            playerMovement.direction = calculateDirection(targetPos.x - playerPos.x, targetPos.y - playerPos.y);
            playerMovement.lastDirection = playerMovement.direction;
            
            playerCombat.startAttack();
            performAttack(player, target, playerPos, targetPos);
        } else {
            // Out of range - will path to target in update loop
            System.out.println("Moving to attack range...");
        }
    }
    /**
     * Stop auto-attacking (called when moving to new location)
     */
    public void stopAutoAttack() {
        state.clearAutoAttackTarget();
    }
    /**
     * Perform attack calculation with crit/evasion
     */
    private void performAttack(Entity attacker, Entity target, Position attackerPos, Position targetPos) {
        Stats attackerStats = attacker.getComponent(Stats.class);
        Stats targetStats = target.getComponent(Stats.class);
        Combat attackerCombat = attacker.getComponent(Combat.class);
        Combat targetCombat = target.getComponent(Combat.class);
        
        if (attackerStats == null || targetStats == null) return;
        
        // Check evasion
        float evasionRoll = ThreadLocalRandom.current().nextFloat();
        float evasionChance = targetCombat != null ? targetCombat.evasionChance : 0f;
        
        if (evasionRoll < evasionChance) {
            System.out.println(attacker.getName() + " attacks " + target.getName() + " - MISS!");
            DamageText missText = new DamageText("MISS", DamageText.Type.MISS, targetPos.x, targetPos.y - 30);
            state.addDamageText(missText);
            return;
        }
        
        // Check critical hit
        boolean isCrit = false;
        float critRoll = ThreadLocalRandom.current().nextFloat();
        float critChance = attackerCombat != null ? attackerCombat.critChance : 0f;
        
        if (critRoll < critChance) {
            isCrit = true;
        }
        
        // Calculate damage
        int baseDamage = attackerStats.attack - targetStats.defense;
        baseDamage = Math.max(1, baseDamage);
        
        if (isCrit && attackerCombat != null) {
            baseDamage = (int)(baseDamage * attackerCombat.critMultiplier);
        }
        
        // Apply damage
        targetStats.hp -= baseDamage;
        if (targetStats.hp < 0) targetStats.hp = 0;
        
        // Log
        String hitType = isCrit ? " - CRITICAL HIT!" : "";
        System.out.println(attacker.getName() + " attacks " + target.getName() + " for " + baseDamage + " damage" + hitType);
        
        // Spawn damage text
        DamageText.Type textType = isCrit ? DamageText.Type.CRITICAL : DamageText.Type.NORMAL;
        DamageText damageText = new DamageText(String.valueOf(baseDamage), textType, targetPos.x, targetPos.y - 30);
        state.addDamageText(damageText);
        
        // Check death - only call if not already dead
        if (targetStats.hp <= 0) {
            Dead alreadyDead = target.getComponent(Dead.class);
            if (alreadyDead == null) {  // ⭐ Only handle death once
                if (target.getType() == EntityType.MONSTER) {
                    handleMonsterDeath(target, target.getComponent(Sprite.class));
                } else if (target.getType() == EntityType.PLAYER) {
                    handlePlayerDeath(target, target.getComponent(Sprite.class));
                }
            }
        }
        
        // If monster was hit by player, aggro it
        if (attacker.getType() == EntityType.PLAYER && target.getType() == EntityType.MONSTER) {
            AI targetAI = target.getComponent(AI.class);
            Stats monsterStats = target.getComponent(Stats.class);
            
            // Only aggro if monster is alive
            if (targetAI != null && monsterStats != null && monsterStats.hp > 0 && 
                targetAI.currentState != AI.State.DEAD) {
                if (targetAI.currentState == AI.State.IDLE || 
                    targetAI.currentState == AI.State.ROAMING || 
                    targetAI.currentState == AI.State.RETURNING) {
                    transitionAIState(target, targetAI, AI.State.CHASING);
                    targetAI.target = attacker;
                }
            }
        }
    }
    /**
     * Handle player death
     */
    private void handlePlayerDeath(Entity player, Sprite sprite) {
        System.out.println("Player has died!");
        
        // Stop all actions
        Movement movement = player.getComponent(Movement.class);
        if (movement != null) {
            movement.stopMoving();
        }
        
        Path path = player.getComponent(Path.class);
        if (path != null) {
            path.clear();
        }
        
        // Clear auto-attack
        state.clearAutoAttackTarget();
        
        // Play death animation
        if (sprite != null) {
            sprite.setAnimation(Sprite.ANIM_DEAD);
        }
        
        // Add dead component
        player.addComponent(new Dead(10f));  // Player corpse lasts 10 seconds
        
        // TODO: Show respawn UI, game over screen, etc.
    }

	private void updatePlayer(Entity player, float delta) {
		Movement movement = player.getComponent(Movement.class);
	    Position position = player.getComponent(Position.class);
	    Sprite sprite = player.getComponent(Sprite.class);
	    Stats stats = player.getComponent(Stats.class);
	    Path path = player.getComponent(Path.class);
	    Combat combat = player.getComponent(Combat.class);
	    TargetIndicator indicator = player.getComponent(TargetIndicator.class);
	    Dead dead = player.getComponent(Dead.class);  // NEW: Check if already dead
	    
	    if (movement == null || position == null || sprite == null || stats == null) return;
	    
	    // If already has Dead component, just play death animation and return
	    if (dead != null) {
	        if (sprite != null) {
	            sprite.setAnimation(Sprite.ANIM_DEAD);
	        }
	        dead.update(delta);
	        // TODO: Show respawn button after corpse timer
	        return;
	    }
	    
	    // Check if player just died (hp <= 0 but no Dead component yet)
	    if (stats.hp <= 0) {
	        handlePlayerDeath(player, sprite);
	        return;
	    }
        // Auto-attack logic
        Entity autoAttackTarget = state.getAutoAttackTarget();
        if (autoAttackTarget != null) {
            Stats targetStats = autoAttackTarget.getComponent(Stats.class);
            if (targetStats == null || targetStats.hp <= 0) {
                state.clearAutoAttackTarget();
                autoAttackTarget = null;
            } else {
                Position targetPos = autoAttackTarget.getComponent(Position.class);
                if (targetPos != null) {
                    float distance = distance(position.x, position.y, targetPos.x, targetPos.y);
                    
                    if (distance <= 80f) {
                        // In range - attack
                        if (combat != null && combat.canAttack() && !combat.isAttacking) {
                            // Make both face each other
                            makeFaceEachOther(player, autoAttackTarget);
                            
                            // Stop moving
                            movement.stopMoving();
                            if (path != null) path.clear();
                            if (indicator != null) indicator.clear();
                            
                            // Attack
                            combat.startAttack();
                            performAttack(player, autoAttackTarget, position, targetPos);
                        }
                    } else {
                        // Out of range - path to target
                        if (!movement.isMoving || (path != null && !path.isFollowing)) {
                            int startTileX = (int)(position.x / TileMap.TILE_SIZE);
                            int startTileY = (int)(position.y / TileMap.TILE_SIZE);
                            int goalTileX = (int)(targetPos.x / TileMap.TILE_SIZE);
                            int goalTileY = (int)(targetPos.y / TileMap.TILE_SIZE);
                            
                            List<int[]> foundPath = state.getPathfinder().findPath(startTileX, startTileY, goalTileX, goalTileY);
                            
                            if (foundPath != null && path != null) {
                                path.setPath(foundPath);
                                movement.isRunning = false;
                            }
                        }
                    }
                }
            }
        }
        
        // Handle attack animation
        if (combat != null && combat.isAttacking) {
            sprite.setAnimation(getAttackAnimationForDirection(movement.lastDirection));
            
            // Keep facing target while attacking
            if (autoAttackTarget != null) {
                Position targetPos = autoAttackTarget.getComponent(Position.class);
                if (targetPos != null) {
                    float dx = targetPos.x - position.x;
                    float dy = targetPos.y - position.y;
                    int newDirection = calculateDirection(dx, dy);
                    if (newDirection != movement.lastDirection) {
                        movement.direction = newDirection;
                        movement.lastDirection = newDirection;
                    }
                }
            }
            return;
        }
        
        // Follow path
        if (path != null && path.isFollowing) {
            followPath(player, path, movement, position, delta);
        }
        
        if (movement.isMoving) {
            if (movement.isRunning) {
                boolean hasStamina = stats.consumeStamina(movement.staminaCostPerSecond * delta);
                if (!hasStamina) {
                    movement.stopRunning();
                }
            }
            
            moveTowardsTarget(player, movement, position, delta);
            
            String moveAnim = movement.isRunning 
                ? getRunAnimationForDirection(movement.direction)
                : getWalkAnimationForDirection(movement.direction);
            sprite.setAnimation(moveAnim);
            
        } else {
            if (indicator != null && autoAttackTarget == null) {
                indicator.clear();
            }
            
            sprite.setAnimation(getIdleAnimationForDirection(movement.lastDirection));
            stats.regenerateStamina(delta);
        }
        
        if (movement.isMoving && !movement.isRunning) {
            stats.regenerateStamina(delta);
        }
    }
	
	private void updateMonster(Entity monster, Position playerPos, float delta) {
	    AI ai = monster.getComponent(AI.class);
	    Position position = monster.getComponent(Position.class);
	    Movement movement = monster.getComponent(Movement.class);
	    Sprite sprite = monster.getComponent(Sprite.class);
	    Stats stats = monster.getComponent(Stats.class);
	    Path path = monster.getComponent(Path.class);
	    Dead dead = monster.getComponent(Dead.class);
	    Combat combat = monster.getComponent(Combat.class);
	    
	    if (position == null || ai == null) return;
	    
	    // Update dead state
	    if (dead != null) {
	        dead.update(delta);
	        if (dead.shouldRemove()) {
	            state.markForRemoval(monster);
	        }
	        return;
	    }
	    
	    // Check if dead
	    if (stats != null && stats.hp <= 0) {
	        handleMonsterDeath(monster, sprite);
	        return;
	    }
	    
	    // ⭐ IMPORTANT: Update AI (this increments timers)
	    ai.update(delta);
	    
	    // Update combat animations
	    if (combat != null && combat.isAttacking) {
	        if (sprite != null && movement != null) {
	            sprite.setAnimation(getAttackAnimationForDirection(movement.lastDirection));
	        }
	    }
	    
	    // AI State Machine
	    switch (ai.currentState) {
	        case IDLE:
	            handleIdleState(monster, ai, movement, sprite, playerPos, delta);
	            break;
	            
	        case ROAMING:
	            handleRoamingState(monster, ai, movement, position, path, sprite, playerPos, delta);
	            break;
	            
	        case CHASING:
	            handleChasingState(monster, ai, movement, position, path, sprite, playerPos, delta);
	            break;
	            
	        case RETURNING:
	            handleReturningState(monster, ai, movement, position, path, sprite, delta);
	            break;
	            
	        case ATTACKING:
	            handleAttackingState(monster, ai, movement, sprite, playerPos, stats, delta);
	            break;
	            
	        case VICTORY_IDLE:
	            handleVictoryIdleState(monster, ai, movement, sprite, delta);
	            break;
	    }
	    
	    // Move if has target
	    if (movement != null && movement.isMoving) {
	        moveTowardsTarget(monster, movement, position, delta);
	    }
	    
	    // Follow path
	    if (path != null && path.isFollowing) {
	        followPath(monster, path, movement, position, delta);
	    }
	}
	/**
	 * Victory idle state - monster idles after killing player
	 */
	private void handleVictoryIdleState(Entity monster, AI ai, Movement movement, Sprite sprite, float delta) {
	    // Play idle animation
	    if (sprite != null && movement != null) {
	        sprite.setAnimation(getIdleAnimationForDirection(movement.lastDirection));
	    }
	    
	    // Debug output
	    System.out.println(monster.getName() + " victory idle: " + ai.victoryIdleTimer + "/" + ai.victoryIdleDuration);
	    
	    // After timer expires, return to patrolling
	    if (ai.victoryIdleTimer >= ai.victoryIdleDuration) {
	        System.out.println(monster.getName() + " victory timer complete! Transitioning...");
	        
	        Position monsterPos = monster.getComponent(Position.class);
	        if (monsterPos != null) {
	            float distFromHome = distance(monsterPos.x, monsterPos.y, ai.homeX, ai.homeY);
	            
	            System.out.println("  Distance from home: " + distFromHome + " (roam radius: " + ai.roamRadius + ")");
	            
	            if (distFromHome <= ai.roamRadius * 1.2f) {
	                System.out.println("  -> Transitioning to IDLE");
	                transitionAIState(monster, ai, AI.State.IDLE);
	            } else {
	                System.out.println("  -> Transitioning to RETURNING");
	                transitionAIState(monster, ai, AI.State.RETURNING);
	            }
	        } else {
	            System.out.println("  -> No position, transitioning to IDLE");
	            transitionAIState(monster, ai, AI.State.IDLE);
	        }
	    }
	}
	/**
	 * Helper to cleanly transition AI state and clear movement
	 */
	private void transitionAIState(Entity entity, AI ai, AI.State newState) {
	    if (ai.currentState == newState) return;
	    
	    System.out.println(entity.getName() + " transitioning: " + ai.currentState + " -> " + newState);
	    
	    AI.State oldState = ai.currentState;
	    ai.currentState = newState;
	    
	    // Clear movement/path when transitioning
	    Movement movement = entity.getComponent(Movement.class);
	    Path path = entity.getComponent(Path.class);
	    
	    if (movement != null) {
	        movement.stopMoving();
	        
	        // ⭐ Clear haste when leaving RETURNING state
	        if (oldState == AI.State.RETURNING && newState != AI.State.RETURNING) {
	            movement.setHaste(false);
	            System.out.println("  Haste removed");
	        }
	    }
	    if (path != null) {
	        path.clear();
	    }
	    
	    // Reset timers based on new state
	    switch(newState) {
	        case IDLE:
	            ai.roamTimer = 0;
	            ai.roamInterval = ThreadLocalRandom.current().nextFloat(3f, 6f);
	            break;
	        case VICTORY_IDLE:
	            ai.victoryIdleTimer = 0;
	            ai.target = null;
	            System.out.println("  Victory idle timer reset to 0");
	            break;
	        case RETURNING:
	            ai.target = null;
	            // Haste will be applied in handleReturningState
	            break;
	    }
	}
    // Add attack animation helper (8 directions)
    private String getAttackAnimationForDirection(int direction) {
        // TODO: Map to actual attack animation rows in sprite sheet
        // For now, return walk animation as placeholder
        switch(direction) {
            case Movement.DIR_EAST:
                return "attack_right";  // or Sprite.ANIM_ATTACK_RIGHT
            case Movement.DIR_SOUTH_EAST:
                return "attack_down_right";
            case Movement.DIR_SOUTH:
                return "attack_down";
            case Movement.DIR_SOUTH_WEST:
                return "attack_down_left";
            case Movement.DIR_WEST:
                return "attack_left";
            case Movement.DIR_NORTH_WEST:
                return "attack_up_left";
            case Movement.DIR_NORTH:
                return "attack_up";
            case Movement.DIR_NORTH_EAST:
                return "attack_up_right";
            default:
                return "attack_down";
        }
    }
    
    private void handleIdleState(Entity monster, AI ai, Movement movement, Sprite sprite, Position playerPos, float delta) {
        Position monsterPos = monster.getComponent(Position.class);
        
        // Detect player (only if alive and we're within our territory)
        if (ai.behaviorType.equals("aggressive") && playerPos != null && monsterPos != null) {
            Entity player = state.getPlayer();
            Stats playerStats = player.getComponent(Stats.class);
            
            // Check if we're within safe territory before engaging
            float distFromHome = distance(monsterPos.x, monsterPos.y, ai.homeX, ai.homeY);
            
            // Only detect if player is alive AND we're not too far from home
            if (playerStats != null && playerStats.hp > 0 && distFromHome <= ai.roamRadius * 1.5f) {
                if (canDetectPlayer(monsterPos, playerPos, ai.detectionRange)) {
                    transitionAIState(monster, ai, AI.State.CHASING);
                    ai.target = state.getPlayer();
                    return;
                }
            }
        }
        
        // Transition to roaming after timer
        if (ai.roamTimer >= ai.roamInterval) {
            transitionAIState(monster, ai, AI.State.ROAMING);
        }
        
        // Play idle animation
        if (sprite != null && movement != null) {
            sprite.setAnimation(getIdleAnimationForDirection(movement.lastDirection));
        }
    }

    private void handleRoamingState(Entity monster, AI ai, Movement movement, Position position, Path path, Sprite sprite, Position playerPos, float delta) {
        // Detect player (only if alive and within territory)
        if (ai.behaviorType.equals("aggressive") && playerPos != null && position != null) {
            Entity player = state.getPlayer();
            Stats playerStats = player.getComponent(Stats.class);
            
            // Check territory
            float distFromHome = distance(position.x, position.y, ai.homeX, ai.homeY);
            
            // Only detect if player is alive AND we're not too far from home
            if (playerStats != null && playerStats.hp > 0 && distFromHome <= ai.roamRadius * 1.5f) {
                if (canDetectPlayer(position, playerPos, ai.detectionRange)) {
                    transitionAIState(monster, ai, AI.State.CHASING);
                    ai.target = state.getPlayer();
                    return;
                }
            }
        }
        
        // Pick random point within roam radius
        if (movement != null && !movement.isMoving) {
            float angle = (float)(ThreadLocalRandom.current().nextDouble() * Math.PI * 2);
            float distance = ThreadLocalRandom.current().nextFloat(0.5f, 1f) * ai.roamRadius;
            
            float targetX = ai.homeX + (float)Math.cos(angle) * distance;
            float targetY = ai.homeY + (float)Math.sin(angle) * distance;
            
            int startTileX = (int)(position.x / TileMap.TILE_SIZE);
            int startTileY = (int)(position.y / TileMap.TILE_SIZE);
            int goalTileX = (int)(targetX / TileMap.TILE_SIZE);
            int goalTileY = (int)(targetY / TileMap.TILE_SIZE);
            
            List<int[]> foundPath = state.getPathfinder().findPath(startTileX, startTileY, goalTileX, goalTileY);
            
            if (foundPath != null && path != null) {
                path.setPath(foundPath);
                movement.isRunning = false;
            } else {
                transitionAIState(monster, ai, AI.State.IDLE);
            }
        }
        
        // Reached destination
        if (movement != null && !movement.isMoving && (path == null || !path.isFollowing)) {
            transitionAIState(monster, ai, AI.State.IDLE);
        }
        
        // Animation
        if (sprite != null && movement != null) {
            if (movement.isMoving) {
                sprite.setAnimation(getWalkAnimationForDirection(movement.direction));
            } else {
                sprite.setAnimation(getIdleAnimationForDirection(movement.lastDirection));
            }
        }
    }
    /**
     * 
	## The Key Logic
	
	**Before (buggy):**
	```
	Mob outside range + player nearby:
	CHASING → (too far from home) → RETURNING
	RETURNING → (player detected) → CHASING
	CHASING → (too far from home) → RETURNING
	(infinite loop)
	```
	
	**After (fixed):**
	```
	Mob outside range + player nearby:
	CHASING → (too far from home) → RETURNING
	RETURNING → (ignore player completely) → reaches home → IDLE
(clean behavior)
     *      */
    private void handleChasingState(Entity monster, AI ai, Movement movement, Position position, Path path, Sprite sprite, Position playerPos, float delta) {
        if (playerPos == null || movement == null || position == null) {
            transitionAIState(monster, ai, AI.State.RETURNING);
            return;
        }
        
        // Check if player is dead
        Entity player = state.getPlayer();
        if (player != null) {
            Stats playerStats = player.getComponent(Stats.class);
            if (playerStats != null && playerStats.hp <= 0) {
                transitionAIState(monster, ai, AI.State.VICTORY_IDLE);
                return;
            }
        }
        
        // ⭐ PRIORITY 1: Check if too far from home (strict leash)
        float distFromHome = distance(position.x, position.y, ai.homeX, ai.homeY);
        if (distFromHome > ai.returnThreshold) {
            transitionAIState(monster, ai, AI.State.RETURNING);
            System.out.println(monster.getName() + " leash break - too far from home!");
            return;
        }
        
        // ⭐ PRIORITY 2: Check if player moved too far away (lose aggro)
        float distToPlayer = distance(position.x, position.y, playerPos.x, playerPos.y);
        if (distToPlayer > ai.detectionRange * TileMap.TILE_SIZE * 1.5f) {
            transitionAIState(monster, ai, AI.State.RETURNING);
            System.out.println(monster.getName() + " player too far - returning home");
            return;
        }
        
        // ⭐ PRIORITY 3: Check if in attack range
        if (distToPlayer <= ai.attackRange) {
            transitionAIState(monster, ai, AI.State.ATTACKING);
            return;
        }
        
        // PRIORITY 4: Chase player - update path periodically
        if (!movement.isMoving || (path != null && !path.isFollowing)) {
            int startTileX = (int)(position.x / TileMap.TILE_SIZE);
            int startTileY = (int)(position.y / TileMap.TILE_SIZE);
            int goalTileX = (int)(playerPos.x / TileMap.TILE_SIZE);
            int goalTileY = (int)(playerPos.y / TileMap.TILE_SIZE);
            
            List<int[]> foundPath = state.getPathfinder().findPath(startTileX, startTileY, goalTileX, goalTileY);
            
            if (foundPath != null && path != null) {
                path.setPath(foundPath);
                movement.isRunning = true;
            } else {
                // Can't find path to player, return home
                transitionAIState(monster, ai, AI.State.RETURNING);
                System.out.println(monster.getName() + " can't path to player - returning");
            }
        }
        
        // Animation
        if (sprite != null && movement.isMoving) {
            sprite.setAnimation(getRunAnimationForDirection(movement.direction));
        } else if (sprite != null) {
            // Not moving but still in chase state (stuck?)
            sprite.setAnimation(getIdleAnimationForDirection(movement.lastDirection));
        }
    }

    private void handleReturningState(Entity monster, AI ai, Movement movement, Position position, Path path, Sprite sprite, float delta) {
        if (movement == null || position == null) return;
        
        // ⭐ Apply haste effect when returning
        if (!movement.isHasted) {
            movement.setHaste(true);
            System.out.println(monster.getName() + " has haste! (3x speed)");
        }
        
        // Check if back home
        float distFromHome = distance(position.x, position.y, ai.homeX, ai.homeY);
        if (distFromHome < 32f) {
            // Remove haste
            movement.setHaste(false);
            
            // Heal to full when reaching home
            Stats stats = monster.getComponent(Stats.class);
            if (stats != null && stats.hp < stats.maxHp) {
                int healAmount = stats.maxHp - stats.hp;
                stats.hp = stats.maxHp;
                System.out.println(monster.getName() + " healed to full! (+" + healAmount + " HP)");
                
                DamageText healText = new DamageText("+" + healAmount, DamageText.Type.HEAL, position.x, position.y - 30);
                state.addDamageText(healText);
            }
            
            transitionAIState(monster, ai, AI.State.IDLE);
            return;
        }
        
        // ⭐ REMOVED: Don't check for player while returning - just go home!
        // Once we commit to returning, we ignore everything until we're home
        
        // Path back home
        if (!movement.isMoving || (path != null && !path.isFollowing)) {
            int startTileX = (int)(position.x / TileMap.TILE_SIZE);
            int startTileY = (int)(position.y / TileMap.TILE_SIZE);
            int goalTileX = (int)(ai.homeX / TileMap.TILE_SIZE);
            int goalTileY = (int)(ai.homeY / TileMap.TILE_SIZE);
            
            if (startTileX == goalTileX && startTileY == goalTileY) {
                // Remove haste
                movement.setHaste(false);
                
                // Heal to full
                Stats stats = monster.getComponent(Stats.class);
                if (stats != null && stats.hp < stats.maxHp) {
                    int healAmount = stats.maxHp - stats.hp;
                    stats.hp = stats.maxHp;
                    System.out.println(monster.getName() + " healed to full! (+" + healAmount + " HP)");
                    
                    DamageText healText = new DamageText("+" + healAmount, DamageText.Type.HEAL, position.x, position.y - 30);
                    state.addDamageText(healText);
                }
                
                transitionAIState(monster, ai, AI.State.IDLE);
                return;
            }
            
            List<int[]> foundPath = state.getPathfinder().findPath(startTileX, startTileY, goalTileX, goalTileY);
            
            if (foundPath != null && path != null) {
                path.setPath(foundPath);
                movement.isRunning = false;
            } else {
                System.out.println(monster.getName() + " couldn't find path home, going idle");
                movement.setHaste(false);
                transitionAIState(monster, ai, AI.State.IDLE);
            }
        }
        
        // Animation - use run animation when hasted
        if (sprite != null && movement != null) {
            if (movement.isMoving) {
                if (movement.isHasted) {
                    sprite.setAnimation(getRunAnimationForDirection(movement.direction));
                } else {
                    sprite.setAnimation(getWalkAnimationForDirection(movement.direction));
                }
            } else {
                sprite.setAnimation(getIdleAnimationForDirection(movement.lastDirection));
            }
        }
    }
    /**
     * Make two entities face each other
     */
    private void makeFaceEachOther(Entity entity1, Entity entity2) {
        Position pos1 = entity1.getComponent(Position.class);
        Position pos2 = entity2.getComponent(Position.class);
        Movement move1 = entity1.getComponent(Movement.class);
        Movement move2 = entity2.getComponent(Movement.class);
        
        if (pos1 == null || pos2 == null) return;
        
        // Entity1 faces Entity2
        if (move1 != null) {
            float dx = pos2.x - pos1.x;
            float dy = pos2.y - pos1.y;
            move1.direction = calculateDirection(dx, dy);
            move1.lastDirection = move1.direction;
        }
        
        // Entity2 faces Entity1
        if (move2 != null) {
            float dx = pos1.x - pos2.x;
            float dy = pos1.y - pos2.y;
            move2.direction = calculateDirection(dx, dy);
            move2.lastDirection = move2.direction;
        }
    }
    
    private void handleAttackingState(Entity monster, AI ai, Movement movement, Sprite sprite, Position playerPos, Stats stats, float delta) {
        Position monsterPos = monster.getComponent(Position.class);
        Entity player = state.getPlayer();
        
        if (playerPos == null || monsterPos == null) {
            transitionAIState(monster, ai, AI.State.IDLE);
            return;
        }
        
        // Check if player is dead
        if (player != null) {
            Stats playerStats = player.getComponent(Stats.class);
            if (playerStats != null && playerStats.hp <= 0) {
                transitionAIState(monster, ai, AI.State.VICTORY_IDLE);
                return;
            }
        }
        
        float distToPlayer = distance(monsterPos.x, monsterPos.y, playerPos.x, playerPos.y);
        
        // Player moved away
        if (distToPlayer > ai.attackRange * 1.5f) {
            transitionAIState(monster, ai, AI.State.CHASING);
            return;
        }
        
        // Make monster face player
        makeFaceEachOther(monster, player);
        
        // Attack if cooldown ready
        if (ai.canAttack()) {
            Combat monsterCombat = monster.getComponent(Combat.class);
            if (monsterCombat != null) {
                monsterCombat.startAttack();
            }
            
            tryAttack(monster, player);
            ai.resetAttackCooldown();
            
            if (sprite != null && movement != null) {
                sprite.setAnimation(getAttackAnimationForDirection(movement.lastDirection));
            }
        } else {
            Combat monsterCombat = monster.getComponent(Combat.class);
            if (sprite != null && movement != null) {
                if (monsterCombat != null && monsterCombat.isAttacking) {
                    sprite.setAnimation(getAttackAnimationForDirection(movement.lastDirection));
                } else {
                    sprite.setAnimation(getIdleAnimationForDirection(movement.lastDirection));
                }
            }
        }
    }
    
    private void handleMonsterDeath(Entity monster, Sprite sprite) {
        System.out.println(monster.getName() + " has died!");
        
        // Clear as auto-attack target
        if (state.getAutoAttackTarget() == monster) {
            state.clearAutoAttackTarget();
        }
        
        // Clear as targeted entity
        if (state.getTargetedEntity() == monster) {
            state.setTargetedEntity(null);
        }
        
        // Add dead component
        monster.addComponent(new Dead(5f));
        
        // Stop movement
        Movement movement = monster.getComponent(Movement.class);
        if (movement != null) {
            movement.stopMoving();
        }
        
        Path path = monster.getComponent(Path.class);
        if (path != null) {
            path.clear();
        }
        
        // Set AI to dead state
        AI ai = monster.getComponent(AI.class);
        if (ai != null) {
            ai.currentState = AI.State.DEAD;
        }
        
        // Play death animation
        if (sprite != null) {
            sprite.setAnimation(Sprite.ANIM_DEAD);
        }
    }
    /**
     * Detect player using distance check (can add dot product later for FOV)
     */
    private boolean canDetectPlayer(Position monsterPos, Position playerPos, float detectionTiles) {
        float detectionDistance = detectionTiles * TileMap.TILE_SIZE;
        float dist = distance(monsterPos.x, monsterPos.y, playerPos.x, playerPos.y);
        return dist <= detectionDistance;
    }
    
    private float distance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float)Math.sqrt(dx * dx + dy * dy);
    }
    
    public void tryAttack(Entity attacker, Entity target) {
        Position attackerPos = attacker.getComponent(Position.class);
        Position targetPos = target.getComponent(Position.class);
        
        if (attackerPos == null || targetPos == null) {
            System.out.println("ERROR: tryAttack called without valid positions!");
            return;
        }
        
        performAttack(attacker, target, attackerPos, targetPos);
    }
    /**
     * Follow the current path waypoint by waypoint
     */
    private void followPath(Entity entity, Path path, Movement movement, Position position, float delta) {
        int[] waypoint = path.getCurrentWaypoint();
        
        if (waypoint == null) {
            path.clear();
            movement.stopMoving();
            return;
        }
        
        // Convert tile coordinates to world coordinates (center of tile)
        float waypointWorldX = waypoint[0] * TileMap.TILE_SIZE + TileMap.TILE_SIZE / 2f;
        float waypointWorldY = waypoint[1] * TileMap.TILE_SIZE + TileMap.TILE_SIZE / 2f;
        
        // Check if we're close to the waypoint
        float dx = waypointWorldX - position.x;
        float dy = waypointWorldY - position.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (distance < 5f) {
            // Reached waypoint, move to next
            path.advanceWaypoint();
            
            if (!path.isFollowing) {
                // Reached final destination
                movement.stopMoving();
                
                // Snap to exact position
                position.x = waypointWorldX;
                position.y = waypointWorldY;
            } else {
                // Set target to next waypoint
                int[] nextWaypoint = path.getCurrentWaypoint();
                if (nextWaypoint != null) {
                    float nextX = nextWaypoint[0] * TileMap.TILE_SIZE + TileMap.TILE_SIZE / 2f;
                    float nextY = nextWaypoint[1] * TileMap.TILE_SIZE + TileMap.TILE_SIZE / 2f;
                    movement.setTarget(nextX, nextY, movement.isRunning);
                } else {
                    // No more waypoints
                    path.clear();
                    movement.stopMoving();
                }
            }
        } else {
            // Still moving to current waypoint
            if (!movement.isMoving) {
                movement.setTarget(waypointWorldX, waypointWorldY, movement.isRunning);
            }
        }
    }
    
    private String getIdleAnimationForDirection(int direction) {
        switch(direction) {
            case Movement.DIR_EAST:
                return Sprite.ANIM_IDLE_RIGHT;
            case Movement.DIR_SOUTH_EAST:
                return Sprite.ANIM_IDLE_DOWN_RIGHT;
            case Movement.DIR_SOUTH:
                return Sprite.ANIM_IDLE_DOWN;
            case Movement.DIR_SOUTH_WEST:
                return Sprite.ANIM_IDLE_DOWN_LEFT;
            case Movement.DIR_WEST:
                return Sprite.ANIM_IDLE_LEFT;
            case Movement.DIR_NORTH_WEST:
                return Sprite.ANIM_IDLE_UP_LEFT;
            case Movement.DIR_NORTH:
                return Sprite.ANIM_IDLE_UP;
            case Movement.DIR_NORTH_EAST:
                return Sprite.ANIM_IDLE_UP_RIGHT;
            default:
                return Sprite.ANIM_IDLE_DOWN;
        }
    }
    
    private void moveTowardsTarget(Entity entity, Movement movement, Position position, float delta) {
        float dx = movement.targetX - position.x;
        float dy = movement.targetY - position.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (distance < 2f) {
            position.x = movement.targetX;
            position.y = movement.targetY;
            movement.stopMoving();
            return;
        }
        
        float moveAmount = movement.currentSpeed * delta;
        
        if (moveAmount >= distance) {
            // Would reach destination - check if destination is valid
            CollisionBox collisionBox = entity.getComponent(CollisionBox.class);
            TileMap map = state.getMap();
            
            if (collisionBox != null && map != null) {
                if (map.collidesWithTiles(collisionBox, movement.targetX, movement.targetY)) {
                    // Destination is blocked, stop here
                    movement.stopMoving();
                    return;
                }
            }
            
            position.x = movement.targetX;
            position.y = movement.targetY;
            movement.stopMoving();
        } else {
            // Moving partial distance
            float ratio = moveAmount / distance;
            float newX = position.x + dx * ratio;
            float newY = position.y + dy * ratio;
            
            // Check collision at new position
            CollisionBox collisionBox = entity.getComponent(CollisionBox.class);
            TileMap map = state.getMap();
            
            if (collisionBox != null && map != null) {
                // Try moving on both axes
                if (!map.collidesWithTiles(collisionBox, newX, newY)) {
                    // No collision, move normally
                    position.x = newX;
                    position.y = newY;
                } else {
                    // Collision detected - try sliding along walls
                    
                    // Try X axis only
                    if (!map.collidesWithTiles(collisionBox, newX, position.y)) {
                        position.x = newX;
                    }
                    // Try Y axis only
                    else if (!map.collidesWithTiles(collisionBox, position.x, newY)) {
                        position.y = newY;
                    }
                    // Completely blocked
                    else {
                        movement.stopMoving();
                        return;
                    }
                }
            } else {
                // No collision box, move freely
                position.x = newX;
                position.y = newY;
            }
            
            movement.direction = calculateDirection(dx, dy);
        }
    }
    
    private int calculateDirection(float dx, float dy) {
        // Returns 0-7 for 8 directions
        // 0=East, 1=SE, 2=South, 3=SW, 4=West, 5=NW, 6=North, 7=NE
        double angle = Math.atan2(dy, dx);
        int direction = (int) Math.round(angle / (Math.PI / 4));
        return (direction + 8) % 8;
    }
    
    private String getWalkAnimationForDirection(int direction) {
        switch(direction) {
            case Movement.DIR_EAST:
                return Sprite.ANIM_WALK_RIGHT;
            case Movement.DIR_SOUTH_EAST:
                return Sprite.ANIM_WALK_DOWN_RIGHT;
            case Movement.DIR_SOUTH:
                return Sprite.ANIM_WALK_DOWN;
            case Movement.DIR_SOUTH_WEST:
                return Sprite.ANIM_WALK_DOWN_LEFT;
            case Movement.DIR_WEST:
                return Sprite.ANIM_WALK_LEFT;
            case Movement.DIR_NORTH_WEST:
                return Sprite.ANIM_WALK_UP_LEFT;
            case Movement.DIR_NORTH:
                return Sprite.ANIM_WALK_UP;
            case Movement.DIR_NORTH_EAST:
                return Sprite.ANIM_WALK_UP_RIGHT;
            default:
                return Sprite.ANIM_WALK_DOWN;
        }
    }
    
    private String getRunAnimationForDirection(int direction) {
        switch(direction) {
            case Movement.DIR_EAST:
                return Sprite.ANIM_RUN_RIGHT;
            case Movement.DIR_SOUTH_EAST:
                return Sprite.ANIM_RUN_DOWN_RIGHT;
            case Movement.DIR_SOUTH:
                return Sprite.ANIM_RUN_DOWN;
            case Movement.DIR_SOUTH_WEST:
                return Sprite.ANIM_RUN_DOWN_LEFT;
            case Movement.DIR_WEST:
                return Sprite.ANIM_RUN_LEFT;
            case Movement.DIR_NORTH_WEST:
                return Sprite.ANIM_RUN_UP_LEFT;
            case Movement.DIR_NORTH:
                return Sprite.ANIM_RUN_UP;
            case Movement.DIR_NORTH_EAST:
                return Sprite.ANIM_RUN_UP_RIGHT;
            default:
                return Sprite.ANIM_RUN_DOWN;
        }
    }
    private void updateCamera(float delta) {
        Entity player = state.getPlayer();
        Position playerPos = player.getComponent(Position.class);
        TileMap map = state.getMap();
        
        if (playerPos != null && map != null) {
            float targetX = playerPos.x - Engine.WIDTH / 2;
            float targetY = playerPos.y - Engine.HEIGHT / 2;
            
            // Clamp to map bounds
            float maxCameraX = map.getWidthInPixels() - Engine.WIDTH;
            float maxCameraY = map.getHeightInPixels() - Engine.HEIGHT;
            
            targetX = Math.max(0, Math.min(targetX, maxCameraX));
            targetY = Math.max(0, Math.min(targetY, maxCameraY));
            
            // Smooth lerp
            float currentX = state.getCameraX();
            float currentY = state.getCameraY();
            
            float newX = currentX + (targetX - currentX) * cameraLerpSpeed * delta;
            float newY = currentY + (targetY - currentY) * cameraLerpSpeed * delta;
            
            state.setCameraPosition(newX, newY);
        }
    } 
    
    public void movePlayerTo(float worldX, float worldY, boolean run) {
        Entity player = state.getPlayer();
        Position position = player.getComponent(Position.class);
        Movement movement = player.getComponent(Movement.class);
        Path path = player.getComponent(Path.class);
        TargetIndicator indicator = player.getComponent(TargetIndicator.class);  // NEW
        
        if (position == null || movement == null || path == null) {
            return;
        }
        
        // Determine smart starting point for pathfinding
        int startTileX, startTileY;
        
        if (path.isFollowing && path.waypoints != null && path.currentWaypoint < path.waypoints.size()) {
            int[] currentWaypoint = path.waypoints.get(path.currentWaypoint);
            startTileX = currentWaypoint[0];
            startTileY = currentWaypoint[1];
        } else {
            startTileX = (int)(position.x / TileMap.TILE_SIZE);
            startTileY = (int)(position.y / TileMap.TILE_SIZE);
        }
        
        int goalTileX = (int)(worldX / TileMap.TILE_SIZE);
        int goalTileY = (int)(worldY / TileMap.TILE_SIZE);
        
        // Check if clicking the same tile
        if (path.isFollowing && path.waypoints != null && !path.waypoints.isEmpty()) {
            int[] lastWaypoint = path.waypoints.get(path.waypoints.size() - 1);
            if (lastWaypoint[0] == goalTileX && lastWaypoint[1] == goalTileY) {
                movement.isRunning = run;
                return;
            }
        }
        
        // Find new path
        Pathfinder pathfinder = state.getPathfinder();
        List<int[]> foundPath = pathfinder.findPath(startTileX, startTileY, goalTileX, goalTileY);
        
        if (foundPath != null) {
            path.setPath(foundPath);
            movement.isRunning = run;
            
            // Set target indicator at final destination
            if (indicator != null) {
                indicator.setTarget(worldX, worldY);
            }
            
            System.out.println("Path found with " + foundPath.size() + " waypoints");
        } else {
            System.out.println("No path to destination!");
            path.clear();
            movement.stopMoving();
            
            if (indicator != null) {
                indicator.clear();
            }
        }
    }
    public void setCameraLerpSpeed(float speed) {
        this.cameraLerpSpeed = speed;
    }
}
/*
## Visual Guide of What Should Happen
```
[HOME] ---roamRadius---> [ROAM ZONE] ---returnThreshold---> [LEASH BREAK]
  |                           |                                    |
  IDLE/ROAMING           CAN CHASE                           MUST RETURN
                         PLAYER HERE                         (ignore player)
*/