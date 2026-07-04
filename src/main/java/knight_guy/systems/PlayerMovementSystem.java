package knight_guy.systems;

import javafx.scene.input.KeyCode;
import knight_guy.Consts;
import knight_guy.Platform;
import knight_guy.Player;
import knight_guy.PlayerState;
import knight_guy.SolidPlatform;
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
    final State<GameState> state = world.getResource(State.class);
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
        AnimatedSprite playerSprite = world.getComponent(
          entity,
          AnimatedSprite.class
        );

        PlayerState playerState = world.getResource(PlayerState.class);

        // once dead, freeze in place and let the death-check system in
        // Main.java own the animation/state transition from here on
        if (playerState.isDead()) {
          playerVelocity.x = 0;
          playerVelocity.y = 0;
          return;
        }

        playerState.dashCooldown -= time.delta;
        playerState.dashTimer -= time.delta;
        playerState.platformDropTimer -= time.delta;
        playerState.jumpBufferTimer -= time.delta;

        playerVelocity.y += GRAVITY * time.delta;

        boolean dashPressed = input.justPressed(KeyCode.K);
        boolean left = input.pressed(KeyCode.A);
        boolean right = input.pressed(KeyCode.D);
        boolean jumpPressed =
          input.justPressed(KeyCode.W) || input.justPressed(KeyCode.SPACE);
        boolean drop = input.pressed(KeyCode.S);
        playerState.moving = left || right;

        if (jumpPressed) {
          playerState.jumpBufferTimer = JUMP_BUFFER_TIME;
        }

        // coyote time: capture "was on ground" before this frame's physics
        // resets it below, so jumping just after walking off a ledge still
        // works instead of feeling like it "didn't register"
        if (playerState.onGround) {
          playerState.coyoteTimer = COYOTE_TIME;
        } else {
          playerState.coyoteTimer -= time.delta;
        }

        if (drop && playerState.platformDropTimer <= 0) {
          playerState.platformDropTimer = time.delta;
        }

        boolean dashing = playerState.dashTimer > 0;

        if (dashPressed && playerState.dashCooldown <= 0 && !dashing) {
          playerState.dashTimer = DASH_DURATION;
          playerState.dashCooldown = DASH_COOLDOWN;
          dashing = true;
          playerVelocity.y = 0;
        }

        if (dashing) {
          // hold a steady dash speed for the whole dash duration instead of
          // a single frame's nudge (velocity used to get reset to 0 every
          // frame, so the dash only ever moved you one tick's worth)
          playerVelocity.x = playerState.facingRight
            ? DASH_SPEED
            : -DASH_SPEED;
        } else {
          double targetSpeed = 0;
          if (left) {
            targetSpeed = -SPEED;
            playerState.facingRight = false;
          } else if (right) {
            targetSpeed = SPEED;
            playerState.facingRight = true;
          }

          // smoothly accelerate/decelerate toward the target speed instead
          // of snapping straight to it - slightly less control in the air,
          // like most platformers
          double rate =
            (targetSpeed != 0 ? ACCELERATION : DECELERATION) *
            (playerState.onGround ? 1.0 : AIR_CONTROL);
          double maxStep = rate * time.delta;
          double diff = targetSpeed - playerVelocity.x;

          if (Math.abs(diff) <= maxStep) {
            playerVelocity.x = targetSpeed;
          } else {
            playerVelocity.x += Math.signum(diff) * maxStep;
          }
        }

        // keep the player inside the level
        if (playerTransform.x < PLAYER_W / 2) {
          playerTransform.x = PLAYER_W / 2;
          playerVelocity.x = Math.max(playerVelocity.x, 0);
        }
        if (playerTransform.x > LEVEL_WIDTH - PLAYER_W / 2) {
          playerTransform.x = LEVEL_WIDTH - PLAYER_W / 2;
          playerVelocity.x = Math.min(playerVelocity.x, 0);
        }
        playerTransform.x += playerVelocity.x * time.delta;
        playerTransform.y += playerVelocity.y * time.delta;

        if (playerState.onGround) {
          playerState.fallStartY = playerTransform.y;
        }
        playerState.onGround = false;

        world
          .query(Transform2D.class, StaticSprite.class)
          .with(SolidPlatform.class)
          .forEach((_, components_) -> {
            Transform2D solidTransform = (Transform2D) components_[0];
            StaticSprite solidSprite = (StaticSprite) components_[1];

            double playerLeft = playerTransform.x - PLAYER_W / 2;
            double playerRight = playerTransform.x + PLAYER_W / 2;
            double playerTop = playerTransform.y - PLAYER_H / 2;
            double playerBottom = playerTransform.y + PLAYER_H / 2;

            double platLeft = solidTransform.x;
            double platRight = solidTransform.x + solidSprite.width;
            double platTop = solidTransform.y;
            double platBottom = solidTransform.y + solidSprite.height;

            if (
              playerRight <= platLeft ||
              playerLeft >= platRight ||
              playerBottom <= platTop ||
              playerTop >= platBottom
            ) {
              return;
            }

            double overlapLeft = playerRight - platLeft;
            double overlapRight = platRight - playerLeft;
            double overlapTop = playerBottom - platTop;
            double overlapBottom = platBottom - playerTop;

            double minOverlapX = Math.min(overlapLeft, overlapRight);
            double minOverlapY = Math.min(overlapTop, overlapBottom);

            if (minOverlapX < minOverlapY) {
              if (overlapLeft < overlapRight) {
                playerTransform.x = platLeft - PLAYER_W / 2;
              } else {
                playerTransform.x = platRight + PLAYER_W / 2;
              }
              playerVelocity.x = 0;
            } else {
              if (overlapTop < overlapBottom) {
                playerTransform.y = platTop - PLAYER_H / 2;
                if (playerVelocity.y >= 0) {
                  if (!playerState.onGround) {
                    double fallDist =
                      playerState.fallStartY - playerTransform.y;
                    if (fallDist > 400) {
                      playerState.hp -= 1;
                    }
                  }
                  playerState.onGround = true;
                  playerVelocity.y = 0;
                }
              } else {
                playerTransform.y = platBottom + PLAYER_H / 2;
                if (playerVelocity.y < 0) {
                  playerVelocity.y = 0;
                }
              }
            }
          });

        // offset so sprite render actually represents hitbox
        playerSprite.offsetX =
          -PLAYER_W * (playerState.facingRight ? 0.5d : 1.5d);

        if (playerVelocity.y >= 0 && playerState.platformDropTimer <= 0) {
          world
            .query(Transform2D.class, StaticSprite.class)
            .with(Platform.class)
            .forEach((_, components_) -> {
              // var name is `components_` cuz java can not redeclare/shadow variables
              Transform2D platformTransform = (Transform2D) components_[0];
              StaticSprite platformSprite = (StaticSprite) components_[1];

              double platformTop = platformTransform.y;
              double playerBottom = playerTransform.y + PLAYER_H / 2;
              double playerPrevBottom =
                playerBottom - playerVelocity.y * time.delta;

              double playerRightEdge = playerTransform.x + PLAYER_W / 2 - 20.0d;
              double playerLeftEdge = playerTransform.x - PLAYER_W / 2 + 20.0d;
              double platformLeftEdge = platformTransform.x;
              double platformRightEdge = platformTransform.x + platformSprite.width;

              boolean horizOverlap =
                playerRightEdge > platformLeftEdge &&
                playerLeftEdge < platformRightEdge;

              if (!horizOverlap) {
                return;
              }

              boolean crossingTop =
                playerPrevBottom <= platformTop + platformSprite.height / 2.0 &&
                playerBottom >= platformTop;

              if (crossingTop) {
                if (!playerState.onGround) {
                  double fallDist = playerState.fallStartY - playerTransform.y;
                  if (fallDist > 400) {
                    playerState.hp -= 1;
                  }
                }
                playerState.onGround = true;
                playerTransform.y = platformTop - PLAYER_H / 2;
                playerVelocity.y = 0;
              }
            });
        }

        if (playerState.jumpBufferTimer > 0 && playerState.coyoteTimer > 0) {
          playerVelocity.y = JUMP_VEL;
          playerState.jumpBufferTimer = 0;
          playerState.coyoteTimer = 0;
          playerState.onGround = false;
        }

        playerTransform.scaleX = playerState.facingRight
          ? Math.abs(playerTransform.scaleX)
          : Math.abs(playerTransform.scaleX) * -1d;

        AnimatedSprite anim = world.getComponent(entity, AnimatedSprite.class);
        if (anim != null && (anim.finished || anim.isLooping())) {
          if (anim.finished) {
            anim.finished = false;
          }
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
