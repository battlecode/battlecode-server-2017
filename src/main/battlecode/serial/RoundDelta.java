package battlecode.serial;

import battlecode.engine.signal.Signal;
import battlecode.world.signal.IndicatorStringSignal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class RoundDelta implements Serializable {

    private static final long serialVersionUID = 1667367676711924140L;
    private Signal[] signals;

    public RoundDelta() {
    }

    public RoundDelta(Signal[] signals) {
        this.signals = signals;
        foldIndicatorSignals();
    }

    public Signal[] getSignals() {
        return signals;
    }

    public void setSignals(Signal[] signals) {
        this.signals = signals;
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
        HashMap<IndicatorString, Integer> folded = new HashMap<IndicatorString, Integer>();
        for (int i = 0; i < signals.length; i++) {
            if (signals[i] instanceof IndicatorStringSignal) {
                IndicatorString is = new IndicatorString((IndicatorStringSignal) signals[i]);
                Integer prevSignal = folded.get(is);
                if (prevSignal != null) {
                    signals[prevSignal] = null;
                }
                folded.put(is, i);
            }
        }
        ArrayList<Signal> foldedSignals = new ArrayList<Signal>(signals.length);
        for (int i = 0; i < signals.length; i++) {
            if (signals[i] != null) {
                foldedSignals.add(signals[i]);
            }
        }
        signals = new Signal[foldedSignals.size()];
        foldedSignals.toArray(signals);
    }
}
