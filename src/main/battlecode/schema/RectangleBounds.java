// automatically generated, do not modify

package battlecode.schema;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
/**
 * A rectangular map.
 */
public final class RectangleBounds extends Table {
  public static RectangleBounds getRootAsRectangleBounds(ByteBuffer _bb) { return getRootAsRectangleBounds(_bb, new RectangleBounds()); }
  public static RectangleBounds getRootAsRectangleBounds(ByteBuffer _bb, RectangleBounds obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public RectangleBounds __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public float width() { int o = __offset(4); return o != 0 ? bb.getFloat(o + bb_pos) : 0; }
  public float height() { int o = __offset(6); return o != 0 ? bb.getFloat(o + bb_pos) : 0; }

  public static int createRectangleBounds(FlatBufferBuilder builder,
      float width,
      float height) {
    builder.startObject(2);
    RectangleBounds.addHeight(builder, height);
    RectangleBounds.addWidth(builder, width);
    return RectangleBounds.endRectangleBounds(builder);
  }

  public static void startRectangleBounds(FlatBufferBuilder builder) { builder.startObject(2); }
  public static void addWidth(FlatBufferBuilder builder, float width) { builder.addFloat(0, width, 0); }
  public static void addHeight(FlatBufferBuilder builder, float height) { builder.addFloat(1, height, 0); }
  public static int endRectangleBounds(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
};

