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
  public int hp = 3;
  public double fallStartY = 0;
  public boolean climbing = false;
}
