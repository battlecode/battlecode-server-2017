package instrumentertest;

import java.util.Random;
import java.util.function.Supplier;

/**
 * @author james
 */
@SuppressWarnings("unused")
public class IllegalMethodReference {
    Supplier<Random> randomSupplier = Random::new;
}
