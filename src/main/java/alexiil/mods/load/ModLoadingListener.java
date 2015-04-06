package alexiil.mods.load;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.google.common.eventbus.Subscribe;

public class ModLoadingListener {
    private enum State {
        CONSTRUCT("Construction"), PRE_INIT("Pre Initialization"), INIT("Initialization"), POST_INIT("Post Initialization"), LOAD_COMPLETE(
                "Completed"), FINAL_LOADING("Reloading Resource Packs");

        final String displayName;

        State(String name) {
            displayName = name;
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
            if (ind == listeners.size()) {
                ind = 0;
                int ord = s.ordinal() + 1;
                if (ord == State.values().length)
                    return null;
                s = State.values()[ord];
            }
            return new ModStage(s, ind);
        }

        public String getDisplayText() {
            if (state == State.FINAL_LOADING)
                return state.displayName;
            return state.displayName + ": loading " + listeners.get(index).mod.getName();
        }

        public int getProgress() {
            int values = 100 / State.values().length;
            int part = (int) (state.ordinal() * values);
            int size = listeners.size();
            int percent = values * index / size;
            return part + percent;
        }
    }

    private static List<ModLoadingListener> listeners = new ArrayList<ModLoadingListener>();
    private static ModStage stage = null;
    private static LoadingFrame frame = null;
    private static boolean hasFailed = false;

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

    @SubscribeEvent
    public void guiOpen(GuiOpenEvent event) {
        if (frame != null)
            frame.dispose();
    }

    private static void doProgress(State state, ModLoadingListener mod) {
        if (frame == null && !hasFailed) {
            frame = LoadingFrame.openWindow();
            if (frame == null) {
                hasFailed = true;
                System.out.println("Could not open the JFrame");
            }
        }
        if (frame != null) {
            if (stage == null)
                stage = new ModStage(state, listeners.indexOf(mod));
            stage = stage.getNext();
            if (stage != null && frame != null) {
                frame.setMessage(stage.getDisplayText());
                frame.setProgress(stage.getProgress());
                frame.repaint();
            }
        }
    }
}
