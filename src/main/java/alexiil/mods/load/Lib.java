package alexiil.mods.load;

public class Lib {
    public static class Mod {
        public static final String ID = "betterloadingscreen";
        public static final String NAME = "Better Loading Screen";
        public static final String VERSION = "@VERSION@";
        public static final String COMMIT_HASH = "@COMMIT_HASH@";

        public static int buildType() {
            if (COMMIT_HASH.startsWith("@"))
                return 0;
            if (COMMIT_HASH.startsWith("manual "))
                return 1;
            return 2;
        }
    }
}
