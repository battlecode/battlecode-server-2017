package battlecode.world.signal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Overrides the default behavior for automatic signal discovery.
 *
 * @see AutoSignalHandler
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DiscoverSignal {
    boolean value() default true;
}
