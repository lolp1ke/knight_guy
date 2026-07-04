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
      if (anim.finished) {
        return;
      }
      double duration = anim.getCurrentFrameDuration();
      anim.frameTimer += time.delta;
      if (anim.frameTimer >= duration) {
        int next = anim.frame + 1;
        if (next >= anim.getCurrentFrameCount()) {
          if (anim.isLooping()) {
            anim.frame = 0;
          } else {
            anim.frame = anim.getCurrentFrameCount() - 1;
            anim.finished = true;
          }
        } else {
          anim.frame = next;
        }
        anim.frameTimer -= duration;
      }
    });
  }
}
