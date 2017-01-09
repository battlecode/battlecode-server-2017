package battlecode.util;

import battlecode.common.RobotType;
import battlecode.schema.BodyType;
import battlecode.schema.VecTable;
import battlecode.schema.RGBTable;
import com.google.flatbuffers.FlatBufferBuilder;
import gnu.trove.TByteCollection;
import gnu.trove.list.TByteList;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TByteArrayList;
import gnu.trove.list.array.TFloatArrayList;

import java.util.List;
import java.util.function.ObjIntConsumer;

/**
 * Misc. helper functions for working with flatbuffers.
 *
 * @author james
 */
public class FlatHelpers {
    public static RobotType getRobotTypeFromBodyType(byte bodyType) {
        switch (bodyType) {
            case BodyType.ARCHON:
                return RobotType.ARCHON;
            case BodyType.GARDENER:
                return RobotType.GARDENER;
            case BodyType.LUMBERJACK:
                return RobotType.LUMBERJACK;
            case BodyType.SCOUT:
                return RobotType.SCOUT;
            case BodyType.SOLDIER:
                return RobotType.SOLDIER;
            case BodyType.TANK:
                return RobotType.TANK;
            case BodyType.NONE:
                return null;
            default:
                throw new RuntimeException("No robot type for: "+bodyType);
        }
    }

    public static byte getBodyTypeFromRobotType(RobotType type) {
        if (type == null) {
            return BodyType.NONE;
        }

        switch (type) {
            case ARCHON:
                return BodyType.ARCHON;
            case GARDENER:
                return BodyType.GARDENER;
            case LUMBERJACK:
                return BodyType.LUMBERJACK;
            case SCOUT:
                return BodyType.SCOUT;
            case SOLDIER:
                return BodyType.SOLDIER;
            case TANK:
                return BodyType.TANK;
            default:
                throw new RuntimeException("No body type for: "+type);
        }
    }

    /**
     * DO NOT CALL THIS WITH OFFSETS!
     * Only call it when you're adding an actual int[] to a buffer,
     * not a Table[].
     * For that, call offsetVector.
     *
     * Well that's a weird API.
     *
     * Call like so:
     * int xyzP = intVector(builder, xyz, BufferType::startXyzVector);
     */
    public static int intVector(FlatBufferBuilder builder,
                                TIntList arr,
                                ObjIntConsumer<FlatBufferBuilder> start) {
        final int length = arr.size();
        start.accept(builder, length);

        // arrays go backwards in flatbuffers
        // for reasons
        for (int i = length - 1; i >= 0; i--) {
            builder.addInt(arr.get(i));
        }
        return builder.endVector();
    }

    /**
     * This is DIFFERENT from intVector!
     *
     * Call this when you're adding a table of offsets, not flat ints.
     */
    public static int offsetVector(FlatBufferBuilder builder,
                                   TIntList arr,
                                   ObjIntConsumer<FlatBufferBuilder> start) {
        final int length = arr.size();
        start.accept(builder, length);

        // arrays go backwards in flatbuffers
        // for reasons
        for (int i = length - 1; i >= 0; i--) {
            builder.addOffset(arr.get(i));
        }
        return builder.endVector();
    }

    public static int floatVector(FlatBufferBuilder builder,
                                  TFloatList arr,
                                  ObjIntConsumer<FlatBufferBuilder> start) {
        final int length = arr.size();
        start.accept(builder, length);

        for (int i = length - 1; i >= 0; i--) {
            builder.addFloat(arr.get(i));
        }
        return builder.endVector();
    }

    public static int byteVector(FlatBufferBuilder builder,
                                 TByteList arr,
                                 ObjIntConsumer<FlatBufferBuilder> start) {
        final int length = arr.size();
        start.accept(builder, length);

        for (int i = length - 1; i >= 0; i--) {
            builder.addByte(arr.get(i));
        }
        return builder.endVector();
    }

    public static int createVecTable(FlatBufferBuilder builder, TFloatList xs, TFloatList ys) {
        if (xs.size() != ys.size()) {
            throw new RuntimeException("Mismatched x/y length: "+xs.size()+" != "+ys.size());
        }
        int xsP = floatVector(builder, xs, VecTable::startXsVector);
        int ysP = floatVector(builder, ys, VecTable::startYsVector);
        return VecTable.createVecTable(builder, xsP, ysP);
    }

    public static int createRGBTable(FlatBufferBuilder builder, TIntList red, TIntList green, TIntList blue) {
        if (red.size() != green.size() || green.size() != blue.size()) {
            throw new RuntimeException("Mismatched lengths: "+red.size()+", "+green.size()+", "+blue.size());
        }
        int redP = intVector(builder, red, RGBTable::startRedVector);
        int greenP = intVector(builder, green, RGBTable::startGreenVector);
        int blueP = intVector(builder, blue, RGBTable::startBlueVector);
        return RGBTable.createRGBTable(builder, redP, greenP, blueP);
    }
}
