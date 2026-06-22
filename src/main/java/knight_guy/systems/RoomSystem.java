package knight_guy.systems;

import knight_guy.Consts;
import knight_guy.Exit;
import knight_guy.game_engine_internals.System;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.components.Transform2D;
import knight_guy.game_engine_internals.rendering.StaticSprite;
import knight_guy.rooms.RoomManager;

public final class RoomSystem implements System, Consts {

  public void run(World world) {
    RoomManager manager = world.getResource(RoomManager.class);
    if (
      manager == null ||
      manager.player == null ||
      !world.isAlive(manager.player)
    ) {
      return;
    }

    Transform2D playerTransform = world.getComponent(
      manager.player,
      Transform2D.class
    );

    double pLeft = playerTransform.x;
    double pRight = playerTransform.x + PLAYER_W;
    double pTop = playerTransform.y + PLAYER_H;
    double pBottom = playerTransform.y;

    world
      .query(Transform2D.class, StaticSprite.class, Exit.class)
      .forEach((_, components) -> {
        Transform2D exitTransform = (Transform2D) components[0];
        StaticSprite exitSprite = (StaticSprite) components[1];
        Exit exit = (Exit) components[2];

        double eLeft = exitTransform.x;
        double eRight = exitTransform.x + exitSprite.width;
        double eTop = exitTransform.y;
        double eBottom = exitTransform.y + exitSprite.height;

        if (
          pRight > eLeft && pLeft < eRight && pBottom > eTop && pTop < eBottom
        ) {
          if (exit.target != null) {
            manager.transition(exit.target, world);
          }
        }
      });
  }
}
