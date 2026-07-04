package knight_guy.systems;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import knight_guy.Consts;
import knight_guy.game_engine_internals.State;
import knight_guy.game_engine_internals.System;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.rendering.MainCanvas;
import knight_guy.rooms.RoomManager;
import knight_guy.states.GameState;

// shows "Defeat all enemies to proceed!" briefly whenever the player
// bumps into a locked exit - without this the exit would just silently
// do nothing, which reads as a bug rather than a rule
public final class ExitLockHintRenderSystem implements System, Consts {

  @Override
  public void run(World world) {
    State<GameState> state = world.getResource(State.class);
    if (state == null || state.getState() != GameState.Running) {
      return;
    }

    RoomManager manager = world.getResource(RoomManager.class);
    if (manager == null || manager.lockedExitHintTimer <= 0) {
      return;
    }

    MainCanvas mainCanvas = world.getResource(MainCanvas.class);
    if (mainCanvas == null) {
      return;
    }

    GraphicsContext gc = mainCanvas.canvas.getGraphicsContext2D();

    // fade the last few frames out instead of a hard cut
    double alpha = Math.min(1.0, manager.lockedExitHintTimer / 0.4);

    String text = "Defeat all enemies to proceed!";
    double x = SCREEN_WIDTH / 2.0;
    double y = 90.0;

    gc.save();
    gc.setTextAlign(TextAlignment.CENTER);
    gc.setFont(Font.font("Sans Serif", FontWeight.BOLD, 22));

    gc.setStroke(Color.rgb(0, 0, 0, alpha * 0.8));
    gc.setLineWidth(4);
    gc.strokeText(text, x, y);

    gc.setFill(Color.rgb(255, 107, 107, alpha));
    gc.fillText(text, x, y);
    gc.restore();
  }
}
