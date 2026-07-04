package knight_guy.systems;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import knight_guy.Consts;
import knight_guy.Enemy;
import knight_guy.EnemyState;
import knight_guy.game_engine_internals.State;
import knight_guy.game_engine_internals.System;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.rendering.MainCanvas;
import knight_guy.states.GameState;

public final class BossHealthBarRenderSystem implements System, Consts {

  private static final double BAR_WIDTH = 360.0;
  private static final double BAR_HEIGHT = 16.0;
  private static final double UI_Y = 24.0;

  @Override
  public void run(World world) {
    State<GameState> state = world.getResource(State.class);
    if (state == null || state.getState() != GameState.Running) {
      return;
    }

    MainCanvas mainCanvas = world.getResource(MainCanvas.class);
    if (mainCanvas == null) {
      return;
    }

    // find the boss, if the current room has one alive
    EnemyState[] boss = { null };
    world
      .query(EnemyState.class)
      .with(Enemy.class)
      .forEach((_, components) -> {
        EnemyState enemyState = (EnemyState) components[0];
        if (enemyState.isBoss) {
          boss[0] = enemyState;
        }
      });

    if (boss[0] == null || boss[0].isDead()) {
      return;
    }

    double pct = boss[0].maxHp <= 0
      ? 0
      : Math.max(0.0, Math.min(1.0, (double) boss[0].hp / boss[0].maxHp));

    double x = SCREEN_WIDTH / 2.0 - BAR_WIDTH / 2.0;

    GraphicsContext gc = mainCanvas.canvas.getGraphicsContext2D();
    gc.save();

    gc.setTextAlign(TextAlignment.CENTER);
    gc.setFont(Font.font("Sans Serif", FontWeight.BOLD, 16));
    gc.setFill(Color.rgb(0, 0, 0, 0.7));
    gc.fillText("BOSS", SCREEN_WIDTH / 2.0 + 1, UI_Y - 5);
    gc.setFill(Color.web("#ffb0b0"));
    gc.fillText("BOSS", SCREEN_WIDTH / 2.0, UI_Y - 6);

    gc.setFill(Color.rgb(0, 0, 0, 0.55));
    gc.fillRoundRect(x - 2, UI_Y - 2, BAR_WIDTH + 4, BAR_HEIGHT + 4, 8, 8);

    gc.setFill(Color.rgb(40, 8, 8, 0.85));
    gc.fillRoundRect(x, UI_Y, BAR_WIDTH, BAR_HEIGHT, 6, 6);

    gc.setFill(Color.web("#e23b3b"));
    gc.fillRoundRect(x, UI_Y, BAR_WIDTH * pct, BAR_HEIGHT, 6, 6);

    gc.setStroke(Color.rgb(255, 255, 255, 0.5));
    gc.setLineWidth(1.2);
    gc.strokeRoundRect(x, UI_Y, BAR_WIDTH, BAR_HEIGHT, 6, 6);

    gc.restore();
  }
}
