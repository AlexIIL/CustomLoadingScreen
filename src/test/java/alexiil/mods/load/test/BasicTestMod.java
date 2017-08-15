package alexiil.mods.load.test;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "empty_test_mod_x")
public class BasicTestMod {

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        TestModHelper.preInit(this, event);
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        TestModHelper.preInit(this, event);
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        TestModHelper.postInit(this, event);
    }
}
