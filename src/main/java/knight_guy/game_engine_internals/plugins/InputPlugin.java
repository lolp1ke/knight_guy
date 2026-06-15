package knight_guy.game_engine_internals.plugins;

import javafx.scene.Scene;
import knight_guy.game_engine_internals.Engine;
import knight_guy.game_engine_internals.Input;
import knight_guy.game_engine_internals.Plugin;
import knight_guy.game_engine_internals.ScheduleStage;
import knight_guy.game_engine_internals.World;

// simple input handler plugin
public final class InputPlugin implements Plugin {

  private final Scene scene;

  public InputPlugin(Scene scene) {
    this.scene = scene;
  }

  @Override
  public void build(Engine engine, World world) {
    Input input = new Input(scene);
    world.add_resource(input);
    engine.add_system(ScheduleStage.POST_UPDATE, _ ->
      input.clear_for_new_tick()
    );
  }
}
