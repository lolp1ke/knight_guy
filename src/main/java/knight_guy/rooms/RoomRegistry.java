package knight_guy.rooms;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import knight_guy.game_engine_internals.Resource;

public final class RoomRegistry implements Resource {

  private final List<Room> factories = new ArrayList<>();

  public RoomRegistry add(Room factory) {
    this.factories.add(factory);
    return this;
  }

  public Room random() {
    return factories.get(
      RandomGenerator.getDefault().nextInt(this.factories.size())
    );
  }
}
