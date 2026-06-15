package knight_guy.game_engine_internals.rendering;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import knight_guy.game_engine_internals.System;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.components.Transform2D;

// helper class to draw stuff
public final class RenderSystem implements System {

  private record DrawItem(Sprite sprite, Transform2D transform) {}

  @Override
  public void run(World world) {
    MainCanvas main_canvas = world.get_resource(MainCanvas.class);
    if (main_canvas == null) {
      return;
    }

    Camera2D camera = world.get_resource(Camera2D.class);
    GraphicsContext gc = main_canvas.canvas.getGraphicsContext2D();
    double w = main_canvas.canvas.getWidth();
    double h = main_canvas.canvas.getHeight();

    gc.clearRect(0, 0, w, h);
    if (camera != null) {
      // check api cuz works strange
      gc.scale(camera.zoom, camera.zoom);
    }

    List<DrawItem> items = new ArrayList<>();
    world.query(Sprite.class, Transform2D.class).for_each((_, components) -> {
      items.add(
        new DrawItem((Sprite) components[0], (Transform2D) components[1])
      );
    });
    items.sort(Comparator.comparingInt(item -> item.sprite.z_index));

    for (DrawItem item : items) {
      Sprite sprite = item.sprite;
      Transform2D transform = item.transform;
      if (sprite.image != null) {
        gc.drawImage(
          sprite.image,
          transform.x,
          transform.y,
          sprite.width * transform.scale_x,
          sprite.height * transform.scale_y
        );
      }
    }
  }
}
