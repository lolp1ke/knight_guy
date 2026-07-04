package knight_guy;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

public final class Utils {

  // simple rect creator
  public static Image rect(int w, int h, Color color) {
    WritableImage img = new WritableImage(w, h);
    PixelWriter pw = img.getPixelWriter();
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        pw.setColor(x, y, color);
      }
    }
    return img;
  }

  // a small glowing capsule used as the level exit whenever exit.png isn't
  // present - stands in for missing art without looking like a flat
  // placeholder rectangle
  public static Image portal(int w, int h) {
    Canvas canvas = new Canvas(w, h);
    GraphicsContext gc = canvas.getGraphicsContext2D();

    double arc = Math.min(w, h) * 0.7;

    LinearGradient glow = new LinearGradient(
      0,
      0,
      1,
      0,
      true,
      CycleMethod.NO_CYCLE,
      new Stop(0, Color.web("#0c1f3d")),
      new Stop(0.5, Color.web("#5fd9ff")),
      new Stop(1, Color.web("#0c1f3d"))
    );

    gc.setFill(glow);
    gc.fillRoundRect(0, 0, w, h, arc, arc);

    gc.setStroke(Color.web("#eafcff", 0.9));
    gc.setLineWidth(2.0);
    gc.strokeRoundRect(1, 1, w - 2, h - 2, arc, arc);

    WritableImage snapshot = new WritableImage(w, h);
    canvas.snapshot(new SnapshotParameters(), snapshot);
    return snapshot;
  }

  // a small red heart icon for HP pickups - drawn procedurally so no new
  // art file is needed
  public static Image heart(int w, int h) {
    Canvas canvas = new Canvas(w, h);
    GraphicsContext gc = canvas.getGraphicsContext2D();

    gc.setFill(Color.web("#ff5d7a"));
    gc.beginPath();
    gc.moveTo(w * 0.5, h * 0.88);
    gc.bezierCurveTo(w * -0.05, h * 0.55, w * 0.15, h * 0.02, w * 0.5, h * 0.3);
    gc.bezierCurveTo(w * 0.85, h * 0.02, w * 1.05, h * 0.55, w * 0.5, h * 0.88);
    gc.closePath();
    gc.fill();

    gc.setStroke(Color.web("#7a1030", 0.9));
    gc.setLineWidth(Math.max(1.0, w * 0.06));
    gc.stroke();

    WritableImage snapshot = new WritableImage(w, h);
    canvas.snapshot(new SnapshotParameters(), snapshot);
    return snapshot;
  }
}
