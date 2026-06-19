package knight_guy.game_engine_internals;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class World {

  private static final class EntityRecord {

    Archetype archetype;
    int row;

    EntityRecord(Archetype archetype, int row) {
      this.archetype = archetype;
      this.row = row;
    }
  }

  private final HashMap<Class<? extends Component>, Integer> componentTyIds =
    new HashMap<>();
  private int nextComponentTyId = 0;

  // map mask to archetype
  final HashMap<Long, Archetype> archetypes = new HashMap<>();

  private EntityRecord[] entityRecords = new EntityRecord[256];
  private final ArrayDeque<Integer> freeEntityIds = new ArrayDeque<>();
  private int nextEntityId = 1;

  private final Map<Class<? extends Resource>, Resource> resources =
    new HashMap<>();

  int getComponentTyId(final Class<? extends Component> ty) {
    return this.componentTyIds.computeIfAbsent(ty, _ -> {
      int id = this.nextComponentTyId;
      this.nextComponentTyId += 1;
      return id;
    });
  }

  // create an entity
  public Entity spawn() {
    int id;
    if (this.freeEntityIds.isEmpty()) {
      id = this.nextEntityId;
      this.nextEntityId += 1;
      this.ensureCapacity(id);
    } else {
      id = this.freeEntityIds.poll();
    }
    this.entityRecords[id] = null;
    return new Entity(id);
  }

  // create an entity and append components to it
  public Entity spawn(final Component... components) {
    Entity entity = this.spawn();

    for (final Component component : components) {
      this.insert(entity, component);
    }

    return entity;
  }

  // remove an entity
  public void despawn(Entity entity) {
    int id = (int) entity.id;
    EntityRecord record = this.entityRecords[id];
    if (record != null && record.archetype != null) {
      int movedId = record.archetype.removeEntity(record.row);
      if (movedId >= 0) {
        this.entityRecords[movedId].row = record.row;
      }
    }
    this.entityRecords[id] = null;
    this.freeEntityIds.add(id);
  }

  // remove entities
  public void despawn(Entity... entities) {
    for (final Entity entity : entities) {
      this.despawn(entity);
    }
  }

  // append component to entity
  public <T extends Component> void insert(Entity entity, T component) {
    int componentId = this.getComponentTyId(component.getClass());
    int id = (int) entity.id;
    this.ensureCapacity(id);

    EntityRecord record = this.entityRecords[id];
    if (record != null && record.archetype != null) {
      int col = record.archetype.columnIdx(componentId);
      if (col >= 0) {
        record.archetype.setComponent(record.row, col, component);
        return;
      }
      this.moveToArchetype(
        id,
        record,
        record.archetype.mask | (1L << componentId),
        component
      );
    } else {
      long mask = 1L << componentId;
      Archetype arch = this.getOrCreateArchetype(mask);
      int row = arch.addEntity(id);
      arch.setComponent(row, arch.columnIdx(componentId), component);
      this.entityRecords[id] = new EntityRecord(arch, row);
    }
  }

  // get a component from an entity
  // use this over querying if you want specific entity's component
  public <T extends Component> T getComponent(Entity entity, Class<T> type) {
    int componentId = this.getComponentTyId(type);
    int id = (int) entity.id;
    if (id >= this.entityRecords.length) {
      return null;
    }
    EntityRecord record = this.entityRecords[id];
    if (record == null || record.archetype == null) {
      return null;
    }
    int col = record.archetype.columnIdx(componentId);
    if (col < 0) {
      return null;
    }
    return record.archetype.get(record.row, col);
  }

  public <T extends Component> T removeComponent(Entity entity, Class<T> type) {
    int componentId = this.getComponentTyId(type);
    int id = (int) entity.id;
    if (id >= this.entityRecords.length) {
      return null;
    }
    EntityRecord record = this.entityRecords[id];
    if (record == null || record.archetype == null) {
      return null;
    }

    int col = record.archetype.columnIdx(componentId);
    if (col < 0) {
      return null;
    }

    T removed = type.cast(record.archetype.columns[col][record.row]);

    long oldMask = record.archetype.mask;
    long newMask = oldMask & ~(1L << componentId);

    if (newMask == 0) {
      int movedId = record.archetype.removeEntity(record.row);
      if (movedId >= 0) {
        this.entityRecords[movedId].row = record.row;
      }
      this.entityRecords[id] = null;
    } else {
      Archetype newArch = this.getOrCreateArchetype(newMask);
      int newRow = newArch.addEntity(id);
      newArch.copyInto(newRow, record.archetype, record.row);

      int movedId = record.archetype.removeEntity(record.row);
      if (movedId >= 0) {
        this.entityRecords[movedId].row = record.row;
      }
      this.entityRecords[id] = new EntityRecord(newArch, newRow);
    }

    return removed;
  }

  // checks whether entity is still in the world or it was despawned
  public boolean isAlive(Entity entity) {
    int id = (int) entity.id;
    return id < this.entityRecords.length && this.entityRecords[id] != null;
  }

  // retrieve resource by its type (a.k.a. class)
  public <T extends Resource> T getResource(final Class<T> ty) {
    Resource resource = this.resources.get(ty);
    return resource == null ? null : ty.cast(resource);
  }

  // append resource (a.k.a. class)
  public void addResource(final Resource resource) {
    this.resources.put(resource.getClass(), resource);
  }

  public final Query query(final Class<? extends Component>... components) {
    return new Query(this, components);
  }

  private void moveToArchetype(
    final int entityId,
    EntityRecord record,
    final long newMask,
    final Component extra
  ) {
    Archetype newArch = this.getOrCreateArchetype(newMask);
    int newRow = newArch.addEntity(entityId);
    newArch.copyInto(newRow, record.archetype, record.row);

    if (extra != null) {
      int column = newArch.columnIdx(this.getComponentTyId(extra.getClass()));
      newArch.setComponent(newRow, column, extra);
    }

    int movedId = record.archetype.removeEntity(record.row);
    if (movedId >= 0) {
      this.entityRecords[movedId].row = record.row;
    }
    this.entityRecords[entityId] = new EntityRecord(newArch, newRow);
  }

  private Archetype getOrCreateArchetype(final long mask) {
    return this.archetypes.computeIfAbsent(mask, m -> {
      List<Integer> ids = new ArrayList<>();
      long temp = m;
      int bitIdx = 0;
      while (temp != 0) {
        if ((temp & 1l) != 0) {
          ids.add(bitIdx);
        }
        // unsigned right shift
        temp >>>= 1;
        bitIdx += 1;
      }
      int[] componentIds = new int[ids.size()];
      for (int i = 0; i < ids.size(); i++) {
        componentIds[i] = ids.get(i);
      }
      return new Archetype(m, componentIds);
    });
  }

  private void ensureCapacity(final int id) {
    if (id >= this.entityRecords.length) {
      this.entityRecords = Arrays.copyOf(
        this.entityRecords,
        this.entityRecords.length * 2
      );
    }
  }
}
