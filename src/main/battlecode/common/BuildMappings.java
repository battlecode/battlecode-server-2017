/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package battlecode.common;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Sasa
 */
public class BuildMappings {

    private static final Map<ComponentType, Set<Chassis>> chassisMappings = new EnumMap<ComponentType, Set<Chassis>>(ComponentType.class);
    private static final Map<ComponentType, Set<ComponentType>> componentMappings = new EnumMap<ComponentType, Set<ComponentType>>(ComponentType.class);

    static {
        chassisMappings.put(ComponentType.RECYCLER, EnumSet.of(Chassis.LIGHT));
        chassisMappings.put(ComponentType.CONSTRUCTOR, EnumSet.of(Chassis.BUILDING));
        chassisMappings.put(ComponentType.ARMORY, EnumSet.of(
                Chassis.LIGHT,
                Chassis.MEDIUM,
                Chassis.FLYING));
        chassisMappings.put(ComponentType.ARMORY, EnumSet.of(
                Chassis.LIGHT,
                Chassis.MEDIUM,
                Chassis.HEAVY));

        componentMappings.put(ComponentType.RECYCLER, EnumSet.of(
                ComponentType.SHIELD,
                ComponentType.PLATING,
                ComponentType.SMG,
                ComponentType.HAMMER,
                ComponentType.SIGHT,
                ComponentType.RADAR,
                ComponentType.ANTENNA,
                ComponentType.PROCESSOR,
                ComponentType.CONSTRUCTOR));

        componentMappings.put(ComponentType.CONSTRUCTOR, EnumSet.of(
                ComponentType.RECYCLER));

        componentMappings.put(ComponentType.FACTORY, EnumSet.of(
                ComponentType.HARDENED,
                ComponentType.REGEN,
                ComponentType.IRON,
                ComponentType.RAILGUN,
                ComponentType.MEDIC,
                ComponentType.TELESCOPE,
                ComponentType.DUMMY,
                ComponentType.DROPSHIP));

        componentMappings.put(ComponentType.ARMORY, EnumSet.of(
                ComponentType.PLASMA,
                ComponentType.BEAM,
                ComponentType.SATELLITE,
                ComponentType.NETWORK,
                ComponentType.JUMP,
                ComponentType.BUG));
    }

    public static boolean canBuild(ComponentType t, Chassis c) {
        if (chassisMappings.get(t).contains(c)) return true;
        return false;
    }

    public static boolean canBuild(ComponentType t, ComponentType c) {
        if (componentMappings.get(t).contains(c)) return true;
        return false;
    }
}
