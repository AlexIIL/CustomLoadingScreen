package alexiil.mc.mod.load;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.*;

public class ModLoadingListener {
    public enum State {
        CONSTRUCT("construction", 0),
        PRE_INIT("pre_initialization", 0),
        LITE_LOADER_INIT("lite", true, true, 0),
        INIT("initialization", 0),
        POST_INIT("post_initialization", 1),
        LOAD_COMPLETE("completed", 1);

        public final boolean isAfterReload1, isAfterReload2;

        private String translatedName = null;
        final String name;
        /** If this state is only called once. This is false for all except for FINAL_LOADING */
        final boolean isLoneState;
        /** If this is true, then ModStage.getNext will skip this, but it will still be included in the percentage
         * calculation */
        final boolean shouldSkip;

        State(String name, boolean loneState, boolean skip, int state) {
            isAfterReload1 = state > 0;
            isAfterReload2 = state > 1;
            isLoneState = loneState;
            this.name = name;
            shouldSkip = skip;
        }

        State(String name, int state) {
            this(name, false, false, state);
        }

        public String translate() {
            if (translatedName != null) return translatedName;
            translatedName = Translation.translate("customloadingscreen.modstate." + name);
            return translatedName;
        }
    }

    public static class ModStage {
        public final State state;

        @Override
        public String toString() {
            return "ModStage [state=" + state + ", index=" + index + "]";
        }

        public final int index;

        public ModStage(State state, int index) {
            this.state = state;
            this.index = index;
        }

        public ModStage getNext() {
            int ind = index + 1;
            State s = state;
            if (ind == listeners.size() || s.isLoneState) {
                ind = 0;
                int ord = s.ordinal() + 1;
                if (ord == State.values().length) return this;
                s = State.values()[ord];
                if (s.shouldSkip) {
                    return new ModStage(s, ind).getNext();
                }
            }
            return new ModStage(s, ind);
        }

        public String getDisplayText() {
            return state.translate();
        }

        public String getSubDisplayText() {
            if (state.isLoneState) return "";
            return listeners.get(index).mod.getName();
        }

        /** @return A number between 0 and 1000 */
        public int getSubProgress() {
            return index * 1000 / listeners.size();
        }

        @Deprecated
        public int getProgress() {
            int values = 1000 / State.values().length;
            int part = state.ordinal() * values;
            int size = listeners.size();
            int percent = values * index / size;
            return part + percent;
        }
    }

    public static void setup() {
        for (ModContainer mod : Loader.instance().getActiveModList()) {
            if (mod instanceof FMLModContainer) {
                EventBus bus = null;
                try {
                    // Its a bit questionable to be changing FML itself, but reflection is better than ASM transforming
                    // forge
                    Field f = FMLModContainer.class.getDeclaredField("eventBus");
                    f.setAccessible(true);
                    bus = (EventBus) f.get(mod);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                if (bus != null) {
                    bus.register(new ModLoadingListener(mod));
                }
            }
        }
    }

    public static volatile ModStage stage = null;
    private static List<ModLoadingListener> listeners = new ArrayList<>();

    private final ModContainer mod;

    public ModLoadingListener(ModContainer mod) {
        this.mod = mod;
        listeners.add(this);
    }

    @Subscribe
    public void construct(FMLConstructionEvent event) {
        doProgress(State.CONSTRUCT, this);
    }

    @Subscribe
    public void preinit(FMLPreInitializationEvent event) {
        doProgress(State.PRE_INIT, this);
    }

    @Subscribe
    public void init(FMLInitializationEvent event) {
        doProgress(State.INIT, this);
    }

    @Subscribe
    public void postinit(FMLPostInitializationEvent event) {
        doProgress(State.POST_INIT, this);
    }

    @Subscribe
    public void loadComplete(FMLLoadCompleteEvent event) {
        doProgress(State.LOAD_COMPLETE, this);
    }

    private static void doProgress(State state, ModLoadingListener mod) {
        ModStage ms = stage;
        if (ms == null) {
            if (mod == null) {
                ms = new ModStage(state, 0);
            } else {
                ms = new ModStage(state, listeners.indexOf(mod));
            }
        }
        stage = ms.getNext();
        try {
            Thread.sleep(1);
        } catch (InterruptedException t) {
            t.printStackTrace();
        }
    }
}
