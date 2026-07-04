package knight_guy;

import knight_guy.game_engine_internals.Component;

public final class EnemyState implements Component {

  // Health
  public int hp = 2;
  // set alongside hp whenever an enemy is configured, so render systems
  // can compute a percentage without needing to know the source difficulty
  public int maxHp = 2;

  // flags the boss specifically so the boss health bar render system can
  // find it - the generic room-clear check doesn't care either way
  public boolean isBoss = false;

  // Direction
  public boolean facingRight = true;

  // fall start
  public double fallStartY = 0;

  // Ground
  public boolean onGround = false;

  // Movement
  public boolean moving = false;

  // Combat
  public boolean attacking = false;
  public boolean attackHit = false;

  // Timers
  public double attackCooldown = 0.0;
  public double attackTimer = 0.0;

  // Death
  public boolean dying = false;

  // Difficulty-driven combat stats. These default to the old fixed
  // constants below, but EnemyFactory overwrites them per-enemy based on
  // the DifficultySettings that were active when it was spawned.
  public double attackRange = ATTACK_RANGE;
  public double attackCooldownMax = ATTACK_COOLDOWN;
  public double attackDuration = ATTACK_DURATION;
  public int attackDamage = 1;

  public static final double ATTACK_RANGE = 80.0;
  public static final double ATTACK_DURATION = 0.5;
  public static final double ATTACK_COOLDOWN = 1.0;

  public void takeDamage(int amount) {
    if (amount <= 0 || isDead()) {
      return;
    }

    hp -= amount;

    if (hp < 0) {
      hp = 0;
    }
  }

  public boolean isDead() {
    return hp <= 0;
  }
}
