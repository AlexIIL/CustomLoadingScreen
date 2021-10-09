package alexiil.mc.mod.load.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

        public boolean hasDefault() {
            return this == EType.RENDERING_PART;
        }

        public JsonConfigurable<?, ?> getNotFound(String location) throws InvalidExpressionException {
            if (this == EType.RENDERING_PART) {
                JsonRender ji = getAsImage(location);
                if (ji != null) {
                    JsonRenderingPart jrp = new JsonRenderingPart(ji, new JsonInsn[0], "true");
                    jrp.setSource(
                        ("{#-'image':'" + location + "'#}").replace('\'', '"').replace('#', '\n').replace('-', '\t')
                    );
                    jrp.setLocation(ji.resourceLocation);
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
            .registerTypeAdapter(Area.class, Area.DESERIALISER)//
            // .registerTypeAdapter(JsonAction.class, ActionDeserialiser.INSTANCE)//
            .create();
        GSON_DEFAULT = new GsonBuilder().setPrettyPrinting().create();
    }

    private static String getFirst(ResourceLocation identifier, boolean firstAttempt, boolean hasDefaut) {
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
                    if (!hasDefaut) {
                        CLSLog.warn("Tried to get the resource but failed! (" + real + ") because " + e.getClass());
                    }
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
            if (firstAttempt && !hasDefaut) {
                CLSLog.warn("Tried to get the resource but failed! (" + identifier + ") because " + e.getClass());
            }
            return null;
        }
    }

    public static InputStream getInputStream(ResourceLocation identifier) throws FileNotFoundException {
        if (identifier == null) {
            throw new NullPointerException("Identifier provided shouldn't have been null!");
        }
        if ("config".equals(identifier.getResourceDomain())) {
            File file = new File("config/customloadingscreen", identifier.getResourcePath());
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException fnfe) {
                throw fnfe;
            } catch (IOException io) {
                FileNotFoundException fnfe = new FileNotFoundException();
                fnfe.initCause(io);
                throw fnfe;
            }
        }

        try {
            IResource res = ClsManager.getResource(identifier);

            // Wrap the resource
            return new ResourceWrappingInputStream(res);
        } catch (FileNotFoundException fnfe) {
            throw fnfe;
        } catch (IOException io) {
            FileNotFoundException fnfe = new FileNotFoundException();
            fnfe.initCause(io);
            throw fnfe;
        }
    }

    private static String getTextResource(ResourceLocation identifier, boolean hasDefaut) {
        if (identifier == null) throw new NullPointerException("Identifier provided shouldn't have been null!");
        if (cache.containsKey(identifier)) {
            return cache.get(identifier);
        }
        if (failedCache.containsKey(identifier)) {
            String attempt = getFirst(identifier, false, hasDefaut);
            if (attempt != null) {
                failedCache.remove(identifier);
                cache.put(identifier, attempt);
            }
            return attempt;
        }
        String actual = getFirst(identifier, true, hasDefaut);
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
            throw new JsonSyntaxException("Invalid location '" + location + "'");
        }
        CLSLog.info("Getting " + location + " as " + type);
        ResourceLocation loc = getLocation(type, location);
        String text = getTextResource(loc, type.hasDefault());
        if (text == null) {
            JsonConfigurable<?, ?> failed = type.getNotFound(location);
            if (failed != null) {
                failed.setLocation(loc);
                return (T) failed;
            }
            CLSLog.warn("The text inside of \"" + loc + "\" was null!");
            throw new JsonSyntaxException("Invalid location '" + location + "': the text inside it was null!");
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

        /* Namespace rules: */

        // If the location starts contains a colon before a slash then it's a namespace,
        // and we don't do anything special other that use it instead of the default.

        // If the location starts with "config/" then we use "config" as the namespace
        // and we *don't* add the type prefix if the type is EType.CONFIG.

        /* Path rules: */

        // We always append ".json" to the path, no exceptions
        // The other rules are a bit more complicated:

        // If it starts with "builtin/" or "sample/" then we bring that to the front.

        // Then we add "type"

        String namespace = "customloadingscreen";
        String path;
        int colon = base.indexOf(':');
        int slash = base.indexOf('/');

        if (colon > 0 && (colon < slash || slash < 0)) {
            namespace = base.substring(0, colon);
            base = base.substring(colon + 1);

            if ("config".equals(namespace) && type == EType.CONFIG) {
                path = base;
            } else {
                path = type.resourceBase + "/" + base;
            }
        } else {
            if (base.startsWith("builtin/")) {
                path = "builtin/" + type.resourceBase + base.substring(slash);
            } else if (base.startsWith("sample/")) {
                path = "sample/" + type.resourceBase + base.substring(slash);
            } else if (base.startsWith("config/")) {
                if (type == EType.CONFIG) {
                    path = base.substring(slash + 1);
                } else {
                    path = type.resourceBase + base.substring(slash);
                }
                namespace = "config";
            } else {
                path = type.resourceBase + "/" + base;
            }
        }

        return new ResourceLocation(namespace, path + ".json");
    }
}
