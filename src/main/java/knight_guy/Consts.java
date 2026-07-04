package knight_guy;

// bunch of constans
public abstract interface Consts {
  static final double SCREEN_WIDTH = 800.0;
  static final double SCREEN_HEIGHT = 600.0;

  static final double LEVEL_WIDTH = 1200.0;
  static final double LEVEL_HEIGHT = 600.0;

  // shrunk further so the collision box actually matches the character's
  // silhouette instead of dominating the screen - a big square hitbox was
  // eating hits that should've been dodgeable
  static final double PLAYER_W = 50.0;
  static final double PLAYER_H = 40.0;

  static final double GRAVITY = 1400.0;

  static final double SPEED = 340.0;
  static final double DASH_SPEED = 900.0;
  static final double DASH_COOLDOWN = 1.0;
  static final double DASH_DURATION = 0.15;

  // how quickly horizontal speed ramps up/down instead of snapping instantly
  // - raised so starting/stopping feels immediate rather than floaty
  static final double ACCELERATION = 3600.0;
  static final double DECELERATION = 4200.0;
  // movement is a bit less responsive while airborne, like most platformers
  // - raised from 0.6 so air control doesn't feel like wading through mud
  static final double AIR_CONTROL = 0.75;

  // grace period after walking off a ledge where jump still works
  static final double COYOTE_TIME = 0.1;
  // grace period where a jump press slightly before landing still triggers
  static final double JUMP_BUFFER_TIME = 0.1;

  static final double ATTACK_COMBO_THRESHOLD = 0.3;

  // paired with the higher GRAVITY above to keep roughly the same jump
  // height but a quicker, punchier rise/fall instead of a lazy arc
  static final double JUMP_VEL = -625.0;

  static final double FLOOR_Y = 585.0;

  static final double CLIMB_SPEED = 200.0;

  public static final double ENEMY_W = 40.0;
  public static final double ENEMY_H = 60.0;

  // how much vertical difference an enemy will forgive before deciding
  // the player is out of its swing entirely - tightened again to match
  // the smaller player hitbox above
  public static final double ENEMY_ATTACK_VERTICAL_TOLERANCE = 30.0;

  // the player's own sword reach when checking for enemies to hit -
  // longer than the enemies' reach so trading hits favors the player
  public static final double PLAYER_ATTACK_RANGE_X = 110.0;
  public static final double PLAYER_ATTACK_RANGE_Y = 60.0;
}
