package knight_guy.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import knight_guy.game_engine_internals.Resource;

public final class RoomRegistry implements Resource {

  private final List<Structure> factories = new ArrayList<>();

  public RoomRegistry add(Structure factory) {
    this.factories.add(factory);
    return this;
  }

  public Structure random() {
    return factories.get(
      RandomGenerator.getDefault().nextInt(this.factories.size())
    );
  }
}
