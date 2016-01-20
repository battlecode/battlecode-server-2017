package battlecode.serial;

import battlecode.common.GameConstants;
import battlecode.common.RobotType;
import battlecode.server.ErrorReporter;
import battlecode.server.Version;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Makes the game self-describing.
 * We don't actually use this, but other clients can.
 *
 * @author james
 */
@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public final class StoredConstants implements ServerEvent {

    /**
     * Maps game constant names to values.
     */
    private final Map<String, Object> gameConstants;

    /**
     * Maps robot types to values.
     */
    private final Map<String, Map<String, Object>> robotTypes;

    /**
     * Engine version.
     */
    private final String engineVersion;

    public StoredConstants() {
        this.gameConstants = new HashMap<>();
        this.robotTypes = new HashMap<>();
        this.engineVersion = Version.version;

        try {
            this.computeValues();
        } catch (ReflectiveOperationException e) {
            ErrorReporter.report(e, true);
        }
    }

    private void computeValues() throws ReflectiveOperationException {
        for (Field field : GameConstants.class.getDeclaredFields()) {
            if (field.isSynthetic()) {
                continue;
            }
            gameConstants.put(field.getName(), field.get(null));
        }

        for (final RobotType type : RobotType.values()) {
            final Map<String, Object> typeValues = new HashMap<>();

            final int staticOrPrivate = (Modifier.STATIC | Modifier.PRIVATE);

            for (Field field : RobotType.class.getDeclaredFields()) {
                if (field.isSynthetic() || field.isEnumConstant() ||
                        (field.getModifiers() & staticOrPrivate) != 0) {
                    continue;
                }

                typeValues.put(field.getName(), field.get(type));
            }
            for (Method method : RobotType.class.getDeclaredMethods()) {
                if (method.isSynthetic() || method.isBridge()
                        || (method.getModifiers() & staticOrPrivate) != 0
                        || method.getParameterCount() != 0) {
                    continue;
                }

                typeValues.put(method.getName(), method.invoke(type));
            }

            robotTypes.put(type.name(), typeValues);
        }
    }
}
