package knight_guy;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import knight_guy.game_engine_internals.AssetStore;
import knight_guy.game_engine_internals.Engine;
import knight_guy.game_engine_internals.Entity;
import knight_guy.game_engine_internals.ScheduleStage;
import knight_guy.game_engine_internals.State;
import knight_guy.game_engine_internals.components.Transform2D;
import knight_guy.game_engine_internals.components.Velocity2D;
import knight_guy.game_engine_internals.plugins.InputPlugin;
import knight_guy.game_engine_internals.plugins.RenderPlugin;
import knight_guy.game_engine_internals.rendering.AnimatedSprite;
import knight_guy.game_engine_internals.rendering.MainCanvas;
import knight_guy.rooms.DungeonRoom;
import knight_guy.rooms.RoomManager;
import knight_guy.rooms.RoomRegistry;
import knight_guy.states.GameState;
import knight_guy.states.MenuState;
import knight_guy.systems.BossHealthBarRenderSystem;
import knight_guy.systems.CameraSystem;
import knight_guy.systems.DamageSystem;
import knight_guy.systems.EnemyAttackSystem;
import knight_guy.systems.EnemyDeathSystem;
import knight_guy.systems.ExitLockHintRenderSystem;
import knight_guy.systems.HealthBarRenderSystem;
import knight_guy.systems.HeartPickupSystem;
import knight_guy.systems.PlayerAttackSystem;
import knight_guy.systems.PlayerMovementSystem;
import knight_guy.systems.RoomSystem;

public class Main extends Application implements Consts {

  public static void main() {
    Application.launch();
  }

  @Override
  public void start(Stage stage) throws Exception {
    stage.setTitle("The Knight Guy");
    stage.setResizable(false);

    Engine engine = new Engine();
    engine.addPlugin(new RenderPlugin(SCREEN_WIDTH, SCREEN_HEIGHT));

    MainCanvas mainCanvas = engine.getWorld().getResource(MainCanvas.class);
    Scene scene = new Scene(new StackPane(), SCREEN_WIDTH, SCREEN_HEIGHT);
    engine.addPlugin(new InputPlugin(scene));

    // remembers whichever difficulty was last chosen, so "Play Again" from
    // the victory screen doesn't need to ask again
    final DifficultySettings[] chosenDifficulty = new DifficultySettings[1];

    // sets the difficulty resource and starts (or restarts) a run
    Runnable[] startGame = new Runnable[1];
    startGame[0] = () -> {
      engine
        .getWorld()
        .addResource(
          chosenDifficulty[0] != null
            ? chosenDifficulty[0]
            : DifficultySettings.easy()
        );
      engine.setState(GameState.Running);
    };

    // --- shared UI styling -------------------------------------------------
    // a dark, slightly glowing look that matches the new portal art instead
    // of the plain default-white JavaFX background/buttons
    final String panelStyle =
      "-fx-background-color: rgba(9, 18, 32, 0.85);" +
      "-fx-background-radius: 18;" +
      "-fx-border-color: rgba(111, 216, 255, 0.55);" +
      "-fx-border-radius: 18;" +
      "-fx-border-width: 1.5;" +
      "-fx-padding: 36 52 36 52;";

    final String titleStyle =
      "-fx-font-size: 42px;" +
      "-fx-font-weight: bold;" +
      "-fx-text-fill: #eafcff;" +
      "-fx-effect: dropshadow(gaussian, rgba(95,217,255,0.55), 20, 0.35, 0, 0);";

    final String primaryButtonStyle =
      "-fx-font-size: 16px;" +
      "-fx-font-weight: bold;" +
      "-fx-text-fill: white;" +
      "-fx-background-color: linear-gradient(#3d6791, #1c3350);" +
      "-fx-background-radius: 10;" +
      "-fx-border-color: #6fd8ff;" +
      "-fx-border-radius: 10;" +
      "-fx-border-width: 1.4;" +
      "-fx-padding: 10 32 10 32;" +
      "-fx-cursor: hand;";

    final String secondaryButtonStyle =
      "-fx-font-size: 14px;" +
      "-fx-font-weight: bold;" +
      "-fx-text-fill: #d7d7d7;" +
      "-fx-background-color: linear-gradient(#4a4a4a, #262626);" +
      "-fx-background-radius: 10;" +
      "-fx-border-color: #7a7a7a;" +
      "-fx-border-radius: 10;" +
      "-fx-border-width: 1.2;" +
      "-fx-padding: 8 26 8 26;" +
      "-fx-cursor: hand;";

    Label titleLabel = new Label("The Knight Guy");
    titleLabel.setStyle(titleStyle);

    Button playEasyBtn = new Button("Play - Easy");
    playEasyBtn.setStyle(primaryButtonStyle);
    playEasyBtn.setOnAction(_ -> {
      chosenDifficulty[0] = DifficultySettings.easy();
      startGame[0].run();
    });

    Button playHardBtn = new Button("Play - Hard");
    playHardBtn.setStyle(primaryButtonStyle);
    playHardBtn.setOnAction(_ -> {
      chosenDifficulty[0] = DifficultySettings.hard();
      startGame[0].run();
    });

    Button exitBtn = new Button("Exit");
    exitBtn.setStyle(secondaryButtonStyle);
    exitBtn.setOnAction(_ -> {
      javafx.application.Platform.exit();
    });

    VBox menuPanel = new VBox(
      20,
      titleLabel,
      playEasyBtn,
      playHardBtn,
      exitBtn
    );
    menuPanel.setAlignment(Pos.CENTER);
    menuPanel.setStyle(panelStyle);

    StackPane menu = new StackPane(menuPanel);
    menu.setAlignment(Pos.CENTER);

    // game over screen
    Label gameOverLabel = new Label("\tYou lost\nSkill Issue Maybe?");
    gameOverLabel.setStyle(
      "-fx-font-size: 38px;" +
        "-fx-text-fill: #ff6b6b;" +
        "-fx-font-weight: bold;" +
        "-fx-effect: dropshadow(gaussian, rgba(255,107,107,0.5), 18, 0.35, 0, 0);"
    );

    Button backToMenuBtn = new Button("Back to Menu");
    backToMenuBtn.setStyle(primaryButtonStyle);
    backToMenuBtn.setOnAction(_ -> {
      engine.setState(MenuState.Menu);
    });

    VBox gameOverPanel = new VBox(20, gameOverLabel, backToMenuBtn);
    gameOverPanel.setAlignment(Pos.CENTER);
    gameOverPanel.setStyle(panelStyle);

    StackPane gameOverScreen = new StackPane(gameOverPanel);
    gameOverScreen.setAlignment(Pos.CENTER);
    gameOverScreen.setStyle("-fx-background-color: rgba(0, 0, 0, 0.55);");
    gameOverScreen.setVisible(false);

    // victory screen - shown once the player clears a full loop of
    // levels (Dungeon -> Ground -> Sky). The scrolling credits sit on
    // their own translucent dark card (rather than relying on text color
    // against the image directly) so they stay legible no matter which
    // part of the sky/clouds/dark treeline happens to be behind them.
    Label victoryLabel = new Label("\tWell Played.\nHow did you cheat?");
    victoryLabel.setStyle(
      "-fx-font-size: 46px;" +
        "-fx-text-fill: #ffd76a;" +
        "-fx-font-weight: bold;" +
        "-fx-effect: dropshadow(gaussian, rgba(255,215,106,0.55), 16, 0.4, 0, 0);"
    );

    Label victorySubLabel = new Label(
      "You cleared the dungeon, ground, and sky in one loop."
    );
    victorySubLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #f2e9ee;");

    // --- rolling credits ---------------------------------------------------
    final String creditsHeaderStyle =
      "-fx-font-size: 20px;" +
      "-fx-font-weight: bold;" +
      "-fx-text-fill: #ffb6d9;";
    final String creditsLineStyle =
      "-fx-font-size: 16px;" + "-fx-text-fill: #f5eef2;";

    Label madeByHeader = new Label("Made By");
    madeByHeader.setStyle(creditsHeaderStyle);

    VBox madeByNames = new VBox(4);
    madeByNames.setAlignment(Pos.CENTER);
    for (String name : new String[] {
      "Alibek",
      "Iunus",
      "Alim",
      "Yasir",
      "Arham",
    }) {
      Label nameLabel = new Label(name);
      nameLabel.setStyle(creditsLineStyle);
      madeByNames.getChildren().add(nameLabel);
    }

    Label specialThanksHeader = new Label("Special Thanks");
    specialThanksHeader.setStyle(creditsHeaderStyle);

    Label assetCreditLabel = new Label("Daniel — art assets");
    assetCreditLabel.setStyle(creditsLineStyle);

    Region creditsSpacerTop = new Region();
    creditsSpacerTop.setPrefHeight(40);
    Region creditsSpacerMid = new Region();
    creditsSpacerMid.setPrefHeight(40);

    VBox creditsContent = new VBox(
      12,
      victoryLabel,
      victorySubLabel,
      creditsSpacerTop,
      madeByHeader,
      madeByNames,
      creditsSpacerMid,
      specialThanksHeader,
      assetCreditLabel
    );
    creditsContent.setAlignment(Pos.CENTER);

    // a fixed-width card (not full-screen) so it reads as an intentional
    // floating panel rather than a flat overlay dimming the whole image
    final double CREDITS_CARD_WIDTH = 560.0;
    creditsContent.setPrefWidth(CREDITS_CARD_WIDTH);
    creditsContent.setLayoutX((SCREEN_WIDTH - CREDITS_CARD_WIDTH) / 2.0);
    creditsContent.setLayoutY(0);
    creditsContent.setStyle(
      "-fx-background-color: rgba(15, 8, 20, 0.62);" +
        "-fx-background-radius: 22;" +
        "-fx-padding: 30 40 30 40;"
    );

    Pane creditsClipPane = new Pane(creditsContent);
    creditsClipPane.setPrefSize(SCREEN_WIDTH, SCREEN_HEIGHT);
    creditsClipPane.setMouseTransparent(true);
    creditsClipPane.setClip(new Rectangle(SCREEN_WIDTH, SCREEN_HEIGHT));

    // scrolls the whole block from just below the screen to well above it,
    // classic movie-credits style; the button bar below stays fixed so the
    // player never has to wait for it to finish
    TranslateTransition creditsScroll = new TranslateTransition(
      Duration.seconds(20),
      creditsContent
    );
    creditsScroll.setFromY(SCREEN_HEIGHT);
    creditsScroll.setToY(-1400);
    creditsScroll.setInterpolator(Interpolator.LINEAR);
    creditsScroll.setCycleCount(1);

    final String victoryPrimaryButtonStyle =
      "-fx-font-size: 16px;" +
      "-fx-font-weight: bold;" +
      "-fx-text-fill: white;" +
      "-fx-background-color: linear-gradient(#b0447e, #6a1f52);" +
      "-fx-background-radius: 10;" +
      "-fx-border-color: rgba(255,255,255,0.7);" +
      "-fx-border-radius: 10;" +
      "-fx-border-width: 1.4;" +
      "-fx-padding: 10 32 10 32;" +
      "-fx-cursor: hand;";

    final String victorySecondaryButtonStyle =
      "-fx-font-size: 14px;" +
      "-fx-font-weight: bold;" +
      "-fx-text-fill: #4a1942;" +
      "-fx-background-color: rgba(255,255,255,0.7);" +
      "-fx-background-radius: 10;" +
      "-fx-border-color: rgba(122,25,83,0.5);" +
      "-fx-border-radius: 10;" +
      "-fx-border-width: 1.2;" +
      "-fx-padding: 8 26 8 26;" +
      "-fx-cursor: hand;";

    Button playAgainBtn = new Button("Play Again");
    playAgainBtn.setStyle(victoryPrimaryButtonStyle);
    playAgainBtn.setOnAction(_ -> {
      startGame[0].run();
    });

    Button victoryMenuBtn = new Button("Back to Menu");
    victoryMenuBtn.setStyle(victorySecondaryButtonStyle);
    victoryMenuBtn.setOnAction(_ -> {
      engine.setState(MenuState.Menu);
    });

    HBox victoryButtonBar = new HBox(16, playAgainBtn, victoryMenuBtn);
    victoryButtonBar.setAlignment(Pos.CENTER);
    // frosted-glass look instead of the dark navy panel used elsewhere,
    // so it sits naturally on top of the light, pastel ending art
    victoryButtonBar.setStyle(
      "-fx-background-color: rgba(255, 255, 255, 0.45);" +
        "-fx-background-radius: 16;" +
        "-fx-border-color: rgba(255, 255, 255, 0.7);" +
        "-fx-border-radius: 16;" +
        "-fx-border-width: 1.5;" +
        "-fx-padding: 16 28 16 28;"
    );
    StackPane.setAlignment(victoryButtonBar, Pos.BOTTOM_CENTER);
    StackPane.setMargin(victoryButtonBar, new Insets(0, 0, 28, 0));

    StackPane victoryScreen = buildVictoryScreen(
      creditsClipPane,
      victoryButtonBar,
      "backgrounds/ending.png"
    );

    StackPane root = new StackPane(
      mainCanvas.canvas,
      menu,
      gameOverScreen,
      victoryScreen
    );
    // dark gradient behind everything so the canvas isn't sitting on plain
    // white whenever it's cleared/transparent (menu, before a run starts)
    root.setStyle(
      "-fx-background-color: linear-gradient(to bottom, #101a2c, #04070d);"
    );
    scene.setRoot(root);
    stage.setScene(scene);

    engine.initState(MenuState.Menu);

    engine.onEnter(MenuState.Menu, _ -> {
      menu.setVisible(true);
    });
    engine.onExit(MenuState.Menu, _ -> {
      menu.setVisible(false);
    });

    engine.onEnter(GameState.GameOver, _ -> {
      gameOverScreen.setVisible(true);
    });
    engine.onExit(GameState.GameOver, _ -> {
      gameOverScreen.setVisible(false);
    });

    engine.onEnter(GameState.Victory, _ -> {
      victoryScreen.setVisible(true);
      creditsScroll.stop();
      creditsContent.setTranslateY(SCREEN_HEIGHT);
      creditsScroll.playFromStart();
    });
    engine.onExit(GameState.Victory, _ -> {
      victoryScreen.setVisible(false);
      creditsScroll.stop();
    });

    final double PLAYER_SPRITE_FRAME_W = 128.0d;
    final double PLAYER_SPRITE_FRAME_H = 128.0d;

    // tracks the current playthrough's player/room so the exit handler
    // below (registered once) can always clean up the right thing
    final Entity[] currentPlayer = new Entity[1];
    final RoomManager[] currentManager = new RoomManager[1];

    // player init system
    engine.onEnter(GameState.Running, world -> {
      PlayerState playerState = new PlayerState();
      world.addResource(playerState);

      final Image playerIdleImg = AssetStore.load("player/idle.png");
      final Image playerRunImg = AssetStore.load("player/run.png");
      final Image playerJumpImg = AssetStore.load("player/jump.png");
      final Image playerAttack1Img = AssetStore.load("player/attack1.png");
      final Image playerAttack2Img = AssetStore.load("player/attack2.png");
      final Image playerAttack3Img = AssetStore.load("player/attack3.png");
      final Image playerRunningAttackImg = AssetStore.load(
        "player/running_attack.png"
      );
      final Image playerHurtImg = AssetStore.load("player/hurt.png");
      final Image playerDeadImg = AssetStore.load("player/dead.png");

      AnimatedSprite playerSprite = new AnimatedSprite(
        PLAYER_SPRITE_FRAME_W,
        PLAYER_SPRITE_FRAME_H,
        0.15
      );
      playerSprite.frameWidth = PLAYER_SPRITE_FRAME_W;
      playerSprite.frameHeight = PLAYER_SPRITE_FRAME_H;
      playerSprite.sourceX = 0.0;
      playerSprite.sourceY = 0.0;
      playerSprite.offsetY = PLAYER_H / 2 - PLAYER_SPRITE_FRAME_H;
      playerSprite.zIndex = 100;
      // adjust frame count if player sprite changes
      playerSprite.addAnimation("idle", playerIdleImg, 4);
      playerSprite.addAnimation("run", playerRunImg, 7);
      playerSprite.addAnimation("jump", playerJumpImg, 6);
      // attacks play noticeably faster than the sprite's base 0.15s/frame
      // so swings feel snappy and the player can out-pace enemy attacks
      final double ATTACK_FRAME_DURATION = 0.08;
      playerSprite.addAnimation(
        "attack1",
        playerAttack1Img,
        5,
        false,
        ATTACK_FRAME_DURATION
      );
      playerSprite.addAnimation(
        "attack2",
        playerAttack2Img,
        4,
        false,
        ATTACK_FRAME_DURATION
      );
      playerSprite.addAnimation(
        "attack3",
        playerAttack3Img,
        4,
        false,
        ATTACK_FRAME_DURATION
      );
      playerSprite.addAnimation(
        "running_attack",
        playerRunningAttackImg,
        6,
        false,
        ATTACK_FRAME_DURATION
      );
      playerSprite.addAnimation("hurt", playerHurtImg, 2, false);
      playerSprite.addAnimation("dead", playerDeadImg, 6, false);
      playerSprite.setAnimation("idle");

      Entity player = world.spawn(
        playerSprite,
        new Transform2D(100, 300),
        new Velocity2D(),
        new Player()
      );
      currentPlayer[0] = player;

      world.addResource(new RoomRegistry());

      RoomManager manager = new RoomManager();
      manager.player = player;
      currentManager[0] = manager;
      world.addResource(manager);

      manager.transition(new DungeonRoom(), world);
    });

    // registered once (not inside onEnter) so it doesn't pile up duplicate
    // handlers across playthroughs - cleans up both the player AND every
    // entity the room was tracking (platforms, enemies, etc.), otherwise
    // they leak forever and the next playthrough piles a fresh room on top
    engine.onExit(GameState.Running, world -> {
      RoomManager manager = currentManager[0];
      if (manager != null) {
        manager.clearRoom(world);
      }

      Entity player = currentPlayer[0];
      if (player != null && world.isAlive(player)) {
        world.despawn(player);
      }

      currentManager[0] = null;
      currentPlayer[0] = null;
    });

    engine.addSystem(ScheduleStage.UPDATE, new PlayerMovementSystem());
    engine.addSystem(ScheduleStage.UPDATE, new EnemyAttackSystem());
    engine.addSystem(ScheduleStage.UPDATE, new EnemyDeathSystem());
    engine.addSystem(ScheduleStage.UPDATE, new PlayerAttackSystem());
    engine.addSystem(ScheduleStage.UPDATE, new RoomSystem());
    engine.addSystem(ScheduleStage.UPDATE, new HeartPickupSystem());
    engine.addSystem(ScheduleStage.UPDATE, new CameraSystem());

    engine.addSystem(ScheduleStage.UPDATE, new DamageSystem());
    engine.addSystem(ScheduleStage.RENDER, new HealthBarRenderSystem());
    engine.addSystem(ScheduleStage.RENDER, new BossHealthBarRenderSystem());
    engine.addSystem(ScheduleStage.RENDER, new ExitLockHintRenderSystem());

    // victory check: RoomSystem flags RoomManager.victoryPending when the
    // player reaches the loop-ending exit; actually switching engine state
    // has to happen here since that's not something a plain System is
    // allowed to do
    engine.addSystem(ScheduleStage.UPDATE, world -> {
      State<GameState> gameState = world.getResource(State.class);
      if (gameState.getState() != GameState.Running) {
        return;
      }

      RoomManager manager = world.getResource(RoomManager.class);
      if (manager != null && manager.victoryPending) {
        manager.victoryPending = false;
        engine.setState(GameState.Victory);
      }
    });

    // player death check: play the "dead" animation once, then switch to
    // the Game Over screen once it finishes playing
    engine.addSystem(ScheduleStage.UPDATE, world -> {
      State<GameState> gameState = world.getResource(State.class);
      if (gameState.getState() != GameState.Running) {
        return;
      }

      PlayerState currentPlayerState = world.getResource(PlayerState.class);
      if (currentPlayerState == null || !currentPlayerState.isDead()) {
        return;
      }

      world
        .query(Transform2D.class, AnimatedSprite.class)
        .with(Player.class)
        .forEach((_, playerComponents) -> {
          AnimatedSprite sprite = (AnimatedSprite) playerComponents[1];

          if (!currentPlayerState.dying) {
            currentPlayerState.dying = true;
            sprite.finished = false;
            sprite.setAnimation("dead");
            return;
          }

          if (sprite.finished) {
            engine.setState(GameState.GameOver);
          }
        });
    });

    stage.show();
    engine.start();
  }

  // builds a full-screen overlay with a background image (stretched to
  // fill) plus the given foreground content layered on top - falls back
  // to a plain dark scrim if the asset is missing. No scrim is used when
  // the image loads: the credits text is colored to read directly against
  // the art instead of dimming it with a flat overlay.
  private static StackPane buildVictoryScreen(
    Pane creditsClipPane,
    HBox buttonBar,
    String backgroundAssetPath
  ) {
    StackPane screen;
    Image background = AssetStore.load(backgroundAssetPath);

    if (background != null) {
      ImageView backgroundView = new ImageView(background);
      backgroundView.setFitWidth(SCREEN_WIDTH);
      backgroundView.setFitHeight(SCREEN_HEIGHT);
      backgroundView.setPreserveRatio(false);

      screen = new StackPane(backgroundView, creditsClipPane, buttonBar);
    } else {
      screen = new StackPane(creditsClipPane, buttonBar);
      screen.setStyle("-fx-background-color: rgba(0, 0, 0, 0.55);");
    }

    screen.setAlignment(Pos.CENTER);
    screen.setVisible(false);
    return screen;
  }
}
