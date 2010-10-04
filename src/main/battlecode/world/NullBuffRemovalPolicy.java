package battlecode.world;

public class NullBuffRemovalPolicy extends BuffRemovalPolicy {

	public NullBuffRemovalPolicy(InternalBuff buff) {
		super(buff);
	}

	public boolean remove() { return false; }
	
}