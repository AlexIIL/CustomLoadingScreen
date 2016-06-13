package alexiil.mc.mod.load.baked.action;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.baked.BakedAction;
import alexiil.mc.mod.load.baked.func.BakedFunction;
import alexiil.mc.mod.load.baked.func.FunctionException;
import alexiil.mc.mod.load.render.RenderingStatus;

public class ActionSound extends BakedAction {
    public static final SoundHandler sndHandler = Minecraft.getMinecraft().getSoundHandler();
    public final BakedFunction<String> sound;
    public final BakedFunction<Boolean> repeat;
    private ISound currentSound = null;

    public ActionSound(BakedFunction<Boolean> conditionStart, BakedFunction<Boolean> conditionEnd, BakedFunction<String> sound,
            BakedFunction<Boolean> repeat) {
        super(conditionStart, conditionEnd);
        this.sound = sound;
        this.repeat = repeat;
    }

    @Override
    protected void start(RenderingStatus status) throws FunctionException {
        ResourceLocation soundLocation = new ResourceLocation(sound.call(status));
        currentSound = PositionedSoundRecord.create(soundLocation);
        sndHandler.playSound(currentSound);
    }

    @Override
    protected void tick(RenderingStatus status) throws FunctionException {
        if ((!sndHandler.isSoundPlaying(currentSound)) && repeat.call(status) && !conditionEnd.call(status)) {
            sndHandler.playSound(currentSound);
        }
    }

    @Override
    protected void end(RenderingStatus status) throws FunctionException {}
}
