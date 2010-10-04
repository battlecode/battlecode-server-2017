package battlecode.world;

import battlecode.common.TerrainTile;

import java.io.Serializable;
import static battlecode.common.GameConstants.MAX_FLUX_PER_TILE;

import battlecode.world.GameWorld;

public class InternalTerrainTile extends TerrainTile implements Serializable{
	private static final long serialVersionUID = 785608353848029247L;
	
	protected int flux;

	public InternalTerrainTile(int height, TerrainTile.TerrainType type){
		super(height,type);
	}
	
	public InternalTerrainTile(InternalTerrainTile tile){
		super(tile);
		this.flux = tile.flux;
	}

	public int getFlux() {
		return flux;
	}

	public int mineFlux() {
		int tmpFlux = flux;
		flux = 0;
		return tmpFlux;
	}

	public void processBeginningOfRound(int round) {
		if (getType() == TerrainType.LAND && round % height == 0 && flux < MAX_FLUX_PER_TILE){
			flux++;
			
		}
    }
}