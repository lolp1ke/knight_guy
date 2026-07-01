package knight_guy.game_engine_internals;

import java.util.HashSet;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class Input implements Resource {

  private final HashSet<KeyCode> pressed = new HashSet<>();
  private final HashSet<KeyCode> justPressed = new HashSet<>();
  private final HashSet<KeyCode> justReleased = new HashSet<>();

  private final HashSet<MouseButton> mousePressed = new HashSet<>();
  private final HashSet<MouseButton> mouseJustPressed = new HashSet<>();
  private final HashSet<MouseButton> mouseJustReleased = new HashSet<>();

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

    scene.setOnMouseClicked(e -> {
      MouseButton mbtn = e.getButton();
      if (this.mousePressed.remove(mbtn)) {
        this.mouseJustReleased.add(mbtn);
      }
      if (this.mousePressed.add(mbtn)) {
        this.mouseJustPressed.add(mbtn);
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

  public boolean mousePressed(MouseButton btn) {
    return this.mousePressed.contains(btn);
  }

  public boolean mouseJustPressed(MouseButton btn) {
    return this.mouseJustPressed.contains(btn);
  }

  public boolean mouseJustReleased(MouseButton btn) {
    return this.mouseJustReleased.contains(btn);
  }

  // do not use this directly
  // for internal use cases only
  public void clearForNewTick() {
    this.justPressed.clear();
    this.justReleased.clear();

    this.mouseJustPressed.clear();
    this.mouseJustReleased.clear();
  }
}
