package knight_guy.game_engine_internals.rendering;

import knight_guy.game_engine_internals.System;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.resources.Time;

// advances frame for all AnimatedSprite components each update
public final class AnimationSystem implements System {

  @Override
  public void run(World world) {
    Time time = world.getResource(Time.class);
    if (time == null) {
      return;
    }

    world.query(AnimatedSprite.class).forEach((_, components) -> {
      AnimatedSprite anim = (AnimatedSprite) components[0];
      anim.frameTimer += time.delta;
      if (anim.frameTimer >= anim.frameDuration) {
        anim.frameTimer -= anim.frameDuration;
        anim.frame = (anim.frame + 1) % anim.getCurrentFrameCount();
      }
    });
  }
}
