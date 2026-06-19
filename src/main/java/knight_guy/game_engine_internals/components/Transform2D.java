package knight_guy.game_engine_internals.components;

import knight_guy.game_engine_internals.Component;

// built in component for positioning
public final class Transform2D implements Component {

  public double x;
  public double y;
  public double rotation;
  public double scaleX;
  public double scaleY;

  public Transform2D() {
    this.x = 0;
    this.y = 0;
  }

  public Transform2D(double x, double y) {
    this.x = x;
    this.y = y;
    this.rotation = 0;
    this.scaleX = 1;
    this.scaleY = 1;
  }
}
