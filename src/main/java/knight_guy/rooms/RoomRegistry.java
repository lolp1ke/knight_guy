package knight_guy.rooms;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import knight_guy.game_engine_internals.Resource;

public final class RoomRegistry implements Resource {

  private final List<Room> factories = new ArrayList<>();

  // current level index
  private int currentLevel = 1;

  // set for one call whenever nextLevel() wraps back around to level 1,
  // i.e. the player just finished a full loop (Dungeon -> Ground -> Sky)
  private boolean loopJustCompleted = false;

  public RoomRegistry add(Room factory) {
    this.factories.add(factory);
    return this;
  }

  // returns a random room (can still be used elsewhere)
  public Room random() {
    return factories.get(
      RandomGenerator.getDefault().nextInt(this.factories.size())
    );
  }

  // returns the next level in order
  public Room nextLevel() {
    switch (currentLevel) {
      case 1:
        currentLevel++;
        return new GroundRoom();
      case 2:
        currentLevel++;
        return new SkyRoom();
      default:
        currentLevel = 1;
        loopJustCompleted = true;
        return new DungeonRoom();
    }
  }

  // returns whether the last nextLevel() call completed a full loop, and
  // resets the flag - meant to be checked immediately after nextLevel()
  public boolean consumeLoopCompleted() {
    boolean result = loopJustCompleted;
    loopJustCompleted = false;
    return result;
  }
}
