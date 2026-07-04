package knight_guy;

import javafx.scene.image.Image;
import knight_guy.game_engine_internals.AssetStore;
import knight_guy.game_engine_internals.Entity;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.components.Transform2D;
import knight_guy.game_engine_internals.components.Velocity2D;
import knight_guy.game_engine_internals.rendering.AnimatedSprite;

public final class BossFactory implements Consts {

  // the provided sprite sheets are 200x200 per frame, but the actual
  // character art only occupies a small area near the middle of each
  // tile (measured: roughly x[76,113] y[70,122]) - these constants exist
  // so the boss's feet line up with the floor instead of floating
  private static final double FRAME_SIZE = 200.0;
  private static final double FEET_FROM_TOP = 122.0;

  // logical footprint used for spawn positioning - a bit bigger than a
  // regular enemy's, since this is the boss
  public static final double BOSS_W = 50.0;
  public static final double BOSS_H = 70.0;

  // boss stats - eased off from the first pass (short cooldown, long
  // reach, high HP on top of the regular rooms also being harder). Now
  // that the three regular rooms match Easy, the boss doesn't need to
  // compensate as much. The swing itself is also slower now (both the
  // animation and the logical attack window), with a longer cooldown
  // between swings, so it reads clearly and gives real time to react -
  // moving out of range during the wind-up is enough to avoid it.
  private static final int BOSS_MAX_HP = 8;
  private static final double BOSS_ATTACK_RANGE = 70.0;
  private static final double BOSS_ATTACK_COOLDOWN = 2.0;
  private static final double BOSS_ATTACK_DURATION = 0.7;
  private static final double BOSS_ATTACK_FRAME_DURATION = 0.16;
  private static final int BOSS_ATTACK_DAMAGE = 1;

  public static Entity create(World world, double x, double y) {
    AnimatedSprite bossSprite = new AnimatedSprite(
      FRAME_SIZE,
      FRAME_SIZE,
      0.12
    );

    bossSprite.frameWidth = FRAME_SIZE;
    bossSprite.frameHeight = FRAME_SIZE;
    bossSprite.sourceX = 0.0;
    bossSprite.sourceY = 0.0;
    bossSprite.offsetX = -FRAME_SIZE / 2.0;
    bossSprite.offsetY = BOSS_H / 2.0 - FEET_FROM_TOP;
    bossSprite.zIndex = 100;

    Image idle = AssetStore.load("Boss/Idle.png");
    Image attack = AssetStore.load("Boss/Attack1.png");
    Image death = AssetStore.load("Boss/Death.png");

    bossSprite.addAnimation("idle", idle, 8);
    // slower than the sprite's base 0.12s/frame, so the wind-up is
    // actually readable instead of a quick blur
    bossSprite.addAnimation(
      "attack",
      attack,
      6,
      false,
      BOSS_ATTACK_FRAME_DURATION
    );
    bossSprite.addAnimation("death", death, 6, false);
    bossSprite.setAnimation("idle");

    EnemyState bossState = new EnemyState();
    bossState.hp = BOSS_MAX_HP;
    bossState.maxHp = BOSS_MAX_HP;
    bossState.attackRange = BOSS_ATTACK_RANGE;
    bossState.attackCooldownMax = BOSS_ATTACK_COOLDOWN;
    bossState.attackDuration = BOSS_ATTACK_DURATION;
    bossState.attackDamage = BOSS_ATTACK_DAMAGE;
    bossState.isBoss = true;

    return world.spawn(
      new Enemy(),
      bossState,
      bossSprite,
      new Transform2D(x, y),
      new Velocity2D()
    );
  }
}
