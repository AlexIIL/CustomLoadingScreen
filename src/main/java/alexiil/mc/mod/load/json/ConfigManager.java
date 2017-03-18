package alexiil.mc.mod.load.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.CLSLog;
import alexiil.mc.mod.load.ClsManager;
import alexiil.mc.mod.load.json.subtypes.JsonActionSound;
import alexiil.mc.mod.load.json.subtypes.JsonFactoryStatus;
import alexiil.mc.mod.load.json.subtypes.JsonImagePanorama;
import alexiil.mc.mod.load.json.subtypes.JsonImageText;

public class ConfigManager {
    public enum EType {
        FACTORY(JsonFactory.class, "factory"),
//        FUNCTION(JsonFunction.class, "function"),
        ACTION(JsonAction.class, "action"),
        RENDERING_PART(JsonRenderingPart.class, "imagemeta"),
        IMAGE(JsonImage.class, "image"),
        INSTRUCTION(JsonInstruction.class, "instruction"),
        CONFIG(JsonConfig.class, "config");

        public final Class<? extends JsonConfigurable<?, ?>> clazz;
        public final String resourceBase;
        public final LocationDeserialiser excluded;

        public static EType valueOf(Class<? extends JsonConfigurable<?, ?>> configurable) {
            return types.get(configurable);
        }

        <T extends JsonConfigurable<T, ?>> EType(Class<T> clazz, String resourceBase) {
            this.clazz = clazz;
            this.resourceBase = resourceBase;
            types.put(clazz, this);
            excluded = new LocationDeserialiser<>(this, clazz);
        }
    }

    private static final Map<Class<? extends JsonConfigurable<?, ?>>, EType> types = Maps.newHashMap();
    private static IResourceManager resManager = Minecraft.getMinecraft().getResourceManager();
    private static final Map<ResourceLocation, String> cache = Maps.newHashMap(), failedCache = Maps.newHashMap();

    /** Essentially, this 'cheats' the system to allow for loading a class with a deserialiser for all types EXCEPT the
     * one currently being loaded, which makes LocationDeserialiser easy to implement without doing each one
     * indervidualy. */
    public static Gson getGsonExcluding(Class<?> clazz) {
        GsonBuilder builder = new GsonBuilder();
        for (EType type : EType.values()) {
            if (clazz != type.clazz) {
                // We cannot really do much about this warning, as enums cannot have generic types
                builder.registerTypeAdapter(type.clazz, new LocationDeserialiser(type, clazz));
            }
        }
        return builder.create();
    }

    private static String getFirst(ResourceLocation identifier, boolean firstAttempt) {
        if (identifier == null) throw new NullPointerException("Identifier provided shouldn't have been null!");
        try (IResource res = ClsManager.getResource(identifier)) {
            try (InputStream stream = res.getInputStream()) {
                return IOUtils.toString(stream);
            } catch (IOException e) {
                CLSLog.warn("Tried to access \"" + identifier + "\", but an IO exception occoured!", e);
                return null;
            }
        } catch (IOException e) {
            if (firstAttempt) {
                CLSLog.warn("Tried to get the resource but failed! (" + identifier + ") because " + e.getClass());
            }
            return null;
        }
    }

    private static String getTextResource(ResourceLocation identifier) {
        if (identifier == null) throw new NullPointerException("Identifier provided shouldn't have been null!");
        if (cache.containsKey(identifier)) {
            return cache.get(identifier);
        }
        if (failedCache.containsKey(identifier)) {
            String attempt = getFirst(identifier, false);
            if (attempt != null) {
                failedCache.remove(identifier);
                cache.put(identifier, attempt);
            }
            return attempt;
        }
        String actual = getFirst(identifier, true);
        if (actual == null) failedCache.put(identifier, null);
        else cache.put(identifier, actual);
        return actual;
    }

    /** This makes the assumption that the type.clazz is the same as T or a subclass of T. Because this is a
     * package-protected function this is known and so it will NEVER throw a class cast exception. */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    /* For some reason, using <T extends JsonConfigurable<T, ?>> didn't compile. (But it did in eclipse? What?) */
    static <T extends JsonConfigurable> T getAsT(EType type, String location) {
        if (StringUtils.isEmpty(location)) {
            CLSLog.warn("Location was given as null!", new Throwable());
            return null;
        }
        CLSLog.info("Getting " + location + " as " + type);
        ResourceLocation loc = getLocation(type, location);
        String text = getTextResource(loc);
        if (text == null) {
            CLSLog.warn("The text inside of \"" + loc + "\" was null!");
            return null;
        }
        T t = (T) getGsonExcluding(type.clazz).fromJson(text, type.clazz);
        t.resourceLocation = loc;
        CLSLog.info(text);
        return t;
    }

    /** Rendering parts act slightly differently: if they don't exist, but an image with the same name DOES, then use
     * the image and provide a default rendering part. */
    public static JsonRenderingPart getAsRenderingPart(String location) {
        JsonRenderingPart jrp = getAsT(EType.RENDERING_PART, location);
        if (jrp == null) {
            JsonImage ji = getAsImage(location);
            if (ji != null) {
                jrp = new JsonRenderingPart(location, new String[0], "true", "");
                jrp.resourceLocation = getLocation(EType.RENDERING_PART, location);
            } else throw new NullPointerException("Neither the imagemeta, nor an image was found for " + location);
        }
        return jrp;
    }

    public static JsonFactory getAsFactory(String location) {
        if (isBuiltIn(location)) {
            if (location.equalsIgnoreCase("builtin/new_status")) {
                return new JsonFactoryStatus();
            }
        }
        return getAsT(EType.FACTORY, location);
    }

    public static JsonImage getAsImage(String location) {
        if (isBuiltIn(location)) {
            if (location.equalsIgnoreCase("builtin/text")) return new JsonImageText(getLocation(EType.IMAGE, location), "textures/font/ascii.png", null, null, null, null, null, null);
            else if (location.equalsIgnoreCase("builtin/panorama")) return new JsonImagePanorama(getLocation(EType.IMAGE, location), "textures/gui/title/background/panorama_x.png");
        }
        return getAsT(EType.IMAGE, location);
    }

    public static JsonInstruction getAsInsn(String location) {
        return getAsT(EType.INSTRUCTION, location);
    }

    public static JsonAction getAsAction(String location) {
        if (isBuiltIn(location)) {
            if (location.equalsIgnoreCase("builtin/sound")) return new JsonActionSound(getLocation(EType.ACTION, location), null, null, null);
        }
        return getAsT(EType.ACTION, location);
    }

//    public static JsonFunction getAsFunction(String location) {
//        if (location == null) {
//            CLSLog.warn("Location was given as null!", new Throwable());
//            return null;
//        }
//        ResourceLocation loc = getLocation(EType.FUNCTION, location);
//        String text = getTextResource(loc);
//        JsonFunction t = new Gson().fromJson(text, JsonFunction.class);
//        t.resourceLocation = loc;
//        return t;
//    }

    public static JsonConfig getAsConfig(String location) {
        return getAsT(EType.CONFIG, location);
    }

    public static void getAsScript(String location) {
        // TODO: Support for scripts that can do arbitrary things for displaying
        // Scripts should run once each tick for each thing they are associated with
        // (Scripts could just be for the config, for a specific image or more created by factories)
    }

    public static ResourceLocation getLocation(EType type, String base) {
        String path;
        if (StringUtils.startsWith(base, "builtin/")) {
            path = "builtin/" + type.resourceBase + "/" + base.substring("builtin/".length()) + ".json";
        } else if (base.startsWith("sample/")) {
            path = "sample/" + type.resourceBase + "/" + base.substring("sample/".length()) + ".json";
        } else {
            path = "custom/" + type.resourceBase + "/" + base + ".json";
        }

        return new ResourceLocation("customloadingscreen", path);
    }

    private static boolean isBuiltIn(String location) {
        return StringUtils.startsWith(location, "builtin/");
    }
}
