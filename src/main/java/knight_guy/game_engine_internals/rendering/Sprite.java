package knight_guy.game_engine_internals.rendering;

import knight_guy.game_engine_internals.Component;

// general sprite class
// do not query usign Sprite class it won't work
public abstract class Sprite implements Component {

  public double width;
  public double height;
  public int zIndex;
}
