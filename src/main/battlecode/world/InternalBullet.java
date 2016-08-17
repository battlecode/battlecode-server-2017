package battlecode.world;

/**
 * The representation of a bullet used by the server.
 */
public class InternalBullet {



    // ******************************************
    // ****** GETTER METHODS ********************
    // ******************************************

    // ******************************************
    // ****** UPDATE METHODS ********************
    // ******************************************

    // *********************************
    // ****** MISC. METHODS ************
    // *********************************

    @Override
    public boolean equals(Object o) {
        return o != null && (o instanceof InternalTree)
                && ((InternalTree) o).getID() == ID;
    }

    @Override
    public int hashCode() {
        return ID;
    }

    @Override
    public String toString() {
        return String.format("%s:%s#%d", getTeam(), getType(), getID());
    }

}
