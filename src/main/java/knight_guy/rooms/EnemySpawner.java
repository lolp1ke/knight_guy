package knight_guy.rooms;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import knight_guy.Consts;
import knight_guy.DifficultySettings;
import knight_guy.EnemyFactory;
import knight_guy.game_engine_internals.World;

public final class EnemySpawner implements Consts {

  // platform information
  public record PlatformData(double x, double y, double width) {}

  // spawn enemies for the current room
  public static void spawn(
    World world,
    RoomManager manager,
    List<PlatformData> platforms
  ) {
    DifficultySettings difficulty = world.getResource(DifficultySettings.class);
    // number of platform enemies scales with difficulty instead of always
    // being fixed at three
    int platformEnemyCount =
      difficulty != null ? difficulty.platformEnemyCount : 3;

    Collections.shuffle(platforms);

    for (int i = 0; i < Math.min(platformEnemyCount, platforms.size()); i++) {
      PlatformData p = platforms.get(i);

      manager.addEntity(
        EnemyFactory.create(
          world,
          p.x() + p.width() / 2,
          p.y() - ENEMY_H / 2,
          difficulty
        )
      );
    }

    // one enemy on the ground, spawned near the middle of the level instead
    // of a range that was biased toward the left side
    Random random = new Random();

    double x = LEVEL_WIDTH / 2.0 + (random.nextDouble() - 0.5) * 200.0;

    manager.addEntity(
      EnemyFactory.create(world, x, FLOOR_Y - ENEMY_H / 2, difficulty)
    );
  }
}
