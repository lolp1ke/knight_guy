package knight_guy.systems;

import javafx.scene.input.KeyCode;
import knight_guy.Consts;
import knight_guy.Platform;
import knight_guy.Player;
import knight_guy.PlayerState;
import knight_guy.game_engine_internals.Input;
import knight_guy.game_engine_internals.State;
import knight_guy.game_engine_internals.System;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.components.Transform2D;
import knight_guy.game_engine_internals.components.Velocity2D;
import knight_guy.game_engine_internals.rendering.AnimatedSprite;
import knight_guy.game_engine_internals.rendering.StaticSprite;
import knight_guy.game_engine_internals.resources.Time;
import knight_guy.states.GameState;

public final class PlayerMovementSystem implements System, Consts {

  @Override
  public void run(World world) {
    State<GameState> state = world.getResource(State.class);
    if (state.getState() != GameState.Running) {
      return;
    }

    Time time = world.getResource(Time.class);
    Input input = world.getResource(Input.class);

    world
      .query(Transform2D.class, Velocity2D.class)
      .with(Player.class)
      .forEach((entity, components) -> {
        Transform2D playerTransform = (Transform2D) components[0];
        Velocity2D playerVelocity = (Velocity2D) components[1];

        PlayerState playerState = world.getResource(PlayerState.class);
        playerState.dashCooldown -= time.delta;
        playerState.dashTimer -= time.delta;
        playerState.platformDropTimer -= time.delta;

        playerVelocity.y += GRAVITY * time.delta;

        boolean dashPressed = input.justPressed(KeyCode.K);
        boolean left = input.pressed(KeyCode.A) || input.pressed(KeyCode.LEFT);
        boolean right =
          input.pressed(KeyCode.D) || input.pressed(KeyCode.RIGHT);
        boolean jump =
          input.pressed(KeyCode.W) ||
          input.pressed(KeyCode.UP) ||
          input.pressed(KeyCode.SPACE);
        boolean drop = input.pressed(KeyCode.S) || input.pressed(KeyCode.DOWN);

        if (drop && playerState.platformDropTimer <= 0) {
          playerState.platformDropTimer = time.delta;
        }

        if (left) {
          playerVelocity.x += -SPEED;
          playerState.facingRight = false;
        } else if (right) {
          playerVelocity.x += SPEED;
          playerState.facingRight = true;
        }

        if (
          dashPressed &&
          playerState.dashCooldown <= 0 &&
          playerState.dashTimer <= 0
        ) {
          playerState.dashTimer = DASH_DURATION;
          playerState.dashCooldown = DASH_COOLDOWN;
          playerVelocity.x += playerState.facingRight
            ? DASH_SPEED
            : -DASH_SPEED;
          playerVelocity.y = 0;
        }

        playerTransform.x += playerVelocity.x * time.delta;
        playerTransform.y += playerVelocity.y * time.delta;
        playerVelocity.x = 0;

        playerState.onGround = false;

        if (playerTransform.y + PLAYER_H >= FLOOR_Y) {
          playerTransform.y = FLOOR_Y - PLAYER_H;
          playerVelocity.y = 0;
          playerState.onGround = true;
        }

        if (playerVelocity.y >= 0 && playerState.platformDropTimer <= 0) {
          world
            .query(Transform2D.class, StaticSprite.class)
            .with(Platform.class)
            .forEach((_, components_) -> {
              // var name is `components_` cuz java can not redeclare/shadow variables
              Transform2D platformTransform = (Transform2D) components_[0];
              StaticSprite platformSprite = (StaticSprite) components_[1];

              double platformTop = platformTransform.y;
              double playerBottom = playerTransform.y + PLAYER_H;
              double playerPrevBottom =
                playerBottom - playerVelocity.y * time.delta;

              boolean horizOverlap =
                playerTransform.x + PLAYER_W > platformTransform.x &&
                playerTransform.x < platformTransform.x + platformSprite.width;

              if (!horizOverlap) {
                return;
              }

              boolean crossingTop =
                playerPrevBottom <= platformTop + platformSprite.height / 2.0 &&
                playerBottom >= platformTop;

              if (crossingTop) {
                playerState.onGround = true;
                playerTransform.y = platformTop - PLAYER_H;
                playerVelocity.y = 0;
              }
            });
        }

        if (jump && playerState.onGround) {
          playerVelocity.y = JUMP_VEL;
        }

        playerTransform.scaleX = playerState.facingRight
          ? Math.abs(playerTransform.scaleX)
          : Math.abs(playerTransform.scaleX) * -1d;

        AnimatedSprite anim = world.getComponent(entity, AnimatedSprite.class);
        if (anim != null) {
          if (!playerState.onGround) {
            anim.setAnimation("jump");
          } else if (left || right) {
            anim.setAnimation("run");
          } else {
            anim.setAnimation("idle");
          }
        }
      });
  }
}
