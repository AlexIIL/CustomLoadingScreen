package alexiil.mc.mod.load;

import java.security.SecureRandom;
import java.util.Random;

import buildcraft.lib.expression.FunctionContext;

public final class ClsRandom {

    public static final FunctionContext CTX_RANDOM = new FunctionContext("Random numbers");

    private static final Random RAND = new Random();

    static {
        FunctionContext c = CTX_RANDOM;
        SecureRandom srand = new SecureRandom();

        c.put_l("generate_seed", () -> {
            synchronized (srand) {
                return srand.nextLong();
            }
        });

        c.put_l_d("random", (seed) -> random(seed, 0));
        c.put_ll_d("random", (seed, index) -> random(seed, index));
        c.put_ll_l("random_int", (seed, max) -> randomInt(seed, 0, max));
        c.put_lll_l("random_int", (seed, index, max) -> randomInt(seed, index, max));
    }

    private static double random(long seed, long index) {
        // No reason not to sync as setSeed is synchronized
        synchronized (RAND) {
            RAND.setSeed(seed ^ ((index & 0xFF_FF_FF_FF) << 16));
            return RAND.nextDouble();
        }
    }

    private static long randomInt(long seed, long index, long max) {
        if (max <= 1) {
            return 0;
        }
        return Math.min(max - 1, (int) (random(seed, index) * max));
    }
}
