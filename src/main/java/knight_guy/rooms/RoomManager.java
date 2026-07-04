package knight_guy.rooms;

import java.util.ArrayList;
import java.util.List;
import knight_guy.game_engine_internals.Entity;
import knight_guy.game_engine_internals.Resource;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.components.Transform2D;

public final class RoomManager implements Resource {

  public Room currentRoom;
  public Room lastRoom;
  private final List<Entity> roomEntities = new ArrayList<>();
  // reference to player
  public Entity player;

  // set by RoomSystem when the player reaches the loop-ending exit; a
  // system in Main.java watches this and switches the engine into the
  // Victory state
  public boolean victoryPending = false;

  // set (to a few seconds) by RoomSystem whenever the player touches an
  // exit while enemies are still alive in the room, so a render system can
  // show a brief "defeat all enemies" hint instead of the exit just doing
  // nothing with no explanation
  public double lockedExitHintTimer = 0.0;

  public void addEntity(Entity e) {
    this.roomEntities.add(e);
  }

  // despawns every entity tracked for the current room without building a
  // new one - used when the game session itself is ending, not just moving
  // between rooms
  public void clearRoom(World world) {
    for (Entity e : this.roomEntities) {
      if (world.isAlive(e)) {
        world.despawn(e);
      }
    }
    this.roomEntities.clear();
  }

  public void transition(Room nextRoom, World world) {
    clearRoom(world);
    this.lastRoom = this.currentRoom;
    this.currentRoom = nextRoom;
    nextRoom.build(world, this);

    // Move the player to the room spawn position
    Transform2D playerTransform = world.getComponent(player, Transform2D.class);

    if (playerTransform != null) {
      playerTransform.x = 100;
      playerTransform.y = 300;
    }
  }
}
