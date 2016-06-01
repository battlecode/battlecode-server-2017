// automatically generated, do not modify

package battlecode.schema;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
/**
 * A circular map.
 */
public final class CircleBounds extends Table {
  public static CircleBounds getRootAsCircleBounds(ByteBuffer _bb) { return getRootAsCircleBounds(_bb, new CircleBounds()); }
  public static CircleBounds getRootAsCircleBounds(ByteBuffer _bb, CircleBounds obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public CircleBounds __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public float radius() { int o = __offset(4); return o != 0 ? bb.getFloat(o + bb_pos) : 0; }

  public static int createCircleBounds(FlatBufferBuilder builder,
      float radius) {
    builder.startObject(1);
    CircleBounds.addRadius(builder, radius);
    return CircleBounds.endCircleBounds(builder);
  }

  public static void startCircleBounds(FlatBufferBuilder builder) { builder.startObject(1); }
  public static void addRadius(FlatBufferBuilder builder, float radius) { builder.addFloat(0, radius, 0); }
  public static int endCircleBounds(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
};

