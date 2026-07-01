package knight_guy.systems;

import knight_guy.Consts;
import knight_guy.Player;
import knight_guy.game_engine_internals.System;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.components.Transform2D;
import knight_guy.game_engine_internals.rendering.Camera2D;
import knight_guy.game_engine_internals.resources.Time;

public final class CameraSystem implements System, Consts {

  @Override
  public void run(World world) {
    Camera2D camera = world.getResource(Camera2D.class);
    final Time time = world.getResource(Time.class);
    if (camera == null || time == null) {
      return;
    }

    final double smoothness = 2.5;
    world
      .query(Transform2D.class)
      .with(Player.class)
      .forEach((_, components) -> {
        final Transform2D playerTransform = (Transform2D) components[0];

        final Transform2D newCameraPos = new Transform2D(
          playerTransform.x - SCREEN_WIDTH / 2.0d,
          playerTransform.y - SCREEN_HEIGHT / 2.0d
        );
        camera.lerp(newCameraPos, smoothness * time.delta);
      });
  }
}
