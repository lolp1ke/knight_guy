package knight_guy.game_engine_internals.rendering;

import knight_guy.game_engine_internals.Resource;

// does nothing right now
public final class Camera2D implements Resource {

  public double x;
  public double y;
  public double zoom;

  public Camera2D() {
    this.x = 0;
    this.y = 0;
    this.zoom = 1.0;
  }
}
