package knight_guy.rooms;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;
import knight_guy.Platform;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.components.Transform2D;
import knight_guy.game_engine_internals.rendering.StaticSprite;
import knight_guy.rooms.EnemySpawner.PlatformData;

public class PlatformLayouts {

  // platform layout 1
  public static void layout1(World world, RoomManager manager, Image img) {
    List<EnemySpawner.PlatformData> platforms = new ArrayList<>();

    platforms.add(add(world, manager, img, 100, 440, 220, 40));
    platforms.add(add(world, manager, img, 420, 340, 180, 40));
    platforms.add(add(world, manager, img, 180, 230, 160, 40));
    platforms.add(add(world, manager, img, 560, 170, 180, 40));
    platforms.add(add(world, manager, img, 760, 260, 180, 40));
    platforms.add(add(world, manager, img, 980, 170, 160, 40));

    EnemySpawner.spawn(world, manager, platforms);
  }

  // platform layout 2
  public static void layout2(World world, RoomManager manager, Image img) {
    List<EnemySpawner.PlatformData> platforms = new ArrayList<>();

    platforms.add(add(world, manager, img, 60, 440, 180, 40));
    platforms.add(add(world, manager, img, 300, 480, 220, 40));
    platforms.add(add(world, manager, img, 250, 300, 180, 40));
    platforms.add(add(world, manager, img, 560, 210, 170, 40));
    platforms.add(add(world, manager, img, 760, 360, 180, 40));
    platforms.add(add(world, manager, img, 960, 240, 180, 40));

    EnemySpawner.spawn(world, manager, platforms);
  }

  // platform layout 3
  public static void layout3(World world, RoomManager manager, Image img) {
    List<EnemySpawner.PlatformData> platforms = new ArrayList<>();

    platforms.add(add(world, manager, img, 90, 480, 170, 40));
    platforms.add(add(world, manager, img, 330, 390, 180, 40));
    platforms.add(add(world, manager, img, 580, 300, 180, 40));
    platforms.add(add(world, manager, img, 330, 170, 240, 40));
    platforms.add(add(world, manager, img, 760, 400, 180, 40));
    platforms.add(add(world, manager, img, 980, 300, 180, 40));

    EnemySpawner.spawn(world, manager, platforms);
  }

  // creates a platform
  private static PlatformData add(
    World world,
    RoomManager manager,
    Image img,
    double x,
    double y,
    double width,
    double height
  ) {
    StaticSprite sprite = new StaticSprite(img, width, height);

    // draw platforms above the background
    sprite.zIndex = 10;

    manager.addEntity(
      world.spawn(sprite, new Transform2D(x, y), new Platform())
    );

    return new PlatformData(x, y, width);
  }
}
