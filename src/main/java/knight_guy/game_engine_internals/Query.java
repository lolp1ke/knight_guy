package knight_guy.game_engine_internals;

public final class Query {

  private final World world;
  private final long mask;
  private final int[] componentIds;

  // technically can be omitted
  // read more in .with
  private long withMask = 0;
  private long withoutMask = 0;

  Query(World world, final Class<? extends Component>... components) {
    this.world = world;
    this.componentIds = new int[components.length];
    long m = 0;
    for (int i = 0; i < components.length; i++) {
      int id = world.getComponentTyId(components[i]);
      this.componentIds[i] = id;
      m |= (1L << id);
    }
    this.mask = m;
  }

  // .with is just like .without a filter method
  // component passed to .with won't be returned in .for_each callback
  // but technically the same result can be achieved through just by including it in this.mask
  public Query with(Class<? extends Component> ty) {
    int id = this.world.getComponentTyId(ty);
    this.withMask |= (1L << id);
    return this;
  }

  // filter to return entity components without specified component filter
  public Query without(Class<? extends Component> ty) {
    int id = this.world.getComponentTyId(ty);
    this.withoutMask |= (1L << id);
    return this;
  }

  // call callback to every matching component bundle
  public void forEach(EntityComponentCallback callback) {
    Component[] components = new Component[this.componentIds.length];
    int[] cols = new int[this.componentIds.length];

    for (final Archetype archetype : this.world.archetypes.values()) {
      long archMask = archetype.mask;
      if ((archMask & this.mask) != this.mask) {
        continue;
      }
      if (this.withMask != 0 && (archMask & this.withMask) != this.withMask) {
        continue;
      }
      if (this.withoutMask != 0 && (archMask & this.withoutMask) != 0) {
        continue;
      }

      for (int i = 0; i < this.componentIds.length; i++) {
        cols[i] = archetype.columnIdx(this.componentIds[i]);
      }
      for (int row = 0; row < archetype.size; row++) {
        for (int i = 0; i < this.componentIds.length; i++) {
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
