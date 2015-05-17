package alexiil.mods.load.test;

import java.util.List;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import alexiil.mods.load.BLSLog;
import alexiil.mods.load.baked.func.BakedFunction;
import alexiil.mods.load.json.JsonFunction;
import alexiil.mods.load.render.RenderingStatus;

public class TestMain {
    public static void main(String[] args) {
        testFunctions();

        TestModExporter.dumpMods();
        // GradleStart.main(args);
    }

    private static void testFunctions() {
        Map<String, BakedFunction<?>> functions = Maps.newHashMap();

        List<JsonFunction> temp = Lists.newArrayList();
        temp.add(new JsonFunction("operators", "1+2*4-7/2", new String[0]));
        temp.add(new JsonFunction("brackets", "(1+4)*4", new String[0]));
        temp.add(new JsonFunction("caller", "operators+brackets", new String[0]));
        temp.add(new JsonFunction("function", "one - two", new String[] { "one", "two" }));
        temp.add(new JsonFunction("argument", "function(1,2,3)-6", new String[0]));

        RenderingStatus status = new RenderingStatus(1, 1);

        for (JsonFunction func : temp) {
            try {
                func.resourceLocation = new ResourceLocation("betterloadingscreen", func.name);
                BakedFunction<?> bf = func.bake(functions);
                functions.put(func.name, bf);

                Object o = null;
                if (bf.numArgs() > 0)
                    o = bf.call(status, 0D, 1D);
                else
                    o = bf.call(status);
                BLSLog.info(func.function + " = " + o);
            }
            catch (Throwable t) {
                BLSLog.warn(func.name + " failed because ", t);
                throw new Error("Tests failed");
            }

        }
    }
}
