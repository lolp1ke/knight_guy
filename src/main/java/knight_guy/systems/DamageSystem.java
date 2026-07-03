package knight_guy.systems;

import javafx.scene.input.KeyCode;
import knight_guy.PlayerState;
import knight_guy.game_engine_internals.Input;
import knight_guy.game_engine_internals.State;
import knight_guy.game_engine_internals.System;
import knight_guy.game_engine_internals.World;
import knight_guy.states.GameState;

public final class DamageSystem implements System {

  @Override
  public void run(World world) {
    State<GameState> state = world.getResource(State.class);

    if (state == null || state.getState() != GameState.Running) {
      return;
    }

    Input input = world.getResource(Input.class);
    PlayerState playerState = world.getResource(PlayerState.class);

    if (input == null || playerState == null) {
      return;
    }

    // Temporary test damage
    if (input.justPressed(KeyCode.H)) {
      playerState.takeDamage(1);
    }

    // Temporary test healing
    if (input.justPressed(KeyCode.J)) {
      playerState.heal(1);
    }
  }
}
