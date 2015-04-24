package alexiil.mods.load.baked;

import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.baked.func.IBakedFunction;
import alexiil.mods.load.render.RenderingStatus;

public class BakedTextRenderStatic extends BakedTextRender {
    private final IBakedFunction<String> text;

    public BakedTextRenderStatic(IBakedFunction<String> text, IBakedFunction<Double> x, IBakedFunction<Double> y, IBakedFunction<Double> colour,
            String fontTexture) {
        super(x, y, colour, fontTexture);
        this.text = text;
    }

    @Override
    public String getText(RenderingStatus status) throws FunctionException {
        return text.call(status);
    }
}
