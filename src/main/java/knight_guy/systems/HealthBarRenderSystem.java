package knight_guy.systems;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import knight_guy.PlayerState;
import knight_guy.game_engine_internals.AssetStore;
import knight_guy.game_engine_internals.State;
import knight_guy.game_engine_internals.System;
import knight_guy.game_engine_internals.World;
import knight_guy.game_engine_internals.rendering.MainCanvas;
import knight_guy.states.GameState;

public final class HealthBarRenderSystem implements System {

  private static final String HEALTH_BAR_PATH = "health/hp_bar.png";

  private static final double FRAME_WIDTH = 64.0;
  private static final double FRAME_HEIGHT = 16.0;

  private static final double UI_X = 20.0;
  private static final double UI_Y = 20.0;
  private static final double SCALE = 3.0;

  private static Image healthBarImage = AssetStore.load(HEALTH_BAR_PATH);

  @Override
  public void run(World world) {
    State<GameState> state = world.getResource(State.class);

    if (state == null || state.getState() != GameState.Running) {
      return;
    }

    MainCanvas mainCanvas = world.getResource(MainCanvas.class);
    PlayerState playerState = world.getResource(PlayerState.class);

    if (mainCanvas == null || playerState == null || healthBarImage == null) {
      return;
    }

    int hp = playerState.hp;

    if (hp < 0) {
      hp = 0;
    }

    if (hp > PlayerState.MAX_HP) {
      hp = PlayerState.MAX_HP;
    }

    /*
     * sprite sheet layout:
     *
     * row 0: 4 HP | 3 HP | 2 HP
     * row 1: 1 HP | 0 HP | empty
     */
    int frameIndex = PlayerState.MAX_HP - hp;

    int columns = (int) (healthBarImage.getWidth() / FRAME_WIDTH);
    int rows = (int) (healthBarImage.getHeight() / FRAME_HEIGHT);
    int totalFrames = columns * rows;

    if (frameIndex < 0) {
      frameIndex = 0;
    }

    if (frameIndex > 4) {
      frameIndex = 4;
    }

    if (frameIndex >= totalFrames) {
      frameIndex = totalFrames - 1;
    }

    double sourceX = (frameIndex % columns) * FRAME_WIDTH;
    double sourceY = (frameIndex / columns) * FRAME_HEIGHT;

    GraphicsContext gc = mainCanvas.canvas.getGraphicsContext2D();

    gc.drawImage(
      healthBarImage,
      sourceX,
      sourceY,
      FRAME_WIDTH,
      FRAME_HEIGHT,
      UI_X,
      UI_Y,
      FRAME_WIDTH * SCALE,
      FRAME_HEIGHT * SCALE
    );
  }
}
