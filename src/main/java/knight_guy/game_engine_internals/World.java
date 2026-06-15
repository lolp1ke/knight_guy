package knight_guy.game_engine_internals;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public final class World {

  private static final class EntityRecord {

    Archetype archetype;
    int row;

    EntityRecord(Archetype archetype, int row) {
      this.archetype = archetype;
      this.row = row;
    }
  }

  private final HashMap<Class<? extends Component>, Integer> component_ty_ids =
    new HashMap<>();
  private int next_component_ty_id = 0;

  // map mask to archetype
  final HashMap<Long, Archetype> archetypes = new HashMap<>();

  private EntityRecord[] entity_records = new EntityRecord[256];
  private final Queue<Integer> free_entity_ids = new ArrayDeque<>();
  private int next_entity_id = 1;

  private final Map<Class<? extends Resource>, Resource> resources =
    new HashMap<>();

  int get_component_ty_id(final Class<? extends Component> ty) {
    return this.component_ty_ids.computeIfAbsent(ty, _ -> {
      int id = this.next_component_ty_id;
      this.next_component_ty_id += 1;
      return id;
    });
  }

  // create an entity
  public Entity spawn() {
    int id;
    if (this.free_entity_ids.isEmpty()) {
      id = this.next_entity_id;
      this.next_entity_id += 1;
      this.ensure_capacity(id);
    } else {
      id = this.free_entity_ids.poll();
    }
    this.entity_records[id] = null;
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
    EntityRecord record = this.entity_records[id];
    if (record != null && record.archetype != null) {
      int moved_id = record.archetype.remove_entity(record.row);
      if (moved_id >= 0) {
        this.entity_records[moved_id].row = record.row;
      }
    }
    this.entity_records[id] = null;
    this.free_entity_ids.add(id);
  }

  // remove entities
  public void despawn(Entity... entities) {
    for (final Entity entity : entities) {
      this.despawn(entity);
    }
  }

  // append component to entity
  public <T extends Component> void insert(Entity entity, T component) {
    int component_id = this.get_component_ty_id(component.getClass());
    int id = (int) entity.id;
    this.ensure_capacity(id);

    EntityRecord record = this.entity_records[id];
    if (record != null && record.archetype != null) {
      int col = record.archetype.column_idx(component_id);
      if (col >= 0) {
        record.archetype.set_component(record.row, col, component);
        return;
      }
      this.move_to_archetype(
        id,
        record,
        record.archetype.mask | (1L << component_id),
        component
      );
    } else {
      long mask = 1L << component_id;
      Archetype arch = this.get_or_create_archetype(mask);
      int row = arch.add_entity(id);
      arch.set_component(row, arch.column_idx(component_id), component);
      this.entity_records[id] = new EntityRecord(arch, row);
    }
  }

  // get a component from an entity
  // use this over querying if you want specific entity's component
  public <T extends Component> T get_component(Entity entity, Class<T> type) {
    int component_id = this.get_component_ty_id(type);
    int id = (int) entity.id;
    if (id >= this.entity_records.length) {
      return null;
    }
    EntityRecord record = this.entity_records[id];
    if (record == null || record.archetype == null) {
      return null;
    }
    int col = record.archetype.column_idx(component_id);
    if (col < 0) {
      return null;
    }
    return record.archetype.get(record.row, col);
  }

  public <T extends Component> T remove_component(
    Entity entity,
    Class<T> type
  ) {
    int component_id = this.get_component_ty_id(type);
    int id = (int) entity.id;
    if (id >= this.entity_records.length) {
      return null;
    }
    EntityRecord record = this.entity_records[id];
    if (record == null || record.archetype == null) {
      return null;
    }

    int col = record.archetype.column_idx(component_id);
    if (col < 0) {
      return null;
    }

    T removed = type.cast(record.archetype.columns[col][record.row]);

    long old_mask = record.archetype.mask;
    long new_mask = old_mask & ~(1L << component_id);

    if (new_mask == 0) {
      int moved_id = record.archetype.remove_entity(record.row);
      if (moved_id >= 0) {
        this.entity_records[moved_id].row = record.row;
      }
      this.entity_records[id] = null;
    } else {
      Archetype new_arch = this.get_or_create_archetype(new_mask);
      int new_row = new_arch.add_entity(id);
      new_arch.copy_into(new_row, record.archetype, record.row);

      int moved_id = record.archetype.remove_entity(record.row);
      if (moved_id >= 0) {
        this.entity_records[moved_id].row = record.row;
      }
      this.entity_records[id] = new EntityRecord(new_arch, new_row);
    }

    return removed;
  }

  // checks whether entity is still in the world or it was despawned
  public boolean is_alive(Entity entity) {
    int id = (int) entity.id;
    return id < this.entity_records.length && this.entity_records[id] != null;
  }

  // retrieve resource by its type (a.k.a. class)
  public <T extends Resource> T get_resource(final Class<T> ty) {
    Resource r = this.resources.get(ty);
    return r == null ? null : ty.cast(r);
  }

  // append resource (a.k.a. class)
  public void add_resource(final Resource resource) {
    this.resources.put(resource.getClass(), resource);
  }

  public final Query query(final Class<? extends Component>... components) {
    return new Query(this, components);
  }

  private void move_to_archetype(
    int entity_id,
    EntityRecord record,
    long new_mask,
    Component extra
  ) {
    Archetype new_arch = this.get_or_create_archetype(new_mask);
    int new_row = new_arch.add_entity(entity_id);
    new_arch.copy_into(new_row, record.archetype, record.row);

    if (extra != null) {
      int col = new_arch.column_idx(this.get_component_ty_id(extra.getClass()));
      new_arch.set_component(new_row, col, extra);
    }

    int moved_id = record.archetype.remove_entity(record.row);
    if (moved_id >= 0) {
      this.entity_records[moved_id].row = record.row;
    }
    this.entity_records[entity_id] = new EntityRecord(new_arch, new_row);
  }

  private Archetype get_or_create_archetype(long mask) {
    return this.archetypes.computeIfAbsent(mask, m -> {
      List<Integer> ids = new ArrayList<>();
      long temp = m;
      int bit_idx = 0;
      while (temp != 0) {
        if ((temp & 1l) != 0) {
          ids.add(bit_idx);
        }
        // unsigned right shift
        temp >>>= 1;
        bit_idx += 1;
      }
      int[] component_ids = new int[ids.size()];
      for (int i = 0; i < ids.size(); i++) {
        component_ids[i] = ids.get(i);
      }
      return new Archetype(m, component_ids);
    });
  }

  private void ensure_capacity(int id) {
    if (id >= this.entity_records.length) {
      this.entity_records = Arrays.copyOf(
        this.entity_records,
        this.entity_records.length * 2
      );
    }
  }
}
