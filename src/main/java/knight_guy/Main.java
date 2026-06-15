package knight_guy;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import knight_guy.States.GameState;
import knight_guy.States.MenuState;
import knight_guy.game_engine_internals.AssetStore;
import knight_guy.game_engine_internals.Engine;
import knight_guy.game_engine_internals.Entity;
import knight_guy.game_engine_internals.Input;
import knight_guy.game_engine_internals.ScheduleStage;
import knight_guy.game_engine_internals.State;
import knight_guy.game_engine_internals.components.Transform2D;
import knight_guy.game_engine_internals.components.Velocity2D;
import knight_guy.game_engine_internals.plugins.InputPlugin;
import knight_guy.game_engine_internals.plugins.RenderPlugin;
import knight_guy.game_engine_internals.rendering.MainCanvas;
import knight_guy.game_engine_internals.rendering.Sprite;
import knight_guy.game_engine_internals.resources.Time;

public class Main extends Application implements Consts {

  public static void main() {
    Application.launch();
  }

  @Override
  public void start(Stage stage) throws Exception {
    // THIS IS JUST A DEMO NOT A GAME ITSELF
    // PROOF OF CONCEPT OF THE GAME ENGINE NO MORE NO LESS

    stage.setTitle("The Knight Guy");
    stage.setResizable(false);

    Engine engine = new Engine();
    engine.add_plugin(new RenderPlugin(SCREEN_WIDTH, SCREEN_HEIGHT));

    MainCanvas main_canvas = engine.get_world().get_resource(MainCanvas.class);
    Scene scene = new Scene(new StackPane(), SCREEN_WIDTH, SCREEN_HEIGHT);
    engine.add_plugin(new InputPlugin(scene));

    Button play_btn = new Button("play");
    play_btn.setOnAction(_ -> {
      engine.set_state(GameState.Running);
    });

    Button exit_btn = new Button("exit");
    exit_btn.setOnAction(_ -> {
      Platform.exit();
    });

    VBox menu = new VBox(24, play_btn, exit_btn);
    menu.setAlignment(Pos.CENTER);

    StackPane root = new StackPane(main_canvas.canvas, menu);
    scene.setRoot(root);
    stage.setScene(scene);

    engine.init_state(MenuState.Menu);

    engine.on_enter(MenuState.Menu, _ -> {
      menu.setVisible(true);
    });
    engine.on_exit(MenuState.Menu, _ -> {
      menu.setVisible(false);
    });

    final double FLOOR_HEIGHT = 10.0;
    final double FLOOR_Y = SCREEN_HEIGHT - FLOOR_HEIGHT;

    engine.on_enter(GameState.Running, world -> {
      world.add_resource(new PlayerState());

      Image floor_img = AssetStore.load("floor.png");
      if (floor_img == null) {
        floor_img = rect((int) SCREEN_WIDTH, (int) FLOOR_HEIGHT, Color.GREEN);
      }

      Image player_img = AssetStore.load("1player.png", PLAYER_W, PLAYER_H);
      if (player_img == null) {
        player_img = rect((int) PLAYER_W, (int) PLAYER_H, Color.CORNFLOWERBLUE);
      }

      Entity floor = world.spawn(
        new Sprite(floor_img),
        new Transform2D(0, FLOOR_Y)
      );
      Entity player = world.spawn(
        new Sprite(player_img),
        new Transform2D(200, SCREEN_HEIGHT - FLOOR_HEIGHT - PLAYER_H),
        new Velocity2D(),
        new Player()
      );

      engine.on_exit(GameState.Running, _ -> {
        world.despawn(floor, player);
      });
    });

    engine.add_system(ScheduleStage.UPDATE, world -> {
      State<GameState> state = world.get_resource(State.class);
      if (state.get_state() != GameState.Running) {
        return;
      }

      world
        .query(Transform2D.class, Velocity2D.class)
        .with(Player.class)
        .for_each((_, components) -> {
          Transform2D t = (Transform2D) components[0];
          Velocity2D v = (Velocity2D) components[1];
          Time time = world.get_resource(Time.class);
          Input input = world.get_resource(Input.class);

          PlayerState ps = world.get_resource(PlayerState.class);
          ps.dash_cooldown -= time.delta;
          ps.dash_timer -= time.delta;

          v.y += GRAVITY * time.delta;

          boolean dash_pressed = input.just_pressed(KeyCode.K);
          boolean left =
            input.pressed(KeyCode.A) || input.pressed(KeyCode.LEFT);
          boolean right =
            input.pressed(KeyCode.D) || input.pressed(KeyCode.RIGHT);
          boolean jump =
            input.pressed(KeyCode.W) ||
            input.pressed(KeyCode.UP) ||
            input.pressed(KeyCode.SPACE);

          if (left) {
            v.x += -SPEED;
            ps.facing_right = false;
          } else if (right) {
            v.x += SPEED;
            ps.facing_right = true;
          }

          if (jump && t.y + PLAYER_H >= FLOOR_Y) {
            v.y = JUMP_VEL;
          }

          if (dash_pressed && ps.dash_cooldown <= 0 && ps.dash_timer <= 0) {
            ps.dash_timer = DASH_DURATION;
            ps.dash_cooldown = DASH_COOLDOWN;
            v.x += ps.facing_right ? DASH_SPEED : -DASH_SPEED;
            v.y = 0;
          }

          t.x += v.x * time.delta;
          t.y += v.y * time.delta;
          v.x = 0;

          double floor_top = FLOOR_Y;
          if (t.y + PLAYER_H >= floor_top) {
            t.y = SCREEN_HEIGHT - FLOOR_HEIGHT - PLAYER_H;
            v.y = 0;
          }

          if (t.x < 0) {
            t.x = 0;
          }
          if (t.x + PLAYER_W > SCREEN_WIDTH) {
            t.x = SCREEN_WIDTH - PLAYER_W;
          }
        });
    });

    stage.show();
    engine.start();
  }

  private static Image rect(int w, int h, Color color) {
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
