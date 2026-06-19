package knight_guy.game_engine_internals.rendering;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import knight_guy.game_engine_internals.System;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.components.Transform2D;

// helper class to draw stuff
public final class RenderSystem implements System {

  private record DrawEntry(
    int zIndex,
    Image image,
    double srcX,
    double srcY,
    double srcWidth,
    double srcHeight,
    double dstX,
    double dstY,
    double dstWidth,
    double dstHeight
  ) {}

  @Override
  public void run(World world) {
    MainCanvas mainCanvas = world.getResource(MainCanvas.class);
    if (mainCanvas == null) {
      return;
    }

    Camera2D camera = world.getResource(Camera2D.class);
    GraphicsContext gc = mainCanvas.canvas.getGraphicsContext2D();
    double w = mainCanvas.canvas.getWidth();
    double h = mainCanvas.canvas.getHeight();

    gc.clearRect(0, 0, w, h);
    if (camera != null) {
      gc.scale(camera.zoom, camera.zoom);
    }

    List<DrawEntry> entries = new ArrayList<>();

    world
      .query(StaticSprite.class, Transform2D.class)
      .forEach((_, components) -> {
        StaticSprite sprite = (StaticSprite) components[0];
        Transform2D transform = (Transform2D) components[1];
        if (sprite.asset == null) {
          return;
        }
        double sw = sprite.asset.getWidth();
        double sh = sprite.asset.getHeight();
        double dw = sprite.width * transform.scaleX;
        double dh = sprite.height * transform.scaleY;
        entries.add(
          new DrawEntry(
            sprite.zIndex,
            sprite.asset,
            0,
            0,
            sw,
            sh,
            transform.x - Math.min(0, dw),
            transform.y - Math.min(0, dh),
            dw,
            dh
          )
        );
      });

    world
      .query(AnimatedSprite.class, Transform2D.class)
      .forEach((_, components) -> {
        AnimatedSprite anim = (AnimatedSprite) components[0];
        Transform2D transform = (Transform2D) components[1];
        Image image = anim.getCurrentImage();
        if (image == null) {
          return;
        }
        double dw = anim.width * transform.scaleX;
        double dh = anim.height * transform.scaleY;
        entries.add(
          new DrawEntry(
            anim.zIndex,
            image,
            anim.sourceX + anim.frame * anim.frameWidth,
            anim.sourceY,
            anim.width,
            anim.height,
            transform.x + anim.offsetX - Math.min(0, dw),
            transform.y + anim.offsetY - Math.min(0, dh),
            dw,
            dh
          )
        );
      });

    entries.sort(Comparator.comparingInt(key -> key.zIndex));

    for (DrawEntry entry : entries) {
      gc.drawImage(
        entry.image,
        entry.srcX,
        entry.srcY,
        entry.srcWidth,
        entry.srcHeight,
        entry.dstX,
        entry.dstY,
        entry.dstWidth,
        entry.dstHeight
      );
    }
  }
}
