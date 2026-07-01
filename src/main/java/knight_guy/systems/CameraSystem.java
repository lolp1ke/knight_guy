package knight_guy.systems;

import javafx.scene.input.KeyCode;
import knight_guy.Consts;
import knight_guy.Player;
import knight_guy.game_engine_internals.Input;
import knight_guy.game_engine_internals.System;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.components.Transform2D;
import knight_guy.game_engine_internals.rendering.Camera2D;
import knight_guy.game_engine_internals.resources.Time;

public final class CameraSystem implements System, Consts {

  private static final double CAMERA_SPEED = 500.0;

  private static final double SMOOTHNESS = 2.5;
  private boolean freeCam = false;

  @Override
  public void run(World world) {
    Camera2D camera = world.getResource(Camera2D.class);
    final Time time = world.getResource(Time.class);
    final Input input = world.getResource(Input.class);
    if (camera == null || time == null || input == null) {
      return;
    }

    boolean left = input.pressed(KeyCode.LEFT);
    boolean right = input.pressed(KeyCode.RIGHT);
    boolean up = input.pressed(KeyCode.UP);
    boolean down = input.pressed(KeyCode.DOWN);

    freeCam = left || right || up || down;

    if (freeCam) {
      if (left) {
        camera.x -= CAMERA_SPEED * time.delta;
      }
      if (right) {
        camera.x += CAMERA_SPEED * time.delta;
      }
      if (up) {
        camera.y -= CAMERA_SPEED * time.delta;
      }
      if (down) {
        camera.y += CAMERA_SPEED * time.delta;
      }
    } else {
      world
        .query(Transform2D.class)
        .with(Player.class)
        .forEach((_, components) -> {
          final Transform2D playerTransform = (Transform2D) components[0];

          final Transform2D newCameraPos = new Transform2D(
            playerTransform.x - SCREEN_WIDTH / 2.0d,
            playerTransform.y - SCREEN_HEIGHT / 2.0d
          );
          camera.lerp(newCameraPos, SMOOTHNESS * time.delta);
        });
    }
  }
}
