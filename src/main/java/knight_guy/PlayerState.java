package knight_guy;

import knight_guy.game_engine_internals.Resource;

public class PlayerState implements Resource {

  public boolean moving = false;
  public double dashCooldown = 0;
  public double dashTimer = 0;
  public boolean facingRight = true;
  public double platformDropTimer = 0;
  public boolean onGround = false;
  public int attackVariation = 1;
  public double attackComboTimer = 0;
  public double attackCooldown = 0;
  public double fallStartY = 0;

  public static final int MAX_HP = 4;
  public int hp = MAX_HP;

  public void takeDamage(int amount) {
    if (amount <= 0) {
      return;
    }

    hp -= amount;

    if (hp < 0) {
      hp = 0;
    }
  }

  public void heal(int amount) {
    if (amount <= 0) {
      return;
    }

    hp += amount;

    if (hp > MAX_HP) {
      hp = MAX_HP;
    }
  }

  public boolean isDead() {
    return hp <= 0;
  }
}
