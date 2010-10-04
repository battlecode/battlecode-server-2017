package battlecode.common;

public enum AuraType {
	OFF(5,250),
	DEF(5,250),
        MOV(5,250);

	private final int _switchCost;
	private final int _fluxCost;

	AuraType(int fluxCost, int switchCost) {
		_switchCost = switchCost;
		_fluxCost = fluxCost;
	}

	public int switchCost() {
		return _switchCost;
	}

	public int fluxCost() {
		return _fluxCost;
	}
}