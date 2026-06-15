package knight_guy.game_engine_internals;

// easy interface to bundle logic
// check out plugins folder for examples
public abstract interface Plugin {
  void build(Engine engine, World world);
}
