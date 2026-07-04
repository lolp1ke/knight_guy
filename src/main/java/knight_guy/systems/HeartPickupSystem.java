package knight_guy.systems;

import java.util.ArrayList;
import java.util.List;
import knight_guy.Consts;
import knight_guy.HeartPickup;
import knight_guy.PlayerState;
import knight_guy.game_engine_internals.Entity;
import knight_guy.game_engine_internals.State;
import knight_guy.game_engine_internals.System;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.components.Transform2D;
import knight_guy.game_engine_internals.rendering.StaticSprite;
import knight_guy.rooms.RoomManager;
import knight_guy.states.GameState;

public final class HeartPickupSystem implements System, Consts {

  @Override
  public void run(World world) {
    State<GameState> state = world.getResource(State.class);
    if (state == null || state.getState() != GameState.Running) {
      return;
    }

    RoomManager manager = world.getResource(RoomManager.class);
    PlayerState playerState = world.getResource(PlayerState.class);

    if (
      manager == null ||
      playerState == null ||
      manager.player == null ||
      !world.isAlive(manager.player)
    ) {
      return;
    }

    Transform2D playerTransform = world.getComponent(
      manager.player,
      Transform2D.class
    );
    if (playerTransform == null) {
      return;
    }

    double pLeft = playerTransform.x - PLAYER_W / 2;
    double pRight = playerTransform.x + PLAYER_W / 2;
    double pTop = playerTransform.y - PLAYER_H / 2;
    double pBottom = playerTransform.y + PLAYER_H / 2;

    // collect matches first, then despawn - mutating the world mid-query
    // is asking for trouble
    List<Entity> collected = new ArrayList<>();

    world
      .query(Transform2D.class, StaticSprite.class, HeartPickup.class)
      .forEach((entity, components) -> {
        Transform2D heartTransform = (Transform2D) components[0];
        StaticSprite heartSprite = (StaticSprite) components[1];
        HeartPickup heart = (HeartPickup) components[2];

        double hLeft = heartTransform.x;
        double hRight = heartTransform.x + heartSprite.width;
        double hTop = heartTransform.y;
        double hBottom = heartTransform.y + heartSprite.height;

        if (
          pRight > hLeft && pLeft < hRight && pBottom > hTop && pTop < hBottom
        ) {
          playerState.heal(heart.healAmount);
          collected.add(entity);
        }
      });

    for (Entity entity : collected) {
      world.despawn(entity);
    }
  }
}
