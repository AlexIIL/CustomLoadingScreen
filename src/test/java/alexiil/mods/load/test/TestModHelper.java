package alexiil.mods.load.test;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class TestModHelper {
    public static void preInit(TestModVars vars, FMLPreInitializationEvent event) {
        sleep(1000);
    }

    public static void init(TestModVars vars, FMLInitializationEvent event) {
        sleep(1000);
    }

    public static void postInit(TestModVars vars, FMLPostInitializationEvent event) {
        sleep(1000);
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }
}
