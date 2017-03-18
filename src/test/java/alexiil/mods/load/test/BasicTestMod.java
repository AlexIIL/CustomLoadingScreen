package alexiil.mods.load.test;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "empty_test_mod_x")
public class BasicTestMod {

    public static final TestModVars variables = new TestModVars();

    @Mod.EventHandler
    public static void onPreInit(FMLPreInitializationEvent event) {
        TestModHelper.preInit(variables, event);
    }

    @Mod.EventHandler
    public static void onInit(FMLInitializationEvent event) {
        TestModHelper.init(variables, event);
    }

    @Mod.EventHandler
    public static void onPostInit(FMLPostInitializationEvent event) {
        TestModHelper.postInit(variables, event);
    }
}
