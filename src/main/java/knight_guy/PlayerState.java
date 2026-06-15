package knight_guy;

import knight_guy.game_engine_internals.Resource;

public class PlayerState implements Resource {

  public double dash_cooldown = 0;
  public double dash_timer = 0;
  public double attack_cooldown = 0;
  public double attack_timer = 0;
  public boolean facing_right = true;
}
