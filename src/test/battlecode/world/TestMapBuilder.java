package battlecode.world;

import battlecode.common.*;
import battlecode.schema.*;
import battlecode.schema.GameMap;
import battlecode.server.TeamMapping;
import battlecode.util.FlatHelpers;
import com.google.flatbuffers.FlatBufferBuilder;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

@Ignore
/**
 * Lets maps be built easily, for testing purposes.
 */
public class TestMapBuilder {
    private String name;
    private MapLocation origin;
    private float width;
    private float height;
    private int seed;
    private int rounds;

    private List<BodyInfo> bodies;

    public TestMapBuilder(String name, float oX, float oY, float width, float height, int seed, int rounds) {
        this(name, new MapLocation(oX, oY), width, height, seed, rounds);
    }

    public TestMapBuilder(String name, MapLocation origin, float width, float height, int seed, int rounds) {
        this.name = name;
        this.origin = origin;
        this.width = width;
        this.height = height;
        this.seed = seed;
        this.bodies = new ArrayList<>();
    }

    public TestMapBuilder addRobot(int id, Team team, RobotType type, MapLocation loc){
        bodies.add(new RobotInfo(
                id,
                team,
                type,
                loc,
                0,
                0,
                type.getStartingHealth()
        ));

        return this;
    }

    public TestMapBuilder addNeutralTree(int id, MapLocation loc, float radius, int containedBullets, RobotType containedBody){
        bodies.add(new TreeInfo(
                id, Team.NEUTRAL, loc, radius, 0, containedBullets, containedBody
        ));

        return this;
    }

    public TestMapBuilder addBody(BodyInfo info) {
        bodies.add(info);

        return this;
    }

    public LiveMap build() {
        return new LiveMap(
                width, height, origin, seed, GameConstants.GAME_DEFAULT_ROUNDS, name,
                bodies.toArray(new BodyInfo[bodies.size()])
        );
    }
}
