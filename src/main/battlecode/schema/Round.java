// automatically generated, do not modify

package battlecode.schema;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
/**
 * A single time-step in a Game.
 * The bulk of the data in the file is stored in tables like this.
 * Note that a struct-of-arrays format is more space efficient than an array-
 * of-structs.
 */
public final class Round extends Table {
  public static Round getRootAsRound(ByteBuffer _bb) { return getRootAsRound(_bb, new Round()); }
  public static Round getRootAsRound(ByteBuffer _bb, Round obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public Round __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  /**
   * Turns since the start of a match. Rounds may be skipped, so this
   * is useful.
   */
  public long number() { int o = __offset(4); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0; }
  /**
   * The IDs of bodies that moved.
   */
  public long movedIDs(int j) { int o = __offset(6); return o != 0 ? (long)bb.getInt(__vector(o) + j * 4) & 0xFFFFFFFFL : 0; }
  public int movedIDsLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  public ByteBuffer movedIDsAsByteBuffer() { return __vector_as_bytebuffer(6, 4); }
  /**
   * The new locations of bodies that have moved. They are defined to be in
   * their new locations at exactly the time round.number*dt.
   */
  public Vec movedLocs(int j) { return movedLocs(new Vec(), j); }
  public Vec movedLocs(Vec obj, int j) { int o = __offset(8); return o != 0 ? obj.__init(__vector(o) + j * 8, bb) : null; }
  public int movedLocsLength() { int o = __offset(8); return o != 0 ? __vector_len(o) : 0; }
  /**
   * New bodies. They come into existence at round.number*dt.
   */
  public SpawnedBody spawned(int j) { return spawned(new SpawnedBody(), j); }
  public SpawnedBody spawned(SpawnedBody obj, int j) { int o = __offset(10); return o != 0 ? obj.__init(__indirect(__vector(o) + j * 4), bb) : null; }
  public int spawnedLength() { int o = __offset(10); return o != 0 ? __vector_len(o) : 0; }
  /**
   * The IDs of bodies that died. They died at round.number*dt.
   */
  public long diedIDs(int j) { int o = __offset(12); return o != 0 ? (long)bb.getInt(__vector(o) + j * 4) & 0xFFFFFFFFL : 0; }
  public int diedIDsLength() { int o = __offset(12); return o != 0 ? __vector_len(o) : 0; }
  public ByteBuffer diedIDsAsByteBuffer() { return __vector_as_bytebuffer(12, 4); }

  public static int createRound(FlatBufferBuilder builder,
      long number,
      int movedIDsOffset,
      int movedLocsOffset,
      int spawnedOffset,
      int diedIDsOffset) {
    builder.startObject(5);
    Round.addDiedIDs(builder, diedIDsOffset);
    Round.addSpawned(builder, spawnedOffset);
    Round.addMovedLocs(builder, movedLocsOffset);
    Round.addMovedIDs(builder, movedIDsOffset);
    Round.addNumber(builder, number);
    return Round.endRound(builder);
  }

  public static void startRound(FlatBufferBuilder builder) { builder.startObject(5); }
  public static void addNumber(FlatBufferBuilder builder, long number) { builder.addInt(0, (int)number, 0); }
  public static void addMovedIDs(FlatBufferBuilder builder, int movedIDsOffset) { builder.addOffset(1, movedIDsOffset, 0); }
  public static int createMovedIDsVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addInt(data[i]); return builder.endVector(); }
  public static void startMovedIDsVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addMovedLocs(FlatBufferBuilder builder, int movedLocsOffset) { builder.addOffset(2, movedLocsOffset, 0); }
  public static void startMovedLocsVector(FlatBufferBuilder builder, int numElems) { builder.startVector(8, numElems, 4); }
  public static void addSpawned(FlatBufferBuilder builder, int spawnedOffset) { builder.addOffset(3, spawnedOffset, 0); }
  public static int createSpawnedVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startSpawnedVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addDiedIDs(FlatBufferBuilder builder, int diedIDsOffset) { builder.addOffset(4, diedIDsOffset, 0); }
  public static int createDiedIDsVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addInt(data[i]); return builder.endVector(); }
  public static void startDiedIDsVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endRound(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
};

