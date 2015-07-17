package alexiil.mods.load.json;

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

import alexiil.mods.load.BLSLog;
import alexiil.mods.load.json.subtypes.JsonActionSound;
import alexiil.mods.load.json.subtypes.JsonFactoryStatus;
import alexiil.mods.load.json.subtypes.JsonImagePanorama;
import alexiil.mods.load.json.subtypes.JsonImageText;

public class ConfigManager {
    public enum EType {
        FACTORY(JsonFactory.class, "factory"),
        FUNCTION(JsonFunction.class, "function"),
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
            excluded = new LocationDeserialiser<T>(this, clazz);
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
        BLSLog.info("Getting Json Excluding " + clazz);
        for (EType type : EType.values()) {
            if (clazz != type.clazz) {
                // We cannot really do much about this warning, as enums cannot have generic types
                builder.registerTypeAdapter(type.clazz, new LocationDeserialiser(type, clazz));
                BLSLog.info("  - " + type.clazz);
            }
        }
        return builder.create();
    }

    private static String getFirst(ResourceLocation res, boolean firstAttempt) {
        if (res == null) {
            NullPointerException npe = new NullPointerException("Resource provided shouldn't have been null!");
            BLSLog.warn("", npe);
            return null;
        }
        IResource resource;
        try {
            resource = resManager.getResource(res);
        } catch (IOException e) {
            if (firstAttempt) {
                BLSLog.warn("Tried to get the resource but failed! (" + res + ") because " + e.getClass());
            }
            return null;
        }
        if (resource == null) {
            if (firstAttempt) {
                BLSLog.warn("Tried to access \"" + res + "\", but the resource was null! (Does it even exist?)");
            }
            return null;
        }
        InputStream stream = resource.getInputStream();
        if (stream == null) {
            if (firstAttempt) {
                BLSLog.warn("Tried to access \"" + res + "\", but the resulting stream was null!");
            }
            return null;
        }

        try {
            return IOUtils.toString(stream);
        } catch (IOException e) {
            BLSLog.warn("Tried to access \"" + res + "\", but an IO exception occoured!", e);
        }
        return null;
    }

    private static String getTextResource(ResourceLocation res) {
        if (res == null)
            throw new NullPointerException("Cannot (and should not) get a null resource!");
        if (cache.containsKey(res)) {
            return cache.get(res);
        }
        if (failedCache.containsKey(res)) {
            String attempt = getFirst(res, false);
            if (attempt != null) {
                failedCache.remove(res);
                cache.put(res, attempt);
            }
            return attempt;
        }
        String actual = getFirst(res, true);
        if (actual == null)
            failedCache.put(res, null);
        else
            cache.put(res, actual);
        return actual;
    }

    /** This makes the assumption that the type.clazz is the same as T or a subclass of T. Because this is a
     * package-protected function, this is known and so it will NEVER throw a class cast exception. */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    /* For some reason, using <T extends JsonConfigurable<T, ?>> didn't compile. (But it did in eclipse? What?) */
    static <T extends JsonConfigurable> T getAsT(EType type, String location) {
        if (StringUtils.isEmpty(location)) {
            BLSLog.warn("Location was given as null!", new Throwable());
            return null;
        }
        ResourceLocation loc = getLocation(type, location);
        String text = getTextResource(loc);
        if (text == null) {
            BLSLog.warn("The text inside of \"" + loc + "\" was null!");
            return null;
        }
        T t = (T) getGsonExcluding(type.clazz).fromJson(text, type.clazz);
        t.resourceLocation = loc;
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
            } else
                throw new NullPointerException("Neither the imagemeta, nor an image was found for " + location);
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
            if (location.equalsIgnoreCase("builtin/text"))
                return new JsonImageText(getLocation(EType.IMAGE, location), "textures/font/ascii.png", null, null, null, null, null, null);
            else if (location.equalsIgnoreCase("builtin/panorama"))
                return new JsonImagePanorama(getLocation(EType.IMAGE, location), "textures/gui/title/background/panorama_x.png");
        }
        return getAsT(EType.IMAGE, location);
    }

    public static JsonInstruction getAsInsn(String location) {
        return getAsT(EType.INSTRUCTION, location);
    }

    public static JsonAction getAsAction(String location) {
        if (isBuiltIn(location)) {
            if (location.equalsIgnoreCase("builtin/sound"))
                return new JsonActionSound(getLocation(EType.ACTION, location), null, null, null);
        }
        return getAsT(EType.ACTION, location);
    }

    public static JsonFunction getAsFunction(String location) {
        if (location == null) {
            BLSLog.warn("Location was given as null!", new Throwable());
            return null;
        }
        ResourceLocation loc = getLocation(EType.FUNCTION, location);
        String text = getTextResource(loc);
        JsonFunction t = new Gson().fromJson(text, JsonFunction.class);
        t.resourceLocation = loc;
        return t;
    }

    public static JsonConfig getAsConfig(String location) {
        return getAsT(EType.CONFIG, location);
    }

    public static void getAsScript(String location) {
        // TODO: Support for LuaJ scripts that can do arbitrary things for displaying
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

        return new ResourceLocation("betterloadingscreen", path);
    }

    private static boolean isBuiltIn(String location) {
        return StringUtils.startsWith(location, "builtin/");
    }
}
