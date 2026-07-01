package knight_guy.game_engine_internals.rendering;

import java.util.HashMap;
import javafx.scene.image.Image;

// each animation tag has its own image strip (horizontal frames)
public final class AnimatedSprite extends Sprite {

  private static final record AnimDef(
    Image image,
    int frameCount,
    boolean loop
  ) {}

  public double offsetX;
  public double offsetY;

  public double frameWidth;
  public double frameHeight;
  public double sourceX;
  public double sourceY;

  public int frame;
  public double frameTimer;
  public double frameDuration;

  public boolean finished;

  public String animationTag;
  private final HashMap<String, AnimDef> animations;
  private AnimDef currentAnim;

  public AnimatedSprite(double width, double height, double frameDuration) {
    this.width = width;
    this.height = height;
    this.frameDuration = frameDuration;
    this.frameWidth = width;
    this.frameHeight = height;
    this.sourceX = 0;
    this.sourceY = 0;
    this.offsetX = 0;
    this.offsetY = 0;
    this.zIndex = 0;
    this.frame = 0;
    this.frameTimer = 0;
    this.animationTag = null;
    this.animations = new HashMap<>();
    this.currentAnim = null;
  }

  public void addAnimation(String tag, Image image, int frameCount) {
    this.addAnimation(tag, image, frameCount, true);
  }

  public void addAnimation(
    String tag,
    Image image,
    int frameCount,
    boolean loop
  ) {
    this.animations.put(tag, new AnimDef(image, frameCount, loop));
  }

  public void setAnimation(String tag) {
    if (!tag.equals(this.animationTag)) {
      this.animationTag = tag;
      this.currentAnim = this.animations.get(tag);
      this.frame = 0;
      this.frameTimer = 0;
      this.finished = false;
    }
  }

  public boolean isLooping() {
    return currentAnim == null || currentAnim.loop;
  }

  public Image getCurrentImage() {
    return this.currentAnim != null ? this.currentAnim.image : null;
  }

  public int getCurrentFrameCount() {
    return this.currentAnim != null ? this.currentAnim.frameCount : 1;
  }
}
