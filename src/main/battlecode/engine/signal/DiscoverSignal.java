package battlecode.engine.signal;

/**
 * Overrides the default behavior for automatic signal discovery.
 *
 * @see AutoSignalHandler
 */
public @interface DiscoverSignal {
    boolean value() default true;
}
