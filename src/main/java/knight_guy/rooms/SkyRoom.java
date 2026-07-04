package knight_guy.rooms;

import java.util.Random;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import knight_guy.Consts;
import knight_guy.DifficultySettings;
import knight_guy.Exit;
import knight_guy.SolidPlatform;
import knight_guy.Utils;
import knight_guy.game_engine_internals.AssetStore;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.components.Transform2D;
import knight_guy.game_engine_internals.rendering.StaticSprite;

public class SkyRoom implements Room, Consts {

  @Override
  public void build(World world, RoomManager manager) {
    RoomRegistry registry = world.getResource(RoomRegistry.class);

    // load assets
    Image background = AssetStore.load("backgrounds/sky.png");

    Image floorImg = AssetStore.load("floor.png");
    if (floorImg == null) {
      floorImg = Utils.rect((int) SCREEN_WIDTH, 10, Color.GREEN);
    }

    Image platformImg = AssetStore.load("platforms/cloud.png");
    if (platformImg == null) {
      platformImg = Utils.rect(64, 20, Color.LIGHTBLUE);
    }

    Image exitImg = AssetStore.load("exit.png");
    if (exitImg == null) {
      exitImg = Utils.portal(32, 92);
    }

    final double FLOOR_Y = SCREEN_HEIGHT - 10;

    // spawn background
    StaticSprite bg = new StaticSprite(background, LEVEL_WIDTH, LEVEL_HEIGHT);
    bg.zIndex = -100;

    manager.addEntity(world.spawn(bg, new Transform2D(0, 0)));

    // spawn floor
    manager.addEntity(
      world.spawn(
        new StaticSprite(
          Utils.rect((int) LEVEL_WIDTH, 10, Color.TRANSPARENT),
          LEVEL_WIDTH,
          10
        ),
        new Transform2D(0, FLOOR_Y),
        new SolidPlatform()
      )
    );

    // Choose one random platform layout
    Random random = new Random();

    switch (random.nextInt(3)) {
      case 0:
        PlatformLayouts.layout1(world, manager, platformImg);
        break;
      case 1:
        PlatformLayouts.layout2(world, manager, platformImg);
        break;
      default:
        PlatformLayouts.layout3(world, manager, platformImg);
        break;
    }

    // Sky is the last room of the regular loop (Dungeon -> Ground -> Sky).
    // On Hard, that's the cue to send the player into the boss room
    // instead of wrapping back to Dungeon; Easy keeps the original
    // behavior of ending the run right here.
    DifficultySettings difficulty = world.getResource(DifficultySettings.class);
    Exit exit;

    if (difficulty != null && difficulty.bossFightEnabled) {
      exit = new Exit(new BossRoom(), false);
    } else {
      Room nextRoom = registry.nextLevel();
      boolean completesLoop = registry.consumeLoopCompleted();
      exit = new Exit(nextRoom, completesLoop);
    }

    // spawn exit
    manager.addEntity(
      world.spawn(
        new StaticSprite(exitImg, 32, 92),
        new Transform2D(LEVEL_WIDTH - 80, FLOOR_Y - 92),
        exit
      )
    );
  }
}
