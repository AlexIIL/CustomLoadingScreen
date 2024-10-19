package alexiil.mc.mod.load.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.ITickableTextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.CLSLog;
import alexiil.mc.mod.load.CustomLoadingScreen;

public class TextureManagerCLS extends TextureManager {

    /** Map of texture to last access tick. */
    private final Map<ResourceLocation, Long> textures = new HashMap<>();
    // If we're forced to put a full object into the map value, we may as well intern them by only creating them once
    private Long currentTime = System.currentTimeMillis();

    public TextureManagerCLS(IResourceManager resourceManager) {
        super(resourceManager);
    }

    private void onTextureAccess(ResourceLocation resource) {
        textures.put(resource, currentTime);
    }

    @Override
    public void bindTexture(ResourceLocation resource) {
        super.bindTexture(resource);
        onTextureAccess(resource);
    }

    @Override
    public ITextureObject getTexture(ResourceLocation resource) {
        ITextureObject obj = super.getTexture(resource);
        if (obj != null) {
            onTextureAccess(resource);
        }
        return obj;
    }

    @Override
    public void deleteTexture(ResourceLocation textureLocation) {
        super.deleteTexture(textureLocation);
        textures.remove(textureLocation);
    }

    @Override
    public boolean loadTexture(ResourceLocation textureLocation, ITextureObject textureObj) {
        onTextureAccess(textureLocation);
        return super.loadTexture(textureLocation, textureObj);
    }

    @Override
    public boolean loadTickableTexture(ResourceLocation textureLocation, ITickableTextureObject textureObj) {
        onTextureAccess(textureLocation);
        return super.loadTickableTexture(textureLocation, textureObj);
    }

    public void deleteAll() {
        for (ResourceLocation location : textures.keySet().toArray(new ResourceLocation[0])) {
            deleteTexture(location);
        }
    }

    public void onFrame() {
        if (CustomLoadingScreen.textureClearInterval == 0) {
            return;
        }

        Long last = currentTime;
        long next = System.currentTimeMillis();

        if (last + 1000 < next) {
            return;
        }

        currentTime = next;

        long minTime = currentTime - (CustomLoadingScreen.textureClearInterval * 1000);

        List<ResourceLocation> toRemove = new ArrayList<>();

        for (Map.Entry<ResourceLocation, Long> entry : textures.entrySet()) {
            if (entry.getValue() < minTime) {
                toRemove.add(entry.getKey());
            }
        }

        for (ResourceLocation tex : toRemove) {
            if (CustomLoadingScreen.debugResourceLoading) {
                CLSLog.info("[debug] Automatically deleting texture " + tex);
            }
            deleteTexture(tex);
        }
    }
}
