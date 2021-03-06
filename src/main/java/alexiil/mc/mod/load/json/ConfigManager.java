package alexiil.mc.mod.load.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.CLSLog;
import alexiil.mc.mod.load.ClsManager;
import alexiil.mc.mod.load.json.JsonVariable.JsonConstant;
import alexiil.mc.mod.load.json.serial.ConfigDeserialiser;
import alexiil.mc.mod.load.json.serial.FactoryDeserialiser;
import alexiil.mc.mod.load.json.serial.ImageDeserialiser;
import alexiil.mc.mod.load.json.serial.InstructionDeserialiser;
import alexiil.mc.mod.load.json.serial.RenderingPartDeserialiser;
import alexiil.mc.mod.load.json.serial.VariableArrayDeserialiser;

import buildcraft.lib.expression.api.InvalidExpressionException;

public class ConfigManager {
    public enum EType {
        FACTORY(JsonFactory.class, "factory"),
        ACTION(JsonAction.class, "action"),
        RENDERING_PART(JsonRenderingPart.class, "imagemeta"),
        IMAGE(JsonRender.class, "image"),
        INSTRUCTION(JsonInsn.class, "instruction"),
        CONFIG(JsonConfig.class, "config");

        public final Class<? extends JsonConfigurable<?, ?>> clazz;
        public final String resourceBase;

        public static EType valueOf(Class<? extends JsonConfigurable<?, ?>> configurable) {
            return types.get(configurable);
        }

        <T extends JsonConfigurable<T, ?>> EType(Class<T> clazz, String resourceBase) {
            this.clazz = clazz;
            this.resourceBase = resourceBase;
            types.put(clazz, this);
        }

        public JsonConfigurable<?, ?> getNotFound(String location) throws InvalidExpressionException {
            if (this == EType.RENDERING_PART) {
                JsonRender ji = getAsImage(location);
                if (ji != null) {
                    JsonRenderingPart jrp = new JsonRenderingPart(ji, new JsonInsn[0], "true");
                    jrp.setSource(
                        ("{#-'image':'" + location + "'#}").replace('\'', '"').replace('#', '\n').replace('-', '\t')
                    );
                    return jrp;
                }
            }
            return null;
        }
    }

    public static final Gson GSON_ADAPTORS;
    public static final Gson GSON_DEFAULT;

    private static final Map<Class<? extends JsonConfigurable<?, ?>>, EType> types = Maps.newHashMap();
    private static IResourceManager resManager = Minecraft.getMinecraft().getResourceManager();
    private static final Map<ResourceLocation, String> cache = Maps.newHashMap(), failedCache = Maps.newHashMap();

    static {
        GSON_ADAPTORS = new GsonBuilder()//
            .registerTypeAdapter(JsonConfig.class, ConfigDeserialiser.INSTANCE)//
            .registerTypeAdapter(JsonRenderingPart.class, RenderingPartDeserialiser.INSTANCE)//
            .registerTypeAdapter(JsonRender.class, ImageDeserialiser.INSTANCE)//
            .registerTypeAdapter(JsonInsn.class, InstructionDeserialiser.INSTANCE)//
            .registerTypeAdapter(JsonVariable[].class, VariableArrayDeserialiser.VARIABLES)//
            .registerTypeAdapter(JsonConstant[].class, VariableArrayDeserialiser.CONSTANTS)//
            .registerTypeAdapter(JsonFactory.class, FactoryDeserialiser.INSTANCE)//
            // .registerTypeAdapter(JsonAction.class, ActionDeserialiser.INSTANCE)//
            .create();
        GSON_DEFAULT = new GsonBuilder().setPrettyPrinting().create();
    }

    private static String getFirst(ResourceLocation identifier, boolean firstAttempt) {
        if (identifier == null) {
            throw new NullPointerException("Identifier provided shouldn't have been null!");
        }
        if ("config".equals(identifier.getResourceDomain())) {
            File file = new File("config/customloadingscreen", identifier.getResourcePath());
            try (FileInputStream fis = new FileInputStream(file)) {
                return IOUtils.toString(fis, StandardCharsets.UTF_8);
            } catch (IOException e) {
                if (firstAttempt) {
                    
                    String real = file.toString();
                    try {
                        real = file.getCanonicalPath();
                    } catch (IOException io) {
                        // Ignore
                    }
                    CLSLog.warn("Tried to get the resource but failed! (" + real + ") because " + e.getClass());
                }
                return null;
            }
        }
        try (IResource res = ClsManager.getResource(identifier)) {
            try (InputStream stream = res.getInputStream()) {
                return IOUtils.toString(stream, StandardCharsets.UTF_8);
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
    /* For some reason, using <T extends JsonConfigurable<T, ?>> didn't compile. (But it did in eclipse? What?) */
    static <T extends JsonConfigurable<T, ?>> T getAsT(EType type, String location) throws InvalidExpressionException {
        if (StringUtils.isEmpty(location)) {
            CLSLog.warn("Location was given as null!", new Throwable());
            return null;
        }
        CLSLog.info("Getting " + location + " as " + type);
        ResourceLocation loc = getLocation(type, location);
        String text = getTextResource(loc);
        if (text == null) {
            JsonConfigurable<?, ?> failed = type.getNotFound(location);
            if (failed != null) {
                failed.setLocation(loc);
                return (T) failed;
            }
            CLSLog.warn("The text inside of \"" + loc + "\" was null!");
            return null;
        }
        try {
            T t = (T) GSON_ADAPTORS.fromJson(text, type.clazz);
            t.setLocation(loc);
            t.setSource(text);
            return t;
        } catch (JsonSyntaxException t) {
            throw new InvalidSourceException("Failed to read from " + loc + "\n" + text, t);
        }
    }

    /** Rendering parts act slightly differently: if they don't exist, but an image with the same name DOES, then use
     * the image and provide a default rendering part. */
    public static JsonRenderingPart getAsRenderingPart(String location) throws InvalidExpressionException {
        return getAsT(EType.RENDERING_PART, location);
    }

    public static JsonFactory getAsFactory(String location) throws InvalidExpressionException {
        return getAsT(EType.FACTORY, location);
    }

    public static JsonRender getAsImage(String location) throws InvalidExpressionException {
        return getAsT(EType.IMAGE, location);
    }

    public static JsonInsn getAsInsn(String location) throws InvalidExpressionException {
        return getAsT(EType.INSTRUCTION, location);
    }

    public static JsonAction getAsAction(String location) throws InvalidExpressionException {
        return getAsT(EType.ACTION, location);
    }

    public static JsonConfig getAsConfig(String location) throws InvalidExpressionException {
        return getAsT(EType.CONFIG, location);
    }

    public static void getAsScript(String location) {
        // TODO: Support for scripts that can do arbitrary things for displaying
        // Scripts should run once each tick for each thing they are associated with
        // (Scripts could just be for the config, for a specific image or more created by factories)
    }

    public static ResourceLocation getLocation(EType type, String base) {
        String path;
        if (base.startsWith("builtin/")) {
            path = "builtin/" + type.resourceBase + "/" + base.substring("builtin/".length()) + ".json";
        } else if (base.startsWith("sample/")) {
            path = "sample/" + type.resourceBase + "/" + base.substring("sample/".length()) + ".json";
        } else if (base.startsWith("config/")) {
            if (type == EType.CONFIG) {
                path = base.substring("config/".length()) + ".json";
            } else {
                path = type.resourceBase + "/" + base.substring("config/".length()) + ".json";
            }
            return new ResourceLocation("config", path);
        } else {
            path = type.resourceBase + "/" + base + ".json";
        }

        return new ResourceLocation("customloadingscreen", path);
    }
}
