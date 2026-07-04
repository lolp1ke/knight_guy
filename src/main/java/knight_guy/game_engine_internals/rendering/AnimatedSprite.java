package knight_guy.game_engine_internals.rendering;

import java.util.HashMap;
import javafx.scene.image.Image;

// each animation tag has its own image strip (horizontal frames)
public final class AnimatedSprite extends Sprite {

  private static final record AnimDef(
    Image image,
    int frameCount,
    boolean loop,
    // null means "use the sprite's default frameDuration" - lets a single
    // AnimatedSprite play most animations at one speed while a specific
    // one (e.g. an attack swing) plays faster or slower
    Double frameDuration
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
    this.addAnimation(tag, image, frameCount, true, null);
  }

  public void addAnimation(
    String tag,
    Image image,
    int frameCount,
    boolean loop
  ) {
    this.addAnimation(tag, image, frameCount, loop, null);
  }

  // lets a specific animation (e.g. a sword swing) play at its own speed
  // instead of the sprite's default frameDuration
  public void addAnimation(
    String tag,
    Image image,
    int frameCount,
    boolean loop,
    Double frameDurationOverride
  ) {
    this.animations.put(
      tag,
      new AnimDef(image, frameCount, loop, frameDurationOverride)
    );
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

  // the effective per-frame duration for whatever animation is currently
  // playing - falls back to this sprite's default duration when the
  // animation doesn't specify its own
  public double getCurrentFrameDuration() {
    if (this.currentAnim != null && this.currentAnim.frameDuration() != null) {
      return this.currentAnim.frameDuration();
    }
    return this.frameDuration;
  }
}
