package alexiil.mods.load;

import java.lang.reflect.Field;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import alexiil.mods.lib.AlexIILMod;

import com.google.common.eventbus.EventBus;

@Mod(modid = Lib.Mod.ID, guiFactory = "alexiil.mods.load.ConfigGuiFactory", dependencies = "required-after:alexiillib",
        acceptableRemoteVersions = "*")
public class BetterLoadingScreen extends AlexIILMod {
    @Instance(Lib.Mod.ID)
    public static BetterLoadingScreen instance;

    @Override
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        MinecraftForge.EVENT_BUS.register(instance);
    }

    @Override
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @EventHandler
    public void construct(FMLConstructionEvent event) {
        for (ModContainer mod : Loader.instance().getActiveModList()) {
            if (mod instanceof FMLModContainer) {
                EventBus bus = null;
                try {
                    // Its a bit questionable to be changing FML itself, but reflection is better than ASM transforming
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

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void guiOpen(GuiOpenEvent event) {
        if (event.gui != null && event.gui instanceof GuiMainMenu)
            ProgressDisplayer.close();
    }

    @EventHandler
    @SideOnly(Side.SERVER)
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        ProgressDisplayer.close();
    }

    @Override
    public String getCommitHash() {
        return Lib.Mod.COMMIT_HASH;
    }

    @Override
    public int getBuildType() {
        return Lib.Mod.buildType();
    }

    @Override
    public String getUser() {
        return "AlexIIL";
    }

    @Override
    public String getRepo() {
        return "BetterLoadingScreen";
    }
}
