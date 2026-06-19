package knight_guy.game_engine_internals.rendering;

import javafx.scene.image.Image;

// reusable static sprite class for game entities with no animation
public final class StaticSprite extends Sprite {

  public Image asset;

  public StaticSprite() {
    this.asset = null;
    this.width = 0;
    this.height = 0;
    this.zIndex = -1;
  }

  public StaticSprite(Image image) {
    this.asset = image;
    this.width = image.getWidth();
    this.height = image.getHeight();
    this.zIndex = 0;
  }

  public StaticSprite(Image image, double width, double height) {
    this.asset = image;
    this.width = width;
    this.height = height;
    this.zIndex = 0;
  }
}
