// automatically generated, do not modify

package battlecode.schema;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
/**
 * A new Body to be placed on the map.
 */
public final class SpawnedBody extends Table {
  public static SpawnedBody getRootAsSpawnedBody(ByteBuffer _bb) { return getRootAsSpawnedBody(_bb, new SpawnedBody()); }
  public static SpawnedBody getRootAsSpawnedBody(ByteBuffer _bb, SpawnedBody obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public SpawnedBody __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  /**
   * The numeric ID of the new Body.
   */
  public int robotID() { int o = __offset(4); return o != 0 ? bb.getShort(o + bb_pos) & 0xFFFF : 0; }
  /**
   * The team of the new Body.
   */
  public int teamID() { int o = __offset(6); return o != 0 ? bb.get(o + bb_pos) & 0xFF : 0; }
  /**
   * The type of the new Body.
   */
  public int type() { int o = __offset(8); return o != 0 ? bb.get(o + bb_pos) & 0xFF : 0; }
  /**
   * The radius of the Body.
   */
  public float radius() { int o = __offset(10); return o != 0 ? bb.getFloat(o + bb_pos) : 0; }
  /**
   * The location of the Body, in meters from the center of the map.
   */
  public Vec loc() { return loc(new Vec()); }
  public Vec loc(Vec obj) { int o = __offset(12); return o != 0 ? obj.__init(o + bb_pos, bb) : null; }
  /**
   * The velocity of the Body, in meters PER SECOND (NOT per round).
   */
  public Vec vel() { return vel(new Vec()); }
  public Vec vel(Vec obj) { int o = __offset(14); return o != 0 ? obj.__init(o + bb_pos, bb) : null; }

  public static void startSpawnedBody(FlatBufferBuilder builder) { builder.startObject(6); }
  public static void addRobotID(FlatBufferBuilder builder, int robotID) { builder.addShort(0, (short)robotID, 0); }
  public static void addTeamID(FlatBufferBuilder builder, int teamID) { builder.addByte(1, (byte)teamID, 0); }
  public static void addType(FlatBufferBuilder builder, int type) { builder.addByte(2, (byte)type, 0); }
  public static void addRadius(FlatBufferBuilder builder, float radius) { builder.addFloat(3, radius, 0); }
  public static void addLoc(FlatBufferBuilder builder, int locOffset) { builder.addStruct(4, locOffset, 0); }
  public static void addVel(FlatBufferBuilder builder, int velOffset) { builder.addStruct(5, velOffset, 0); }
  public static int endSpawnedBody(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
};

