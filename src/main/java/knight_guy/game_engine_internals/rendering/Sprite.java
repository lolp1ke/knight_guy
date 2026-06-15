package knight_guy.game_engine_internals.rendering;

import javafx.scene.image.Image;
import knight_guy.game_engine_internals.Component;

// reusable sprite class for game entities
public final class Sprite implements Component {

  public Image image;
  public double width;
  public double height;
  public int z_index;

  public Sprite() {
    this.image = null;
    this.width = 0;
    this.height = 0;
    this.z_index = 0;
  }

  public Sprite(Image image) {
    this.image = image;
    this.width = image.getWidth();
    this.height = image.getHeight();
    this.z_index = 0;
  }

  public Sprite(Image image, double width, double height) {
    this.image = image;
    this.width = width;
    this.height = height;
    this.z_index = 0;
  }
}
