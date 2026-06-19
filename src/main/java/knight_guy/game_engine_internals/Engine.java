package knight_guy.game_engine_internals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.animation.AnimationTimer;
import knight_guy.game_engine_internals.resources.Time;

public final class Engine {

  // Engine's data storage
  private final World world = new World();
  // contains systems, basically callbacks
  private final Schedule schedule = new Schedule();

  // on enters
  private final HashMap<States, List<System>> enterSystems = new HashMap<>();
  // on exits
  private final HashMap<States, List<System>> exitSystems = new HashMap<>();

  public World getWorld() {
    return this.world;
  }

  // build plugin
  public void addPlugin(Plugin plugin) {
    plugin.build(this, this.world);
  }

  public void addSystem(ScheduleStage stage, System system) {
    this.schedule.addSystem(stage, system);
  }

  // appends system to execute when state enter happens
  public <T extends States> void onEnter(T state, System system) {
    this.enterSystems
      .computeIfAbsent(state, _ -> new ArrayList<>())
      .add(system);
  }

  // appends system to execute when state exit happens
  public <T extends States> void onExit(T state, System system) {
    this.exitSystems.computeIfAbsent(state, _ -> new ArrayList<>()).add(system);
  }

  // updates current state and runs on_exit/on_enter systems
  public <T extends States> void setState(T next) {
    State<T> currentState = this.world.getResource(State.class);
    this.runSystems(this.exitSystems.get(currentState.getState()));
    currentState.setState(next);
    this.runSystems(this.enterSystems.get(next));
  }

  private void runSystems(List<System> systems) {
    if (systems == null) {
      return;
    }
    for (System system : systems) {
      system.run(this.world);
    }
  }

  // initializes state and run on_enter system
  public <T extends States> void initState(T state) {
    this.world.addResource(new State<T>(state));
    this.runSystems(this.enterSystems.get(state));
  }

  // entry point for a game loop
  public void start() {
    this.world.addResource(new Time());

    new AnimationTimer() {
      private long lastTime = 0;
      private double accumulator = 0.0;
      private static final double TICK_RATE = 1.0 / 32.0;

      @Override
      public void handle(long now) {
        if (this.lastTime == 0) {
          this.lastTime = now;
          return;
        }

        double delta = (now - this.lastTime) / 1_000_000_000.0;
        this.lastTime = now;
        this.accumulator += delta;

        schedule.run(ScheduleStage.STARTUP, world);

        Time time = world.getResource(Time.class);
        time.delta = TICK_RATE;
        while (accumulator >= TICK_RATE) {
          schedule.run(ScheduleStage.PRE_UPDATE, world);
          schedule.run(ScheduleStage.UPDATE, world);
          schedule.run(ScheduleStage.POST_UPDATE, world);
          // decrease by TICK_RATE so if any performance issues arrive game is still ran on 1 / TICK_RATE ticks
          this.accumulator -= TICK_RATE;
        }

        schedule.run(ScheduleStage.RENDER, world);
      }
    }.start();
    // FIXME: i think this won't run cuz .stop on AnimationTimer haven't been called
    //        use some atomic bool and change the value of it in exit_btn instead of Platform.exit()
    //        for now doesn't matter
    schedule.run(ScheduleStage.SHUTDOWN, world);
    // uncomment after fixing FIXME above
    // Platform.exit();
  }
}
