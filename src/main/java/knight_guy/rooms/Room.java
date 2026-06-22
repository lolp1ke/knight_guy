package knight_guy.rooms;

import knight_guy.game_engine_internals.World;

public interface Room {
  void build(World world, RoomManager manager);
}
