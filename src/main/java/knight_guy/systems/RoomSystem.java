package knight_guy.systems;

import knight_guy.Consts;
import knight_guy.Enemy;
import knight_guy.EnemyState;
import knight_guy.Exit;
import knight_guy.game_engine_internals.State;
import knight_guy.game_engine_internals.System;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.components.Transform2D;
import knight_guy.game_engine_internals.rendering.StaticSprite;
import knight_guy.game_engine_internals.resources.Time;
import knight_guy.rooms.RoomManager;
import knight_guy.states.GameState;

public final class RoomSystem implements System, Consts {

  public void run(World world) {
    State<GameState> state = world.getResource(State.class);

    if (state == null || state.getState() != GameState.Running) {
      return;
    }

    RoomManager manager = world.getResource(RoomManager.class);
    if (
      manager == null ||
      manager.player == null ||
      !world.isAlive(manager.player)
    ) {
      return;
    }

    Time time = world.getResource(Time.class);
    if (manager.lockedExitHintTimer > 0 && time != null) {
      manager.lockedExitHintTimer -= time.delta;
    }

    // count enemies still alive in this room - exits stay locked until
    // this hits zero, so you can't just sprint past everything
    int[] livingEnemies = { 0 };
    world
      .query(EnemyState.class)
      .with(Enemy.class)
      .forEach((_, components) -> {
        EnemyState enemyState = (EnemyState) components[0];
        if (!enemyState.isDead()) {
          livingEnemies[0]++;
        }
      });
    boolean roomCleared = livingEnemies[0] == 0;

    Transform2D playerTransform = world.getComponent(
      manager.player,
      Transform2D.class
    );

    double pLeft = playerTransform.x - PLAYER_W / 2;
    double pRight = playerTransform.x + PLAYER_W / 2;
    double pTop = playerTransform.y - PLAYER_H / 2;
    double pBottom = playerTransform.y + PLAYER_H / 2;

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
          if (!roomCleared) {
            manager.lockedExitHintTimer = 1.5;
            return;
          }

          if (exit.triggersVictory) {
            manager.victoryPending = true;
            return;
          }

          if (exit.target != null) {
            manager.transition(exit.target, world);
          }
        }
      });
  }
}
