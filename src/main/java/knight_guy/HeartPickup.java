package knight_guy;

import knight_guy.game_engine_internals.Component;

public final class HeartPickup implements Component {

  public int healAmount = 1;

  public HeartPickup() {}

  public HeartPickup(int healAmount) {
    this.healAmount = healAmount;
  }
}
