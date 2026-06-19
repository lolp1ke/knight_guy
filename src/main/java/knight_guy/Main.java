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
import knight_guy.game_engine_internals.rendering.AnimatedSprite;
import knight_guy.game_engine_internals.rendering.MainCanvas;
import knight_guy.game_engine_internals.rendering.StaticSprite;
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
    engine.addPlugin(new RenderPlugin(SCREEN_WIDTH, SCREEN_HEIGHT));

    MainCanvas mainCanvas = engine.getWorld().getResource(MainCanvas.class);
    Scene scene = new Scene(new StackPane(), SCREEN_WIDTH, SCREEN_HEIGHT);
    engine.addPlugin(new InputPlugin(scene));

    Button playBtn = new Button("play");
    playBtn.setOnAction(_ -> {
      engine.setState(GameState.Running);
    });

    Button exitBtn = new Button("exit");
    exitBtn.setOnAction(_ -> {
      Platform.exit();
    });

    VBox menu = new VBox(24, playBtn, exitBtn);
    menu.setAlignment(Pos.CENTER);

    StackPane root = new StackPane(mainCanvas.canvas, menu);
    scene.setRoot(root);
    stage.setScene(scene);

    engine.initState(MenuState.Menu);

    engine.onEnter(MenuState.Menu, _ -> {
      menu.setVisible(true);
    });
    engine.onExit(MenuState.Menu, _ -> {
      menu.setVisible(false);
    });

    final double FLOOR_HEIGHT = 10.0;
    final double FLOOR_Y = SCREEN_HEIGHT - FLOOR_HEIGHT;

    final double PLAYER_SPRITE_FRAME_W = 128.0d;
    final double PLAYER_SPRITE_FRAME_H = 128.0d;

    engine.onEnter(GameState.Running, world -> {
      world.addResource(new PlayerState());

      Image floorImg = AssetStore.load("floor.png");
      if (floorImg == null) {
        floorImg = rect((int) SCREEN_WIDTH, (int) FLOOR_HEIGHT, Color.GREEN);
      }

      Image playerIdleImg = AssetStore.load("player/idle.png");
      Image playerRunImg = AssetStore.load("player/run.png");
      Image playerJumpImg = AssetStore.load("player/jump.png");

      AnimatedSprite playerSprite = new AnimatedSprite(
        PLAYER_W,
        PLAYER_SPRITE_FRAME_H,
        0.15
      );
      playerSprite.frameWidth = PLAYER_SPRITE_FRAME_W;
      playerSprite.frameHeight = PLAYER_SPRITE_FRAME_H;
      playerSprite.sourceX = 0.0;
      playerSprite.sourceY = 0.0;
      playerSprite.offsetY = PLAYER_H - PLAYER_SPRITE_FRAME_H;
      playerSprite.addAnimation("idle", playerIdleImg, 4);
      playerSprite.addAnimation("run", playerRunImg, 7);
      playerSprite.addAnimation("jump", playerJumpImg, 6);
      playerSprite.setAnimation("idle");

      Entity floor = world.spawn(
        new StaticSprite(floorImg),
        new Transform2D(0, FLOOR_Y)
      );
      Entity player = world.spawn(
        playerSprite,
        new Transform2D(200, SCREEN_HEIGHT - FLOOR_HEIGHT - PLAYER_H),
        new Velocity2D(),
        new Player()
      );

      engine.onExit(GameState.Running, _ -> {
        world.despawn(floor, player);
      });
    });

    engine.addSystem(ScheduleStage.UPDATE, world -> {
      State<GameState> state = world.getResource(State.class);
      if (state.getState() != GameState.Running) {
        return;
      }
      Time time = world.getResource(Time.class);
      Input input = world.getResource(Input.class);

      world
        .query(Transform2D.class, Velocity2D.class)
        .with(Player.class)
        .forEach((entity, components) -> {
          Transform2D t = (Transform2D) components[0];
          Velocity2D v = (Velocity2D) components[1];

          PlayerState ps = world.getResource(PlayerState.class);
          ps.dashCooldown -= time.delta;
          ps.dashTimer -= time.delta;

          v.y += GRAVITY * time.delta;

          boolean dashPressed = input.justPressed(KeyCode.K);
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
            ps.facingRight = false;
          } else if (right) {
            v.x += SPEED;
            ps.facingRight = true;
          }

          if (jump && t.y + PLAYER_H >= FLOOR_Y) {
            v.y = JUMP_VEL;
          }

          if (dashPressed && ps.dashCooldown <= 0 && ps.dashTimer <= 0) {
            ps.dashTimer = DASH_DURATION;
            ps.dashCooldown = DASH_COOLDOWN;
            v.x += ps.facingRight ? DASH_SPEED : -DASH_SPEED;
            v.y = 0;
          }

          t.x += v.x * time.delta;
          t.y += v.y * time.delta;
          v.x = 0;

          boolean onGround = false;
          double floorTop = FLOOR_Y;
          if (t.y + PLAYER_H >= floorTop) {
            t.y = SCREEN_HEIGHT - FLOOR_HEIGHT - PLAYER_H;
            v.y = 0;
            onGround = true;
          }

          if (t.x < 0) {
            t.x = 0;
          }
          if (t.x + PLAYER_W > SCREEN_WIDTH) {
            t.x = SCREEN_WIDTH - PLAYER_W;
          }

          t.scaleX *= ps.facingRight ? 1.0 : -1.0;

          AnimatedSprite anim = world.getComponent(
            entity,
            AnimatedSprite.class
          );
          if (anim != null) {
            if (!onGround) {
              anim.setAnimation("jump");
            } else if (left || right) {
              anim.setAnimation("run");
            } else {
              anim.setAnimation("idle");
            }
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
