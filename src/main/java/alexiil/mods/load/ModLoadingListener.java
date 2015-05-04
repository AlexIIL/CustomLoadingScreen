package alexiil.mods.load;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import com.google.common.eventbus.Subscribe;

public class ModLoadingListener {
    public enum State {
        CONSTRUCT("construction"), PRE_INIT("pre_initialization"), LITE_LOADER_INIT("lite", true, true), INIT("initialization"), POST_INIT(
                "post_initialization"), LOAD_COMPLETE("completed"), FINAL_LOADING("reloading_resource_packs", true, false);

        private String translatedName = null;
        final String name;
        /** If this state is only called once. This is false for all except for FINAL_LOADING */
        final boolean isLoneState;
        /** If this is true, then ModStage.getNext will skip this, but it will still be included in the percentage
         * calculation */
        final boolean shouldSkip;

        State(String name, boolean mods, boolean skip) {
            isLoneState = mods;
            this.name = name;
            shouldSkip = skip;
        }

        State(String name) {
            this(name, false, false);
        }

        public String translate() {
            if (translatedName != null)
                return translatedName;
            String failure = name.replaceAll("_", " ");
            String[] split = failure.split(" ");
            failure = "";
            for (int i = 0; i < split.length; i++) {
                failure += i == 0 ? "" : " ";
                failure += split[i].substring(0, 1).toUpperCase().concat(split[i].substring(1));
            }
            translatedName = Translation.translate("betterloadingscreen.state." + name, failure);
            return translatedName;
        }
    }

    private static class ModStage {
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
                if (ord == State.values().length)
                    return null;
                s = State.values()[ord];
                if (s.shouldSkip)
                    return new ModStage(s, ind).getNext();
            }
            return new ModStage(s, ind);
        }

        public String getDisplayText() {
            if (state.isLoneState)
                return state.translate();
            return state.translate() + ": " + Translation.translate("betterloadingscreen.loading", "loading") + " "
                    + listeners.get(index).mod.getName();
        }

        public float getProgress() {
            float values = 100 / (float) State.values().length;
            float part = state.ordinal() * values;
            float size = listeners.size();
            float percent = values * index / size;
            return part + percent;
        }
    }

    private static List<ModLoadingListener> listeners = new ArrayList<ModLoadingListener>();
    private static ModStage stage = null;

    private final ModContainer mod;

    public ModLoadingListener(ModContainer mod) {
        this.mod = mod;
        if (listeners.isEmpty())
            MinecraftForge.EVENT_BUS.register(this);
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
        try {
            if (stage == null)
                if (mod == null)
                    stage = new ModStage(state, 0);
                else
                    stage = new ModStage(state, listeners.indexOf(mod));
            stage = stage.getNext();
            if (stage != null) {
                ProgressDisplayer.displayProgress(stage.getDisplayText(), stage.getProgress() / 100D);
            }
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
