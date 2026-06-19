package knight_guy.game_engine_internals;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public final class Schedule {

  // list of callbacks mapped to scheduling stage
  private final EnumMap<ScheduleStage, List<System>> systems = new EnumMap<>(
    ScheduleStage.class
  );

  // init empty map
  public Schedule() {
    for (final ScheduleStage stage : ScheduleStage.values()) {
      this.systems.put(stage, new ArrayList<>());
    }
  }

  // append system to certain stage
  public void addSystem(ScheduleStage stage, System system) {
    this.systems.get(stage).add(system);
  }

  // execute system for certain stage
  public void run(ScheduleStage stage, World world) {
    for (final System system : this.systems.get(stage)) {
      system.run(world);
    }
  }
}
