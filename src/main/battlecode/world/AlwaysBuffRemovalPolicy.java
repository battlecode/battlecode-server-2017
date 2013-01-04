package battlecode.world;

public class AlwaysBuffRemovalPolicy extends BuffRemovalPolicy {

    public AlwaysBuffRemovalPolicy(InternalBuff buff) {
        super(buff);
    }

    public boolean remove() {
        return true;
    }
}
