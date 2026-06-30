package knight_guy;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import knight_guy.game_engine_internals.AssetStore;
import knight_guy.game_engine_internals.Engine;
import knight_guy.game_engine_internals.Entity;
import knight_guy.game_engine_internals.ScheduleStage;
import knight_guy.game_engine_internals.components.Transform2D;
import knight_guy.game_engine_internals.components.Velocity2D;
import knight_guy.game_engine_internals.plugins.InputPlugin;
import knight_guy.game_engine_internals.plugins.RenderPlugin;
import knight_guy.game_engine_internals.rendering.AnimatedSprite;
import knight_guy.game_engine_internals.rendering.MainCanvas;
import knight_guy.states.GameState;
import knight_guy.states.MenuState;
import knight_guy.structures.BossRoom;
import knight_guy.structures.EnemyRoom;
import knight_guy.structures.LootRoom;
import knight_guy.structures.RoomManager;
import knight_guy.structures.RoomRegistry;
import knight_guy.structures.StartingRoom;
import knight_guy.systems.CameraSystem;
import knight_guy.systems.PlayerMovementSystem;
import knight_guy.systems.RoomSystem;

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
      javafx.application.Platform.exit();
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

    final double PLAYER_SPRITE_FRAME_W = 128.0d;
    final double PLAYER_SPRITE_FRAME_H = 128.0d;

    // player init system
    engine.onEnter(GameState.Running, world -> {
      world.addResource(new PlayerState());

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
      // adjust frame count if player sprite changes
      playerSprite.addAnimation("idle", playerIdleImg, 4);
      playerSprite.addAnimation("run", playerRunImg, 7);
      playerSprite.addAnimation("jump", playerJumpImg, 6);
      playerSprite.setAnimation("idle");

      Entity player = world.spawn(
        playerSprite,
        new Transform2D(100, 300),
        new Velocity2D(),
        new Player()
      );

      world.addResource(
        new RoomRegistry()
          .add(new LootRoom())
          .add(new EnemyRoom())
          .add(new BossRoom())
      );

      RoomManager manager = new RoomManager();
      manager.player = player;
      world.addResource(manager);

      manager.transition(new StartingRoom(), world);

      engine.onExit(GameState.Running, _ -> {
        world.despawn(player);
      });
    });

    engine.addSystem(ScheduleStage.UPDATE, new PlayerMovementSystem());
    engine.addSystem(ScheduleStage.UPDATE, new RoomSystem());
    engine.addSystem(ScheduleStage.UPDATE, new CameraSystem());

    stage.show();
    engine.start();
  }
}
