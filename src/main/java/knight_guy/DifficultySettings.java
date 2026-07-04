package knight_guy;

import knight_guy.game_engine_internals.Resource;

// holds every enemy-facing number that changes between difficulties, so
// balancing lives in one place instead of being scattered across systems
public final class DifficultySettings implements Resource {

  public final String label;

  public final int enemyMaxHp;
  // how close the player has to be before an enemy will start swinging
  public final double enemyAttackRange;
  // seconds between the end of one enemy attack and the next one starting
  public final double enemyAttackCooldown;
  public final int enemyDamage;
  // how many of the platform spawn points actually get an enemy
  public final int platformEnemyCount;
  // Hard ends its loop with a boss fight (see BossRoom) instead of going
  // straight to the ending; Easy skips straight to the ending
  public final boolean bossFightEnabled;

  private DifficultySettings(
    String label,
    int enemyMaxHp,
    double enemyAttackRange,
    double enemyAttackCooldown,
    int enemyDamage,
    int platformEnemyCount,
    boolean bossFightEnabled
  ) {
    this.label = label;
    this.enemyMaxHp = enemyMaxHp;
    this.enemyAttackRange = enemyAttackRange;
    this.enemyAttackCooldown = enemyAttackCooldown;
    this.enemyDamage = enemyDamage;
    this.platformEnemyCount = platformEnemyCount;
    this.bossFightEnabled = bossFightEnabled;
  }

  // forgiving: enemies are slower to swing, easier to stagger through,
  // and there are fewer of them per room
  public static DifficultySettings easy() {
    return new DifficultySettings("Easy", 2, 55.0, 2.2, 1, 2, false);
  }

  // the three regular rooms play identically to Easy now - the boss fight
  // at the end is what actually makes Hard hard, instead of also tuning
  // up every regular enemy along the way
  public static DifficultySettings hard() {
    return new DifficultySettings("Hard", 2, 55.0, 2.2, 1, 2, true);
  }
}
