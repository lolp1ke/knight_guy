package knight_guy.game_engine_internals;

public enum ScheduleStage {
  // first system to call
  // something like load saves, init level and etc.
  STARTUP,

  // if something has to be done before game logic loop
  // something like chunk generation and etc.
  PRE_UPDATE,
  // game logic loop
  // update rate is fixed 1/32 Hz
  UPDATE,
  // basically for input reset only rn
  POST_UPDATE,

  // render phase
  RENDER,

  // stuff to do when closing the game
  // something like save logic and etc.
  SHUTDOWN,
}
