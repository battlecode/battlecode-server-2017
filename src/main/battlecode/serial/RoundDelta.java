package battlecode.serial;

import battlecode.world.signal.InternalSignal;
import battlecode.world.signal.IndicatorStringSignal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A list of Signals that occurred in a round.
 */
public class RoundDelta implements ServerEvent {

    private static final long serialVersionUID = 1667367676711924140L;

    private InternalSignal[] internalSignals;

    public RoundDelta() {
    }

    public RoundDelta(InternalSignal[] internalSignals) {
        this.internalSignals = internalSignals;
        foldIndicatorSignals();
    }

    public InternalSignal[] getInternalSignals() {
        return internalSignals;
    }

    public void setInternalSignals(InternalSignal[] internalSignals) {
        this.internalSignals = internalSignals;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        foldIndicatorSignals();
    }

    private static class IndicatorString {
        int robot;
        int index;

        public IndicatorString(IndicatorStringSignal iss) {
            this.robot = iss.getRobotID();
            this.index = iss.getStringIndex();
        }

        public boolean equals(Object obj) {
            if (obj instanceof IndicatorString) {
                IndicatorString is = (IndicatorString) obj;
                return robot == is.robot && index == is.index;
            }
            return false;
        }

        public int hashCode() {
            return robot << index;
        }
    }

    private void foldIndicatorSignals() {
        HashMap<IndicatorString, Integer> folded = new HashMap<>();
        for (int i = 0; i < internalSignals.length; i++) {
            if (internalSignals[i] instanceof IndicatorStringSignal) {
                IndicatorString is = new IndicatorString((IndicatorStringSignal) internalSignals[i]);
                Integer prevSignal = folded.get(is);
                if (prevSignal != null) {
                    internalSignals[prevSignal] = null;
                }
                folded.put(is, i);
            }
        }
        ArrayList<InternalSignal> foldedInternalSignals = new ArrayList<>(internalSignals.length);
        for (InternalSignal internalSignal : internalSignals) {
            if (internalSignal != null) {
                foldedInternalSignals.add(internalSignal);
            }
        }
        internalSignals = new InternalSignal[foldedInternalSignals.size()];
        foldedInternalSignals.toArray(internalSignals);
    }
}
