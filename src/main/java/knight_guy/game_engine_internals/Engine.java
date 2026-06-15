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
  private final HashMap<States, List<System>> enter_systems = new HashMap<>();
  // on exits
  private final HashMap<States, List<System>> exit_systems = new HashMap<>();

  public World get_world() {
    return this.world;
  }

  // build plugin
  public void add_plugin(Plugin plugin) {
    plugin.build(this, this.world);
  }

  public void add_system(ScheduleStage stage, System system) {
    this.schedule.add_system(stage, system);
  }

  // appends system to execute when state enter happens
  public <T extends States> void on_enter(T state, System system) {
    this.enter_systems
      .computeIfAbsent(state, _ -> new ArrayList<>())
      .add(system);
  }

  // appends system to execute when state exit happens
  public <T extends States> void on_exit(T state, System system) {
    this.exit_systems
      .computeIfAbsent(state, _ -> new ArrayList<>())
      .add(system);
  }

  // updates current state and runs on_exit/on_enter systems
  public <T extends States> void set_state(T next) {
    State<T> current_state = this.world.get_resource(State.class);
    this.run_systems(this.exit_systems.get(current_state.get_state()));
    current_state.set_state(next);
    this.run_systems(this.enter_systems.get(next));
  }

  private void run_systems(List<System> systems) {
    if (systems == null) {
      return;
    }
    for (System sys : systems) {
      sys.run(this.world);
    }
  }

  // initializes state and run on_enter system
  public <T extends States> void init_state(T state) {
    this.world.add_resource(new State<T>(state));
    this.run_systems(this.enter_systems.get(state));
  }

  // entry point for a game loop
  public void start() {
    this.world.add_resource(new Time());

    new AnimationTimer() {
      private long last_time = 0;
      private double accumulator = 0.0;
      private static final double TICK_RATE = 1.0 / 32.0;

      @Override
      public void handle(long now) {
        if (this.last_time == 0) {
          this.last_time = now;
          return;
        }

        double delta = (now - this.last_time) / 1_000_000_000.0;
        this.last_time = now;
        this.accumulator += delta;

        schedule.run(ScheduleStage.STARTUP, world);

        while (accumulator >= TICK_RATE) {
          Time time = world.get_resource(Time.class);
          time.delta = TICK_RATE;
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
