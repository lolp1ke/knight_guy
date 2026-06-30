package knight_guy.systems;

import knight_guy.Consts;
import knight_guy.Player;
import knight_guy.game_engine_internals.System;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.components.Transform2D;
import knight_guy.game_engine_internals.rendering.Camera2D;

public final class CameraSystem implements System, Consts {

  @Override
  public void run(World world) {
    Camera2D camera = world.getResource(Camera2D.class);
    if (camera == null) {
      return;
    }

    world
      .query(Transform2D.class)
      .with(Player.class)
      .forEach((_, components) -> {
        Transform2D playerTransform = (Transform2D) components[0];
        camera.x = playerTransform.x + PLAYER_W / 2.0 - SCREEN_WIDTH / 2.0;
        camera.y = playerTransform.y + PLAYER_H / 2.0 - SCREEN_HEIGHT / 2.0;
      });
  }
}
