package knight_guy.states;

import knight_guy.game_engine_internals.States;

public enum GameState implements States {
  Running,
  Pause,
  GameOver,
  Victory,
}
