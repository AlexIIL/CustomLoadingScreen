package alexiil.mods.load.render;

import alexiil.mods.load.json.ConfigBase;
import alexiil.mods.load.json.JsonRenderingPart;

public class TextureAnimator {

    public TextureAnimator(ConfigBase images) {
        for (JsonRenderingPart render : images.render) {
            if (render.element.animated) {

            }
        }
    }

    public void tick() {
        // TODO: decide how to do this
    }
}
