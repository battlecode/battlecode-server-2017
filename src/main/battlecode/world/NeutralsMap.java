package battlecode.world;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.MovementType;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TerrainTile;
import battlecode.world.signal.AttackSignal;
import battlecode.world.InternalObject;
import battlecode.world.InternalRobot;
import battlecode.world.signal.MovementSignal;

/**
 * Represents a map (scalar field) of a neutral AI.
 */
public class NeutralsMap {
    /**
     * The width and height of the map.
     */
    private final int mapWidth, mapHeight;
    /**
     * The integer scalar field showing the distribution of the neutral AI.
     */
    private boolean[][] passable;
    private double[][] currentAmount;
    private double[][] growthFactor;
    private double[][] dX, dY;
    private ArrayList<MapLocation> attacks;
    private Set[][] ids;

    public NeutralsMap(double[][] growthFactor, TerrainTile[][] mapTiles) {
        attacks = new ArrayList<MapLocation>();

        this.mapWidth = growthFactor.length;
        int tempMapHeight = 0;
        for (int i = 0; i < this.mapWidth; i++) {
            tempMapHeight = Math.max(this.mapHeight, growthFactor[i].length);
        }
        this.mapHeight = tempMapHeight;

        this.growthFactor = new double[this.mapWidth][this.mapHeight];
        this.currentAmount = new double[this.mapWidth][this.mapHeight];
        for (int i = 0; i < this.mapWidth; i++) {
            System.arraycopy(growthFactor[i], 0, this.growthFactor[i], 0,
                this.mapHeight);
        }

        dX = new double[this.mapWidth][this.mapHeight];
        dY = new double[this.mapWidth][this.mapHeight];
        passable = new boolean[this.mapWidth][this.mapHeight];
        for (int i = 0; i < this.mapWidth; i++) {
            for (int j = 0; j < this.mapHeight; j++) {
                passable[i][j] = mapTiles[i][j] != TerrainTile.VOID;
            }
        }
        ids = new Set[this.mapWidth][this.mapHeight];
        for (int i = 0; i < this.mapWidth; i++) {
            for (int j = 0; j < this.mapHeight; j++) {
                ids[i][j] = new TreeSet<Integer>();
            }
        }
    }

    public NeutralsMap(NeutralsMap nm) {
        this.mapWidth = nm.mapWidth;
        this.mapHeight = nm.mapHeight;
        this.currentAmount = new double[this.mapWidth][this.mapHeight];
        for (int i = 0; i < this.mapWidth; i++) {
            System.arraycopy(nm.currentAmount[i], 0, this.currentAmount[i], 0,
                this.mapHeight);
        }
        this.growthFactor = new double[mapWidth][mapHeight];
        for (int i = 0; i < this.mapWidth; i++) {
            System.arraycopy(nm.growthFactor[i], 0, this.growthFactor[i], 0,
                this.mapHeight);
        }
        this.dX = new double[this.mapWidth][this.mapHeight];
        this.dY = new double[this.mapWidth][this.mapHeight];
        this.passable = new boolean[this.mapWidth][this.mapHeight];
        for (int i = 0; i < this.mapWidth; i++) {
            System.arraycopy(nm.passable[i], 0, this.passable[i], 0, this.mapHeight);
        }
    }

    public double[][] copyOfData() {
        double[][] cowsCopy = new double[this.mapWidth][this.mapHeight];
        for (int i = 0; i < this.mapWidth; i++) {
            System.arraycopy(this.growthFactor[i], 0, cowsCopy[i], 0,
                this.mapHeight);
        }
        return cowsCopy;
    }

    public boolean isValid(int x, int y) {
        return x >= 0 && x < this.mapWidth && y >= 0 && y < this.mapHeight && passable[x][y];
    }

    public boolean isValid(int x, int y, int fromX, int fromY) {
        if (!isValid(x, y)) return false;
        return ids[x][y].equals(ids[fromX][fromY]);
    }

    public void print() {
        System.out.println("Neutrals Map!");
        for (int j = 0; j < this.mapHeight; j++) {
            for (int i = 0; i < this.mapWidth; i++) {
                System.out.print(Double.toString(this.currentAmount[i][j]).substring(0, 3) + " (" + this.growthFactor[i][j] + ")\t");
            }
            System.out.println();
        }
        System.out.println("END Neutrals Map");
    }

    public double getScoreChange(Team t, InternalObject[] objs) {
        double delta = 0.0;
        for (InternalObject obj : objs) {
            InternalRobot ir = (InternalRobot) obj;
            if (ir.getTeam() != t) {
                continue;
            }
            int captureRange = 0;
            if (ir.type == RobotType.PASTR) captureRange = 5;
            MapLocation[] affected = MapLocation.getAllMapLocationsWithinRadiusSq(ir.getLocation(), captureRange);
            for (MapLocation ml : affected) {
                if (isValid(ml.x, ml.y)) {
                    delta += this.currentAmount[ml.x][ml.y];
                }
            }
        }
        return delta;
    }

    @SuppressWarnings("unchecked")
    public void updateIds(InternalObject obj) {
        InternalRobot ir = (InternalRobot) obj;
        int captureRange = 0;
        if (ir.type == RobotType.PASTR) captureRange = 5;
        MapLocation[] affected = MapLocation.getAllMapLocationsWithinRadiusSq(ir.getLocation(), captureRange);
        for (MapLocation ml : affected) {
            if (isValid(ml.x, ml.y)) {
                this.ids[ml.x][ml.y].add(ir.getID());
            }
        }
    }

    final double PI4 = Math.PI / 4;
    final int[][] dirs = {{-1, 0}, {-1, -1}, {0, -1}, {1, -1},
                             {1, 0}, {1, 1}, {0, 1}, {-1, 1}};
    public void next(InternalObject[] objs) {
        for (InternalObject obj : objs) {
            updateIds(obj);
        }

        // Current order:
        // 1) cows are destroyed due to attack
        // 2) cows move
        // 3) cow growth and decay happens
        for (int i = 0; i < attacks.size(); i++) {
            MapLocation target = attacks.get(i);
            if (target.x >= 0 && target.x < this.mapWidth &&
                target.y >= 0 && target.y < this.mapHeight) {
                this.currentAmount[target.x][target.y] = 0;
            }
        }

        double[][] temp = new double[this.mapWidth][this.mapHeight];
        for (int i = 0; i < this.mapWidth; i++) {
            for (int j = 0; j < this.mapHeight; j++) {
                if (this.dX[i][j] != 0 || this.dY[i][j] != 0) {
                    double theta = Math.atan2(this.dY[i][j], this.dX[i][j]);
                    boolean moved = false;
                    for (int k = -4; k < 4; k++) {
                        if (theta >= k * PI4 && theta <= (k + 1) * PI4) {
                            double frac1 = 1 - (theta - k * PI4) / PI4;
                            int x1 = i + dirs[k + 4][0];
                            int y1 = j + dirs[k + 4][1];
                            boolean valid1 = isValid(x1, y1, i, j);

                            double frac2 = 1 - frac1;
                            int x2 = i + dirs[(k + 5) % 8][0];
                            int y2 = j + dirs[(k + 5) % 8][1];
                            boolean valid2 = isValid(x2, y2, i, j);

                            if (valid1 && valid2) {
                                temp[x1][y1] += frac1 * this.currentAmount[i][j];
                                temp[x2][y2] += frac2 * this.currentAmount[i][j];
                            } else if (valid1) {
                                temp[x1][y1] += this.currentAmount[i][j];
                            } else if (valid2) {
                                temp[x2][y2] += this.currentAmount[i][j];
                            } else {
                                temp[i][j] += this.currentAmount[i][j];
                            }

                            moved = true;
                            break;
                        }
                    }
                    if (!moved) {
                        throw new RuntimeException("GameActionException thrown", new GameActionException(GameActionExceptionType.INTERNAL_ERROR, "Failed to move cows away from noise."));
                    }
                } else {
                    temp[i][j] += this.currentAmount[i][j];
                }
            }
        }
        for (int i = 0; i < this.mapWidth; i++) {
            for (int j = 0; j < this.mapHeight; j++) {
                this.currentAmount[i][j] = temp[i][j];
            }
        }

        for (int i = 0; i < this.mapWidth; i++) {
            for (int j = 0; j < this.mapHeight; j++) {
                if (this.isValid(i, j)) {
                    this.currentAmount[i][j] = GameConstants.NEUTRALS_TURN_DECAY * this.currentAmount[i][j] + this.growthFactor[i][j];
                }
            }
        }

        //this.print();
        this.resetAfterTurn();
    }

    public void updateWithNoiseSource(MapLocation source, int radiusSquared) {
        MapLocation[] affected = MapLocation.getAllMapLocationsWithinRadiusSq(source, radiusSquared);
        for (int i = 0; i < affected.length; i++) {
            if (affected[i].equals(source)) {
                // nothing
            } else if (isValid(affected[i].x, affected[i].y)) {
                double curdX = affected[i].x - source.x;
                double curdY = affected[i].y - source.y;
                double mag = Math.sqrt(curdX * curdX + curdY * curdY);
                curdX /= mag;
                curdY /= mag;
                dX[affected[i].x][affected[i].y] += curdX;
                dY[affected[i].x][affected[i].y] += curdY;
            }
        }
    }

    public void updateWithMovement(MovementSignal movement) {
        if (movement.getMovementType() != MovementType.SNEAK) {
            MapLocation source = movement.getNewLoc();
            updateWithNoiseSource(source, GameConstants.MOVEMENT_SCARE_RANGE);
        }
    }

    public void updateWithAttack(AttackSignal attack) {
        attacks.add(attack.getTargetLoc());
        updateWithNoiseSource(attack.getTargetLoc(), GameConstants.ATTACK_SCARE_RANGE);
    }

    public void resetAfterTurn() {
        for (int i = 0; i < this.mapWidth; i++) {
            for (int j = 0; j < this.mapHeight; j++) {
                this.dX[i][j] = 0;
                this.dY[i][j] = 0;
                this.ids[i][j].clear();
            }
        }
        attacks.clear();
    }
}
