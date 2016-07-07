// automatically generated, do not modify

package battlecode.schema;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
/**
 * The first event sent in the game. Contains all metadata about the game.
 */
public final class GameHeader extends Table {
  public static GameHeader getRootAsGameHeader(ByteBuffer _bb) { return getRootAsGameHeader(_bb, new GameHeader()); }
  public static GameHeader getRootAsGameHeader(ByteBuffer _bb, GameHeader obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public GameHeader __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  /**
   * The teams participating in the game.
   */
  public TeamData teams(int j) { return teams(new TeamData(), j); }
  public TeamData teams(TeamData obj, int j) { int o = __offset(4); return o != 0 ? obj.__init(__indirect(__vector(o) + j * 4), bb) : null; }
  public int teamsLength() { int o = __offset(4); return o != 0 ? __vector_len(o) : 0; }

  public static int createGameHeader(FlatBufferBuilder builder,
      int teamsOffset) {
    builder.startObject(1);
    GameHeader.addTeams(builder, teamsOffset);
    return GameHeader.endGameHeader(builder);
  }

  public static void startGameHeader(FlatBufferBuilder builder) { builder.startObject(1); }
  public static void addTeams(FlatBufferBuilder builder, int teamsOffset) { builder.addOffset(0, teamsOffset, 0); }
  public static int createTeamsVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startTeamsVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endGameHeader(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
};

