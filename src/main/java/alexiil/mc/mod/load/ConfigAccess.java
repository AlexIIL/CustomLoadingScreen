package alexiil.mc.mod.load;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ConfigAccess {
    public interface IConfigurableMod {
        String modId();
    }

    private static final String CATEGORY = Configuration.CATEGORY_GENERAL;
    private static final Map<String, ConfigAccess> configs = Maps.newHashMap();

    private final Configuration cfg;
    private boolean isOpen = false;
    private IConfigurableMod mod;

    public static ConfigAccess get(File file, IConfigurableMod mod) {
        String canonical;
        try {
            canonical = file.getCanonicalPath();
        } catch (IOException e) {
            BLSLog.warn("Failed to get the canonical path from " + file.getAbsolutePath(), e);
            canonical = file.getAbsolutePath();
        }

        if (configs.containsKey(canonical)) {
            ConfigAccess ca = configs.get(canonical);
            if (ca.mod == null) ca.mod = mod;
            BLSLog.info("Loaded the existing config for " + canonical);
            return ca;
        }
        ConfigAccess ca = new ConfigAccess(file, mod);
        configs.put(canonical, ca);
        BLSLog.info("Created a new config for " + canonical);
        return ca;
    }

    private ConfigAccess(File file, IConfigurableMod mod) {
        cfg = new Configuration(file);
        this.mod = mod;
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void onConfig(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (isOpen && mod.modId().equals(event.modID)) saveAll();
    }

    public void saveAll() {
        if (isOpen && cfg.hasChanged()) cfg.save();
    }

    public Configuration cfg() {
        if (!isOpen) {
            cfg.load();
            isOpen = true;
        }
        return cfg;
    }

    public Property getProp(String key, boolean defaultValue) {
        return cfg().get(CATEGORY, key, defaultValue);
    }

    public Property getProp(String key, int defaultValue) {
        return cfg().get(CATEGORY, key, defaultValue);
    }

    public Property getProp(String key, byte defaultValue) {
        return cfg().get(CATEGORY, key, defaultValue);
    }

    public Property getProp(String key, double defaultValue) {
        return cfg().get(CATEGORY, key, defaultValue);
    }

    public Property getProp(String key, short defaultValue) {
        return cfg().get(CATEGORY, key, defaultValue);
    }

    public Property getProp(String key, String defaultValue) {
        return cfg().get(CATEGORY, key, defaultValue);
    }
}
