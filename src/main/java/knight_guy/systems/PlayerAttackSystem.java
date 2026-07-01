package knight_guy.systems;

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import knight_guy.Consts;
import knight_guy.Player;
import knight_guy.PlayerState;
import knight_guy.game_engine_internals.Input;
import knight_guy.game_engine_internals.State;
import knight_guy.game_engine_internals.System;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.rendering.AnimatedSprite;
import knight_guy.game_engine_internals.resources.Time;
import knight_guy.states.GameState;

public final class PlayerAttackSystem implements System, Consts {

  @Override
  public void run(World world) {
    final State<GameState> state = world.getResource(State.class);
    if (state.getState() != GameState.Running) {
      return;
    }

    final Time time = world.getResource(Time.class);
    final Input input = world.getResource(Input.class);

    world
      .query(AnimatedSprite.class)
      .with(Player.class)
      .forEach((_, components) -> {
        AnimatedSprite playerAnimatedSprite = (AnimatedSprite) components[0];

        PlayerState playerState = world.getResource(PlayerState.class);
        playerState.attackCooldown -= time.delta;
        playerState.attackComboTimer -= time.delta;

        final boolean attacking =
          input.justPressed(KeyCode.ENTER) ||
          input.mouseJustPressed(MouseButton.PRIMARY);

        if (attacking && playerState.attackCooldown <= 0) {
          if (playerState.moving) {
            playerAnimatedSprite.setAnimation("running_attack");
            return;
          }

          if (playerState.attackComboTimer <= 0) {
            playerState.attackVariation = 1;
            playerState.attackComboTimer = 0;
          }
          playerState.attackCooldown =
            (double) playerAnimatedSprite.getCurrentFrameCount() *
            playerAnimatedSprite.frameDuration;
          playerState.attackComboTimer +=
            playerState.attackCooldown + ATTACK_COMBO_THRESHOLD;
          playerAnimatedSprite.setAnimation(
            "attack" + playerState.attackVariation
          );
          playerState.attackVariation = (playerState.attackVariation % 3) + 1;
        }
      });
  }
}
