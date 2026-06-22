package knight_guy;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

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
}
