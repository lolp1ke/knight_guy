package knight_guy.structures;

import knight_guy.game_engine_internals.World;

public interface Structure {
  void build(World world, RoomManager manager);
}
