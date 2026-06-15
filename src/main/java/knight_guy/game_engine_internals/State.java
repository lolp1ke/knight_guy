package knight_guy.game_engine_internals;

// for anything that has a state (pretty self explanatory i think)
// usecases are ui states, state machines like player {flying, running, falling, dashing, etc. }
public final class State<T extends States> implements Resource {

  private T state = null;

  // constructor must be sealed (a.k.a. in java terms default visibility)
  // use Engine's (init_state|set_state)
  State(T state) {
    this.state = state;
  }

  void set_state(T state) {
    this.state = state;
  }

  public T get_state() {
    return this.state;
  }
}
