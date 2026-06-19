package knight_guy;

import knight_guy.game_engine_internals.Resource;

public class PlayerState implements Resource {

  public double dashCooldown = 0;
  public double dashTimer = 0;
  public boolean facingRight = true;
}
