// automatically generated, do not modify

package battlecode.schema;

/**
 * Types of bounds a map can have.
 */
public final class Bounds {
  private Bounds() { }
  public static final byte NONE = 0;
  public static final byte RectangleBounds = 1;
  public static final byte CircleBounds = 2;

  private static final String[] names = { "NONE", "RectangleBounds", "CircleBounds", };

  public static String name(int e) { return names[e]; }
};

