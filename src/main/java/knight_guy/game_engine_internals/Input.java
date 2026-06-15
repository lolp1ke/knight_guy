package knight_guy.game_engine_internals;

import java.util.HashSet;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

public class Input implements Resource {

  private final HashSet<KeyCode> pressed = new HashSet<>();
  private final HashSet<KeyCode> just_pressed = new HashSet<>();
  private final HashSet<KeyCode> just_released = new HashSet<>();

  public Input(Scene scene) {
    scene.setOnKeyPressed(e -> {
      KeyCode k = e.getCode();
      if (this.pressed.add(k)) {
        this.just_pressed.add(k);
      }
    });
    scene.setOnKeyReleased(e -> {
      KeyCode k = e.getCode();
      if (this.pressed.remove(k)) {
        this.just_released.add(k);
      }
    });
  }

  public boolean pressed(KeyCode key) {
    return this.pressed.contains(key);
  }

  public boolean just_pressed(KeyCode key) {
    return this.just_pressed.contains(key);
  }

  public boolean just_released(KeyCode key) {
    return this.just_released.contains(key);
  }

  // do not use this directly
  // for internal use cases only
  public void clear_for_new_tick() {
    this.just_pressed.clear();
    this.just_released.clear();
  }
}
