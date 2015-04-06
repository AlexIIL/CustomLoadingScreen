package alexiil.mods.load;

import java.lang.reflect.Field;

import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;

import com.google.common.eventbus.EventBus;

@Mod(modid = "betterloadingscreen", guiFactory = "alexiil.mods.load.gui.ConfigGuiFactory")
public class BetterLoadingScreen {
    @EventHandler
    public void construct(FMLConstructionEvent event) {
        for (ModContainer mod : Loader.instance().getActiveModList()) {
            if (mod instanceof FMLModContainer) {
                EventBus bus = null;
                try {
                    // Its a bit questionable to be changing FML itself, but reflection is better that ASM transforming
                    // forge
                    Field f = FMLModContainer.class.getDeclaredField("eventBus");
                    f.setAccessible(true);
                    bus = (EventBus) f.get(mod);
                }
                catch (Throwable t) {
                    t.printStackTrace();
                }
                if (bus != null) {
                    bus.register(new ModLoadingListener(mod));
                }
            }
        }
    }
}
