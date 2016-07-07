// automatically generated, do not modify

package battlecode.schema;

/**
 * The 'category' a Body falls into.
 * We can add more of these later.
 * It might make sense to encode these dynamically, the way we do teams; this
 * is simple and easy for now, though.
 */
public final class BodyType {
  private BodyType() { }
  /**
   * A simple robot.
   */
  public static final byte ROBOT = 0;
  /**
   * A sitting Body that cannot be destroyed.
   */
  public static final byte RUBBLE = 1;
  /**
   * A bullet that moves in a perfectly straight line.
   * Note: bullet location updates are not sent; a bullet is defined to be
   * in position loc + dt * vel after dt seconds.
   * This allows us some significant space savings, since there are lots
   * of bullets, and we don't need to send position updates.
   * The event stream will say if a bullet has been destroyed.
   */
  public static final byte BULLET = 2;

  private static final String[] names = { "ROBOT", "RUBBLE", "BULLET", };

  public static String name(int e) { return names[e]; }
};

