package knight_guy.rooms;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import knight_guy.Consts;
import knight_guy.Exit;
import knight_guy.Platform;
import knight_guy.Utils;
import knight_guy.game_engine_internals.AssetStore;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.components.Transform2D;
import knight_guy.game_engine_internals.rendering.StaticSprite;

public class StartingRoom implements Room, Consts {

  @Override
  public void build(World world, RoomManager manager) {
    RoomRegistry registry = world.getResource(RoomRegistry.class);
    // find some assets
    Image floorImg = AssetStore.load("floor.png");
    if (floorImg == null) {
      floorImg = Utils.rect((int) SCREEN_WIDTH, 10, Color.GREEN);
    }
    Image platformImg = AssetStore.load("platform.png");
    if (platformImg == null) {
      platformImg = Utils.rect(64, 20, Color.BROWN);
    }
    Image exitImg = AssetStore.load("exit.png");
    if (exitImg == null) {
      exitImg = Utils.rect(32, 64, Color.LIME);
    }

    double FLOOR_Y = SCREEN_HEIGHT - 10;

    manager.addEntity(
      world.spawn(new StaticSprite(floorImg), new Transform2D(0, FLOOR_Y))
    );

    manager.addEntity(
      world.spawn(
        new StaticSprite(platformImg, 200, 20),
        new Transform2D(100, 450),
        new Platform()
      )
    );
    manager.addEntity(
      world.spawn(
        new StaticSprite(platformImg, 180, 20),
        new Transform2D(380, 340),
        new Platform()
      )
    );
    manager.addEntity(
      world.spawn(
        new StaticSprite(platformImg, 140, 20),
        new Transform2D(150, 240),
        new Platform()
      )
    );
    manager.addEntity(
      world.spawn(
        new StaticSprite(platformImg, 120, 20),
        new Transform2D(520, 200),
        new Platform()
      )
    );
    manager.addEntity(
      world.spawn(
        new StaticSprite(exitImg, 32, 92),
        new Transform2D(SCREEN_WIDTH - 32 * 4, FLOOR_Y - 92),
        new Exit(registry.random())
      )
    );
  }
}
