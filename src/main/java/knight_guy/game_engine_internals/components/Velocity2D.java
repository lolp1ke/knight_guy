package knight_guy.game_engine_internals.components;

import knight_guy.game_engine_internals.Component;

// built in component for direction vector
public final class Velocity2D implements Component {

  public double x;
  public double y;

  public Velocity2D() {
    this.x = 0;
    this.y = 0;
  }

  public Velocity2D(double x, double y) {
    this.x = x;
    this.y = y;
  }
}
