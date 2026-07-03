package knight_guy.rooms;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import javafx.scene.paint.Color;
import knight_guy.Consts;
import knight_guy.Utils;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.components.Transform2D;
import knight_guy.game_engine_internals.rendering.StaticSprite;

public final class EnemySpawner implements Consts {

  // platform information
  public record PlatformData(double x, double y, double width) {}

  // spawn enemies for the current room
  public static void spawn(
    World world,
    RoomManager manager,
    List<PlatformData> platforms
  ) {
    Collections.shuffle(platforms);

    // three enemies on random platforms
    for (int i = 0; i < Math.min(3, platforms.size()); i++) {
      PlatformData p = platforms.get(i);

      manager.addEntity(
        world.spawn(
          new StaticSprite(Utils.rect(40, 60, Color.RED)),
          new Transform2D(p.x() + p.width() / 2 - 20, p.y() - 60)
        )
      );
    }

    // one enemy on the ground
    Random random = new Random();

    double x = 100 + random.nextInt(700);

    manager.addEntity(
      world.spawn(
        new StaticSprite(Utils.rect(40, 60, Color.RED)),
        new Transform2D(x, FLOOR_Y - 60)
      )
    );
  }
}
