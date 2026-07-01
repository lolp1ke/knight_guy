package knight_guy.structures;

import java.util.ArrayList;
import java.util.List;
import knight_guy.game_engine_internals.Entity;
import knight_guy.game_engine_internals.Resource;
import knight_guy.game_engine_internals.World;

public final class RoomManager implements Resource {

  public Room currentRoom;
  public Room lastRoom;
  private final List<Entity> roomEntities = new ArrayList<>();
  // reference to player
  public Entity player;

  public void addEntity(Entity e) {
    this.roomEntities.add(e);
  }

  public void transition(Room nextRoom, World world) {
    for (Entity e : this.roomEntities) {
      if (world.isAlive(e)) {
        world.despawn(e);
      }
    }
    this.roomEntities.clear();
    this.lastRoom = this.currentRoom;
    this.currentRoom = nextRoom;
    nextRoom.build(world, this);
  }
}
