package knight_guy.game_engine_internals.rendering;

import javafx.scene.canvas.Canvas;
import knight_guy.game_engine_internals.Resource;

// draws everything, main entry for displaying stuff
public final class MainCanvas implements Resource {

  public final Canvas canvas;

  public MainCanvas(double width, double height) {
    this.canvas = new Canvas(width, height);
  }
}
