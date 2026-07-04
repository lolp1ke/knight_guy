package knight_guy;

import javafx.scene.image.Image;
import knight_guy.game_engine_internals.AssetStore;
import knight_guy.game_engine_internals.Entity;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.components.Transform2D;
import knight_guy.game_engine_internals.components.Velocity2D;
import knight_guy.game_engine_internals.rendering.AnimatedSprite;

public final class EnemyFactory implements Consts {

  public static Entity create(
    World world,
    double x,
    double y,
    DifficultySettings difficulty
  ) {
    // Create enemy sprite
    final double ENEMY_FRAME_W = 140.0;
    final double ENEMY_FRAME_H = 93.0;

    AnimatedSprite enemySprite = new AnimatedSprite(
      ENEMY_FRAME_W,
      ENEMY_FRAME_H,
      0.15
    );

    enemySprite.frameWidth = ENEMY_FRAME_W;
    enemySprite.frameHeight = ENEMY_FRAME_H;
    enemySprite.sourceX = 0.0;
    enemySprite.sourceY = 0.0;
    // center the sprite horizontally on its hitbox (it defaulted to 0, which
    // drew the whole 140px-wide frame starting at the hitbox's left edge)
    enemySprite.offsetX = -ENEMY_FRAME_W / 2.0;
    // the character's feet sit almost exactly at the bottom of the frame
    enemySprite.offsetY = ENEMY_H / 2.0 - ENEMY_FRAME_H;
    enemySprite.zIndex = 100;

    // Load animation images
    Image idle = AssetStore.load("enemy/idle.png");
    Image walk = AssetStore.load("enemy/walk.png");
    Image attack = AssetStore.load("enemy/attack.png");
    Image death = AssetStore.load("enemy/death.png");
    Image spell = AssetStore.load("enemy/spell.png");
    Image cast = AssetStore.load("enemy/cast.png");

    // Register animations
    enemySprite.addAnimation("idle", idle, 8);
    enemySprite.addAnimation("walk", walk, 8);
    enemySprite.addAnimation("attack", attack, 10, false);
    enemySprite.addAnimation("death", death, 10, false);
    enemySprite.addAnimation("spell", spell, 8, false);
    enemySprite.addAnimation("cast", cast, 9, false);

    // Default animation
    enemySprite.setAnimation("idle");

    EnemyState enemyState = new EnemyState();

    // apply difficulty tuning - fall back to the old fixed defaults if no
    // difficulty resource is present for some reason, so this never NPEs
    if (difficulty != null) {
      enemyState.hp = difficulty.enemyMaxHp;
      enemyState.maxHp = difficulty.enemyMaxHp;
      enemyState.attackRange = difficulty.enemyAttackRange;
      enemyState.attackCooldownMax = difficulty.enemyAttackCooldown;
      enemyState.attackDamage = difficulty.enemyDamage;
    }

    // Spawn enemy
    return world.spawn(
      new Enemy(),
      enemyState,
      enemySprite,
      new Transform2D(x, y),
      new Velocity2D()
    );
  }
}
