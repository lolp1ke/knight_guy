package knight_guy;

// bunch of constans
public abstract interface Consts {
  static final double SCREEN_WIDTH = 800.0;
  static final double SCREEN_HEIGHT = 600.0;

  static final double PLAYER_W = 64.0;
  static final double PLAYER_H = 64.0;

  static final double GRAVITY = 1200.0;

  static final double SPEED = 300.0;
  static final double DASH_SPEED = 2400.0;
  static final double DASH_COOLDOWN = 1.0;
  static final double DASH_DURATION = 0.15;

  static final double ATTACK_COMBO_THRESHOLD = 0.3;

  static final double JUMP_VEL = -580.0;

  static final double FLOOR_Y = 585.0;

  static final double CLIMB_SPEED = 200.0;
}
