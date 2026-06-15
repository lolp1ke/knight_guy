package knight_guy.game_engine_internals;

public final class Query {

  private final World world;
  private final long mask;
  private final int[] component_ids;

  // technically can be omitted
  // read more in .with
  private long with_mask = 0;
  private long without_mask = 0;

  Query(World world, final Class<? extends Component>... components) {
    this.world = world;
    this.component_ids = new int[components.length];
    long m = 0;
    for (int i = 0; i < components.length; i++) {
      int id = world.get_component_ty_id(components[i]);
      this.component_ids[i] = id;
      m |= (1L << id);
    }
    this.mask = m;
  }

  // .with is just like .without a filter method
  // component passed to .with won't be returned in .for_each callback
  // but technically the same result can be achieved through just by including it in this.mask
  public Query with(Class<? extends Component> ty) {
    int id = this.world.get_component_ty_id(ty);
    this.with_mask |= (1L << id);
    return this;
  }

  // filter to return entity components without specified component filter
  public Query without(Class<? extends Component> ty) {
    int id = this.world.get_component_ty_id(ty);
    this.without_mask |= (1L << id);
    return this;
  }

  // call callback to every matching component bundle
  public void for_each(EntityComponentCallback callback) {
    Component[] components = new Component[this.component_ids.length];
    int[] cols = new int[this.component_ids.length];

    for (final Archetype archetype : this.world.archetypes.values()) {
      long arch_mask = archetype.mask;
      if ((arch_mask & this.mask) != this.mask) {
        continue;
      }
      if (
        this.with_mask != 0 && (arch_mask & this.with_mask) != this.with_mask
      ) {
        continue;
      }
      if (this.without_mask != 0 && (arch_mask & this.without_mask) != 0) {
        continue;
      }

      for (int i = 0; i < this.component_ids.length; i++) {
        cols[i] = archetype.column_idx(this.component_ids[i]);
      }
      for (int row = 0; row < archetype.size; row++) {
        for (int i = 0; i < this.component_ids.length; i++) {
          components[i] = archetype.get(row, cols[i]);
        }
        callback.accept(new Entity(archetype.entities[row]), components);
      }
    }
  }

  public abstract interface EntityComponentCallback {
    void accept(Entity entity, Component... components);
  }
}
