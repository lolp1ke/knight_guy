package knight_guy.game_engine_internals.plugins;

import knight_guy.game_engine_internals.Engine;
import knight_guy.game_engine_internals.Plugin;
import knight_guy.game_engine_internals.ScheduleStage;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.rendering.Camera2D;
import knight_guy.game_engine_internals.rendering.MainCanvas;
import knight_guy.game_engine_internals.rendering.RenderSystem;

public final class RenderPlugin implements Plugin {

  private final double width;
  private final double height;

  public RenderPlugin(double width, double height) {
    this.width = width;
    this.height = height;
  }

  @Override
  public void build(Engine engine, World world) {
    world.add_resource(new MainCanvas(this.width, this.height));
    world.add_resource(new Camera2D());
    engine.add_system(ScheduleStage.RENDER, new RenderSystem());
  }
}
