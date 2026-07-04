package knight_guy;

import knight_guy.game_engine_internals.Component;
import knight_guy.rooms.Room;

public final class Exit implements Component {

  public Room target;

  // true only for the exit that closes out the last room of a loop
  // (currently the Sky Room's exit) - touching it shows the victory screen
  // instead of silently building the next room
  public boolean triggersVictory = false;

  public Exit(Room target) {
    this.target = target;
  }

  public Exit(Room target, boolean triggersVictory) {
    this.target = target;
    this.triggersVictory = triggersVictory;
  }
}
