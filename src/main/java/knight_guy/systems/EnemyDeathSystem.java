package knight_guy.systems;

import knight_guy.Consts;
import knight_guy.Enemy;
import knight_guy.EnemyState;
import knight_guy.game_engine_internals.State;
import knight_guy.game_engine_internals.System;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.rendering.AnimatedSprite;
import knight_guy.states.GameState;

public final class EnemyDeathSystem implements System, Consts {

  @Override
  public void run(World world) {
    State<GameState> state = world.getResource(State.class);

    if (state.getState() != GameState.Running) {
      return;
    }

    world
      .query(EnemyState.class, AnimatedSprite.class)
      .with(Enemy.class)
      .forEach((entity, components) -> {
        EnemyState enemyState = (EnemyState) components[0];

        AnimatedSprite enemySprite = (AnimatedSprite) components[1];

        // Skip if enemy is still alive
        if (!enemyState.isDead()) {
          return;
        }

        // Play death animation
        if (!enemyState.dying) {
          enemyState.dying = true;

          if (enemySprite != null) {
            enemySprite.finished = false;
            enemySprite.setAnimation("death");
          }

          return;
        }

        // Remove enemy after death animation finishes
        if (enemySprite != null && enemySprite.finished) {
          world.despawn(entity);
        }
      });
  }
}
