package knight_guy.game_engine_internals.rendering;

import knight_guy.game_engine_internals.Component;
import knight_guy.game_engine_internals.Resource;
import knight_guy.game_engine_internals.components.Transform2D;

// does nothing right now
public final class Camera2D implements Resource, Component {

  public double x;
  public double y;
  public double zoom;

  public Camera2D() {
    this.x = 0;
    this.y = 0;
    this.zoom = 1.0;
  }

  public void lerp(Transform2D rhs, double s) {
    this.x = this.x * (1.0d - s) + rhs.x * s;
    this.y = this.y * (1.0d - s) + rhs.y * s;
  }
}
