package knight_guy;

import knight_guy.game_engine_internals.Component;
import knight_guy.rooms.Room;

public final class Exit implements Component {

  public Room target;

  public Exit(Room target) {
    this.target = target;
  }
}
