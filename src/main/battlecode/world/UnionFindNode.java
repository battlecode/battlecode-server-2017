package battlecode.world;

/**
 * Tarjan's union-find data structure.  We use it for
 * checking if the power node graph is connected.
 */
public class UnionFindNode {

    private int rank;
    private UnionFindNode parent;

    public UnionFindNode() {
        rank = 0;
        parent = this;
    }

    public UnionFindNode find() {
        UnionFindNode n = this;
        while (n.parent != n)
            n = n.parent;
        parent = n;
        return n;
    }

    public void union(UnionFindNode n) {
        UnionFindNode myRoot = find();
        UnionFindNode nRoot = n.find();
        if (myRoot.rank < nRoot.rank)
            myRoot.parent = nRoot;
        else if (nRoot.rank < myRoot.rank)
            nRoot.parent = myRoot;
        else {
            nRoot.parent = myRoot;
            myRoot.rank++;
        }
    }

}
