// automatically generated, do not modify

package battlecode.schema;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
/**
 * The map a round is played on.
 */
public final class Map extends Table {
  public static Map getRootAsMap(ByteBuffer _bb) { return getRootAsMap(_bb, new Map()); }
  public static Map getRootAsMap(ByteBuffer _bb, Map obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public Map __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  /**
   * The name of a map.
   */
  public String name() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer nameAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  /**
   * The number of simulation steps a map requires per second.
   * The time delta for a simulation step can be calculated as 
   * dt = 1s/stepsPerSecond.
   */
  public int stepsPerSecond() { int o = __offset(6); return o != 0 ? bb.get(o + bb_pos) & 0xFF : 5; }
  public byte boundsType() { int o = __offset(8); return o != 0 ? bb.get(o + bb_pos) : 0; }
  /**
   * The bounds of the map.
   */
  public Table bounds(Table obj) { int o = __offset(10); return o != 0 ? __union(obj, o) : null; }
  /**
   * The bodies on the map.
   */
  public SpawnedBody bodies(int j) { return bodies(new SpawnedBody(), j); }
  public SpawnedBody bodies(SpawnedBody obj, int j) { int o = __offset(12); return o != 0 ? obj.__init(__indirect(__vector(o) + j * 4), bb) : null; }
  public int bodiesLength() { int o = __offset(12); return o != 0 ? __vector_len(o) : 0; }

  public static int createMap(FlatBufferBuilder builder,
      int nameOffset,
      int stepsPerSecond,
      byte bounds_type,
      int boundsOffset,
      int bodiesOffset) {
    builder.startObject(5);
    Map.addBodies(builder, bodiesOffset);
    Map.addBounds(builder, boundsOffset);
    Map.addName(builder, nameOffset);
    Map.addBoundsType(builder, bounds_type);
    Map.addStepsPerSecond(builder, stepsPerSecond);
    return Map.endMap(builder);
  }

  public static void startMap(FlatBufferBuilder builder) { builder.startObject(5); }
  public static void addName(FlatBufferBuilder builder, int nameOffset) { builder.addOffset(0, nameOffset, 0); }
  public static void addStepsPerSecond(FlatBufferBuilder builder, int stepsPerSecond) { builder.addByte(1, (byte)stepsPerSecond, 5); }
  public static void addBoundsType(FlatBufferBuilder builder, byte boundsType) { builder.addByte(2, boundsType, 0); }
  public static void addBounds(FlatBufferBuilder builder, int boundsOffset) { builder.addOffset(3, boundsOffset, 0); }
  public static void addBodies(FlatBufferBuilder builder, int bodiesOffset) { builder.addOffset(4, bodiesOffset, 0); }
  public static int createBodiesVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startBodiesVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endMap(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
};

