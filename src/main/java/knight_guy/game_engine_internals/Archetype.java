package knight_guy.game_engine_internals;

import java.util.Arrays;

// leave as default
final class Archetype {

  private static final int INITIAL_CAPACITY = 64;

  final long mask;
  private final int[] componentIds;
  int[] entities;
  Object[][] columns;
  int size;
  private int capacity;

  Archetype(long mask, int[] componentIds) {
    this.mask = mask;
    this.componentIds = componentIds;
    this.size = 0;
    this.capacity = INITIAL_CAPACITY;
    this.entities = new int[INITIAL_CAPACITY];
    this.columns = new Object[componentIds.length][INITIAL_CAPACITY];
  }

  int columnIdx(int componentId) {
    for (int i = 0; i < this.componentIds.length; i++) {
      if (this.componentIds[i] == componentId) {
        return i;
      }
    }
    return -1;
  }

  <T extends Component> T get(int row, int column_idx) {
    return (T) this.columns[column_idx][row];
  }

  int addEntity(int entity_id) {
    if (this.size >= this.capacity) {
      this.grow();
    }
    int row = this.size;
    this.entities[row] = entity_id;
    this.size += 1;
    return row;
  }

  void setComponent(int row, int column_idx, Component component) {
    this.columns[column_idx][row] = component;
  }

  int removeEntity(int row) {
    int last_row = this.size - 1;
    int moved_id = this.entities[last_row];
    if (row != last_row) {
      this.entities[row] = moved_id;
      for (int i = 0; i < this.componentIds.length; i++) {
        this.columns[i][row] = this.columns[i][last_row];
        this.columns[i][last_row] = null;
      }
    } else {
      for (int i = 0; i < this.componentIds.length; i++) {
        this.columns[i][last_row] = null;
      }
    }
    this.size -= 1;
    return row != last_row ? moved_id : -1;
  }

  void copyInto(int target_row, Archetype source, int source_row) {
    for (int i = 0; i < this.componentIds.length; i++) {
      int cid = this.componentIds[i];
      int src_col = source.columnIdx(cid);
      if (src_col >= 0) {
        this.columns[i][target_row] = source.columns[src_col][source_row];
      }
    }
  }

  // double it and give to yourself :)
  private void grow() {
    int new_capacity = this.capacity * 2;
    this.entities = Arrays.copyOf(this.entities, new_capacity);
    for (int i = 0; i < this.columns.length; i++) {
      this.columns[i] = Arrays.copyOf(this.columns[i], new_capacity);
    }
    this.capacity = new_capacity;
  }
}
