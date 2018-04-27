package alexiil.mc.mod.load;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ModLoadingListener {
    public enum LoaderStage {
        CONSTRUCT("construction", 0),
        PRE_INIT("pre_initialization", 0),
        LITE_LOADER_INIT("lite", true, true, 0),
        INIT("initialization", 0),
        POST_INIT("post_initialization", 1),
        LOAD_COMPLETE("completed", 1);

        public final boolean isAfterReload1;

        private String translatedName = null;
        final String name;
        /** If this state is only called once. This is false for all except for FINAL_LOADING */
        final boolean isLoneState;
        /** If this is true, then ModStage.getNext will skip this, but it will still be included in the percentage
         * calculation */
        final boolean shouldSkip;

        LoaderStage(String name, boolean loneState, boolean skip, int state) {
            isAfterReload1 = state > 0;
            isLoneState = loneState;
            this.name = name;
            shouldSkip = skip;
        }

        LoaderStage(String name, int state) {
            this(name, false, false, state);
        }

        public String translate() {
            if (translatedName != null) return translatedName;
            translatedName = Translation.translate("customloadingscreen.modstate." + name);
            return translatedName;
        }
    }

    public static class ModStage {
        public final LoaderStage state;

        @Override
        public String toString() {
            return "ModStage [state=" + state + ", index=" + index + "]";
        }

        public final int index;

        public ModStage(LoaderStage state, int index) {
            this.state = state;
            this.index = index;
        }

        public ModStage getNext() {
            int ind = index + 1;
            LoaderStage s = state;
            if (ind == listeners.size() || s.isLoneState) {
                ind = 0;
                int ord = s.ordinal() + 1;
                if (ord == LoaderStage.values().length) return this;
                s = LoaderStage.values()[ord];
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

        /** @return A number between 0 and max */
        public int getSubProgress(int max) {
            return index * max / listeners.size();
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
    public static final List<String> modIds = new ArrayList<>();
    private static final List<ModLoadingListener> listeners = new ArrayList<>();

    private final ModContainer mod;

    public ModLoadingListener(ModContainer mod) {
        this.mod = mod;
        modIds.add(mod.getModId());
        listeners.add(this);
    }

    @Subscribe
    public void construct(FMLConstructionEvent event) {
        doProgress(LoaderStage.CONSTRUCT, this);
    }

    @Subscribe
    public void preinit(FMLPreInitializationEvent event) {
        doProgress(LoaderStage.PRE_INIT, this);
    }

    @Subscribe
    public void init(FMLInitializationEvent event) {
        doProgress(LoaderStage.INIT, this);
    }

    @Subscribe
    public void postinit(FMLPostInitializationEvent event) {
        doProgress(LoaderStage.POST_INIT, this);
    }

    @Subscribe
    public void loadComplete(FMLLoadCompleteEvent event) {
        doProgress(LoaderStage.LOAD_COMPLETE, this);
    }

    private static void doProgress(LoaderStage state, ModLoadingListener mod) {
        ModStage ms = stage;
        if (ms == null) {
            if (mod == null) {
                ms = new ModStage(state, 0);
            } else {
                ms = new ModStage(state, listeners.indexOf(mod));
            }
        }
        stage = ms.getNext();
        if (true) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException ignored) {
                // NO_OP
            }
        }
    }
}
