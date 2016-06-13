package alexiil.mc.mod.load.baked.render;

import alexiil.mc.mod.load.baked.func.BakedFunction;
import alexiil.mc.mod.load.baked.func.FunctionException;
import alexiil.mc.mod.load.render.RenderingStatus;

public class BakedTextRenderStatic extends BakedTextRender {
    private final BakedFunction<String> text;

    public BakedTextRenderStatic(BakedFunction<String> text, BakedFunction<Double> x, BakedFunction<Double> y, BakedFunction<Double> colour,
            String fontTexture) {
        super(x, y, colour, fontTexture);
        this.text = text;
    }

    @Override
    public String getText(RenderingStatus status) throws FunctionException {
        return text.call(status);
    }
}
