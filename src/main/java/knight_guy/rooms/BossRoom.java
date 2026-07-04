package knight_guy.rooms;

import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import knight_guy.BossFactory;
import knight_guy.Consts;
import knight_guy.Exit;
import knight_guy.HeartPickup;
import knight_guy.SolidPlatform;
import knight_guy.Utils;
import knight_guy.game_engine_internals.AssetStore;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.components.Transform2D;
import knight_guy.game_engine_internals.rendering.StaticSprite;
import knight_guy.rooms.EnemySpawner.PlatformData;

// Hard-mode only: the final room of the loop, reached from the Sky Room's
// exit instead of looping back to the Dungeon. One boss, reusing platform
// art from all three regular rooms. Beating the boss unlocks the exit,
// which leads straight to the ending (see triggersVictory below) -
// exactly the same "can't leave until every Enemy is dead" rule that
// gates every other room's exit already covers the boss for free.
public class BossRoom implements Room, Consts {

  @Override
  public void build(World world, RoomManager manager) {
    // load assets
    Image background = AssetStore.load("backgrounds/boss.png");

    Image rockImg = AssetStore.load("platforms/rock.png");
    if (rockImg == null) {
      rockImg = Utils.rect(64, 20, Color.BROWN);
    }

    Image grassImg = AssetStore.load("platforms/grass.png");
    if (grassImg == null) {
      grassImg = Utils.rect(64, 20, Color.GREEN);
    }

    Image cloudImg = AssetStore.load("platforms/cloud.png");
    if (cloudImg == null) {
      cloudImg = Utils.rect(64, 20, Color.LIGHTBLUE);
    }

    Image exitImg = AssetStore.load("exit.png");
    if (exitImg == null) {
      exitImg = Utils.portal(32, 92);
    }

    final double FLOOR_Y = SCREEN_HEIGHT - 10;

    // spawn background
    if (background != null) {
      StaticSprite bg = new StaticSprite(background, LEVEL_WIDTH, LEVEL_HEIGHT);
      bg.zIndex = -100;
      manager.addEntity(world.spawn(bg, new Transform2D(0, 0)));
    }

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

    // arena platforms, mixing art from all three regular rooms
    List<PlatformData> platforms = PlatformLayouts.bossArena(
      world,
      manager,
      rockImg,
      grassImg,
      cloudImg
    );

    // a heart above each platform - a reason to actually use the
    // platforms mid-fight instead of just circling on the ground
    Image heartImg = Utils.heart(28, 28);
    for (PlatformData platform : platforms) {
      double heartX = platform.x() + platform.width() / 2.0 - 14;
      double heartY = platform.y() - 40;

      manager.addEntity(
        world.spawn(
          new StaticSprite(heartImg, 28, 28),
          new Transform2D(heartX, heartY),
          new HeartPickup(1)
        )
      );
    }

    // the one boss - no regular mobs in this room
    manager.addEntity(
      BossFactory.create(
        world,
        LEVEL_WIDTH / 2.0,
        FLOOR_Y - BossFactory.BOSS_H / 2.0
      )
    );

    // this is the true end of the run - beating the boss (which unlocks
    // this exit via the normal "no enemies left" rule) leads straight to
    // the victory/credits screen, same as Easy's Sky Room exit does
    manager.addEntity(
      world.spawn(
        new StaticSprite(exitImg, 32, 92),
        new Transform2D(LEVEL_WIDTH - 80, FLOOR_Y - 92),
        new Exit(null, true)
      )
    );
  }
}
