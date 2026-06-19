package knight_guy.game_engine_internals;

import java.util.HashSet;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

public class Input implements Resource {

  private final HashSet<KeyCode> pressed = new HashSet<>();
  private final HashSet<KeyCode> justPressed = new HashSet<>();
  private final HashSet<KeyCode> justReleased = new HashSet<>();

  public Input(Scene scene) {
    scene.setOnKeyPressed(e -> {
      KeyCode k = e.getCode();
      if (this.pressed.add(k)) {
        this.justPressed.add(k);
      }
    });
    scene.setOnKeyReleased(e -> {
      KeyCode k = e.getCode();
      if (this.pressed.remove(k)) {
        this.justReleased.add(k);
      }
    });
  }

  public boolean pressed(KeyCode key) {
    return this.pressed.contains(key);
  }

  public boolean justPressed(KeyCode key) {
    return this.justPressed.contains(key);
  }

  public boolean justReleased(KeyCode key) {
    return this.justReleased.contains(key);
  }

  // do not use this directly
  // for internal use cases only
  public void clearForNewTick() {
    this.justPressed.clear();
    this.justReleased.clear();
  }
}
