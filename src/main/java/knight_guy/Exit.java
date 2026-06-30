package knight_guy;

import knight_guy.game_engine_internals.Component;
import knight_guy.structures.Structure;

public final class Exit implements Component {

  public Structure target;

  public Exit(Structure target) {
    this.target = target;
  }
}
