package knight_guy.game_engine_internals;

import java.net.URL;
import java.util.HashMap;
import javafx.scene.image.Image;

public final class AssetStore {

  private static final HashMap<String, Image> assets = new HashMap<>();

  // doesn't supposed to be created
  // static access only
  private AssetStore() {}

  public static Image load(String path) {
    Image cached = assets.get(path);
    if (cached != null) {
      return cached;
    }

    URL url = AssetStore.class.getResource("/assets/" + path);
    if (url == null) {
      java.lang.System.err.println("asset not found: /assets/" + path);
      return null;
    }

    Image img = new Image(url.toExternalForm());
    assets.put(path, img);
    return img;
  }

  public static Image load(String path, double width, double height) {
    String cacheKey = path + "@" + width + "x" + height;
    Image cached = assets.get(cacheKey);
    if (cached != null) {
      return cached;
    }

    URL url = AssetStore.class.getResource("/assets/" + path);
    if (url == null) {
      java.lang.System.err.println("asset not found: /assets/" + path);
      return null;
    }

    Image img = new Image(url.toExternalForm(), width, height, false, true);
    assets.put(cacheKey, img);
    return img;
  }
}
