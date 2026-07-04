package knight_guy.systems;

import knight_guy.Consts;
import knight_guy.Enemy;
import knight_guy.EnemyState;
import knight_guy.PlayerState;
import knight_guy.game_engine_internals.State;
import knight_guy.game_engine_internals.System;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.components.Transform2D;
import knight_guy.game_engine_internals.components.Velocity2D;
import knight_guy.game_engine_internals.rendering.AnimatedSprite;
import knight_guy.game_engine_internals.resources.Time;
import knight_guy.rooms.RoomManager;
import knight_guy.states.GameState;

public final class EnemyAttackSystem implements System, Consts {

  @Override
  public void run(World world) {
    State<GameState> state = world.getResource(State.class);

    if (state.getState() != GameState.Running) {
      return;
    }

    RoomManager manager = world.getResource(RoomManager.class);

    Time time = world.getResource(Time.class);

    PlayerState playerState = world.getResource(PlayerState.class);

    if (manager == null) {
      return;
    }

    Transform2D playerTransform = world.getComponent(
      manager.player,
      Transform2D.class
    );

    if (playerTransform == null) {
      return;
    }

    world
      .query(Transform2D.class, Velocity2D.class, EnemyState.class)
      .with(Enemy.class)
      .forEach((entity, components) -> {
        Transform2D enemyTransform = (Transform2D) components[0];

        Velocity2D enemyVelocity = (Velocity2D) components[1];

        EnemyState enemyState = (EnemyState) components[2];

        AnimatedSprite enemySprite = world.getComponent(
          entity,
          AnimatedSprite.class
        );

        double dx = Math.abs(playerTransform.x - enemyTransform.x);
        double dy = Math.abs(playerTransform.y - enemyTransform.y);

        // dead/dying enemies are handled entirely by EnemyDeathSystem -
        // leave them alone so it can play the death animation uninterrupted
        if (enemyState.isDead()) {
          enemyVelocity.x = 0;
          return;
        }

        // Face the player when they are behind the enemy
        if (playerTransform.x < enemyTransform.x) {
          enemyState.facingRight = false;
        } else if (playerTransform.x > enemyTransform.x) {
          enemyState.facingRight = true;
        }

        enemyTransform.scaleX = enemyState.facingRight
          ? Math.abs(enemyTransform.scaleX) * -1d
          : Math.abs(enemyTransform.scaleX);

        enemySprite.offsetX = -ENEMY_W * (enemyState.facingRight ? 0.5d : 2d);

        // Update timers
        if (enemyState.attackCooldown > 0) {
          enemyState.attackCooldown -= time.delta;
        }

        if (enemyState.attacking) {
          enemyState.attackTimer -= time.delta;

          enemyVelocity.x = 0;

          // Deal damage once near the end of the attack, but only if the
          // player is still actually in range when the blade swings -
          // previously this fired unconditionally just because the player
          // was in range back when the attack *started*, so stepping back
          // after the wind-up still cost you HP. Now backing off in time
          // makes the swing whiff.
          if (!enemyState.attackHit && enemyState.attackTimer <= 0.15) {
            enemyState.attackHit = true;

            if (
              dx <= enemyState.attackRange &&
              dy <= ENEMY_ATTACK_VERTICAL_TOLERANCE
            ) {
              playerState.takeDamage(enemyState.attackDamage);
            }
          }

          if (enemyState.attackTimer <= 0) {
            enemyState.attacking = false;

            if (enemySprite != null) {
              enemySprite.setAnimation("idle");
            }
          }

          return;
        }

        if (
          dx <= enemyState.attackRange &&
          dy <= ENEMY_ATTACK_VERTICAL_TOLERANCE &&
          enemyState.attackCooldown <= 0
        ) {
          enemyState.attacking = true;
          enemyState.attackHit = false;
          enemyState.attackTimer = enemyState.attackDuration;
          enemyState.attackCooldown = enemyState.attackCooldownMax;

          enemyVelocity.x = 0;

          if (enemySprite != null) {
            enemySprite.finished = false;
            enemySprite.setAnimation("attack");
          }
        }
      });
  }
}
