package battlecode.world;

import battlecode.common.GameConstants;

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
    private double[][] currentAmount;
    private double[][] growthFactor;

    public NeutralsMap(double[][] growthFactor) {
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
    }

    public void print() {
        System.out.println("Neutrals Map!");
        for (int j = 0; j < this.mapHeight; j++) {
            for (int i = 0; i < this.mapWidth; i++) {
                System.out.print(this.currentAmount[i][j] + " (" + this.growthFactor[i][j] + ")\t");
            }
            System.out.println();
        }
        System.out.println("END Neutrals Map");
    }

    public void next() {
        for (int i = 0; i < this.mapWidth; i++) {
            for (int j = 0; j < this.mapHeight; j++) {
                this.currentAmount[i][j] = GameConstants.NEUTRALS_TURN_DECAY * this.currentAmount[i][j] + this.growthFactor[i][j];
            }
        }
        this.print();
    }
}
