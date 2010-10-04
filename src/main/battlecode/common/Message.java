package battlecode.common;

import battlecode.common.MapLocation;

/**
 * A Message is an object that can be broadcast to other robots. You
 * may freely assign arrays or null to an instance's fields, and when
 * the Message is broadcast it is copied into the receiving robots'
 * message queues. Message instances may be mutated and rebroadcast.
 *
 * <p>See the specs for more information about communication.
 */
public final class Message implements Cloneable {

    /**
     * An array of ints to broadcast.
     */
    public int[] ints;
    /**
     * An array of Strings to broadcast.
     */
    public String[] strings;
    /**
     * An array of MapLocations to broadcast.
     */
    public MapLocation[] locations;

    /**
     * Constructs an instance with a null value in each field.
     */
    public Message() {
    }

    /**
     * Returns the number of bytes in this message, which can be used
     * to determine the cost of broadcasting it in energon.
     */
    public int getNumBytes() {
        int cost = 0;
        if (ints != null) {
            cost += ints.length * 4;
        }
        if (locations != null) {
            cost += locations.length * 8;
        }
        if (strings != null) {
            for (int i = 0; i < strings.length; i++) {
                int strCost = 0;
                String str = strings[i];
                if (str != null) {
                    strCost = str.length();
                }
                if (strCost < 4) strCost = 4;
                cost += strCost;
            }
        }
        return cost;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() {
        Message c = new Message();
        if (ints != null) {
            c.ints = new int[ints.length];
            System.arraycopy(ints, 0, c.ints, 0, ints.length);
        }
        if (locations != null) {
            c.locations = new MapLocation[locations.length];
            System.arraycopy(locations, 0, c.locations, 0, locations.length);
        }
        if (strings != null) {
            c.strings = new String[strings.length];
            System.arraycopy(strings, 0, c.strings, 0, strings.length);
        }
        return c;
    }
}
