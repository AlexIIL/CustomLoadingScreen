package alexiil.mods.load.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;
import com.google.gson.Gson;

import alexiil.mods.load.BLSLog;

public class ConfigManager {
    public enum EType {
        FACTORY(JsonFactory.class, "factory"), FUNCTION(JsonFunction.class, "function"), ACTION(JsonAction.class, "action"), RENDERING_PART(
                JsonRenderingPart.class, "imagemeta"), IMAGE(JsonImage.class, "image"), INSTRUCTION(JsonInstruction.class, "instruction"), CONFIG(
                JsonConfig.class, "config");

        public final Class<?> clazz;
        public final String resourceBase;

        EType(Class<?> clazz, String resourceBase) {
            this.clazz = clazz;
            this.resourceBase = resourceBase;
        }
    }

    private static IResourceManager resManager = Minecraft.getMinecraft().getResourceManager();
    private static final Map<ResourceLocation, String> cache = Maps.newHashMap(), failedCache = Maps.newHashMap();

    private static String getFirst(ResourceLocation res, boolean firstAttempt) {
        if (res == null) {
            NullPointerException npe = new NullPointerException("Resource provided shouldn't have been null!");
            BLSLog.warn("", npe);
            return null;
        }
        IResource resource;
        try {
            resource = resManager.getResource(res);
        }
        catch (IOException e) {
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
        }
        catch (IOException e) {
            BLSLog.warn("Tried to access \"" + res + "\", but an IO exception occoured!", e);
        }
        return null;
    }

    private static String getTextResource(ResourceLocation res) {
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

    /** This makes the assumption that the type.clazz is the same as T or a subclass of T. Because this is a private
     * function, this is known and so it will NEVER throw a class cast exception. */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    /* For some reason, using <T extends JsonConfigurable<T, ?>> didn't compile. (But it did in eclipse? What?) */
    private static <T extends JsonConfigurable> T getAsT(EType type, String location) {
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
        T t = (T) new Gson().fromJson(text, type.clazz);
        t.resourceLocation = loc;
        return t;
    }

    /** Rendering parts act slightly differently: if they don't exist, but an image with the same name DOES, then use the
     * image and provide a default rendering part. */
    public static JsonRenderingPart getAsRenderingPart(String location) {
        JsonRenderingPart jrp = getAsT(EType.RENDERING_PART, location);
        if (jrp == null) {
            JsonImage ji = getAsImage(location);
            if (ji != null) {
                jrp = new JsonRenderingPart(location, new JsonInstruction[0], "true", "");
            }
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
                return new JsonImageText("textures/font/ascii.png", null, null, null, null, null, null);
            else if (location.equalsIgnoreCase("builtin/panorama"))
                return null;// new JsonImagePanorama(null,null,null,null);
        }
        return getAsT(EType.IMAGE, location);
    }

    public static JsonInstruction getAsInsn(String location) {
        return getAsT(EType.INSTRUCTION, location);
    }

    public static JsonAction getAsAction(String location) {
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

    public static ResourceLocation getLocation(EType type, String base) {
        String path;
        if (StringUtils.startsWith(base, "builtin/")) {
            path = "builtin/" + type.resourceBase + "/" + base.substring("builtin/".length()) + ".json";
        }
        else if (base.startsWith("sample/")) {
            path = "sample/" + type.resourceBase + "/" + base.substring("sample/".length()) + ".json";
        }
        else {
            path = "custom/" + type.resourceBase + "/" + base + ".json";
        }

        return new ResourceLocation("betterloadingscreen", path);
    }

    private static boolean isBuiltIn(String location) {
        return StringUtils.startsWith(location, "builtin/");
    }
}
