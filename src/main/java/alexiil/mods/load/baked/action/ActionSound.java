package alexiil.mods.load.baked.action;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;

import alexiil.mods.load.baked.BakedAction;
import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.baked.func.IBakedFunction;
import alexiil.mods.load.render.RenderingStatus;

public class ActionSound extends BakedAction {
    public static final SoundHandler sndHandler = Minecraft.getMinecraft().getSoundHandler();
    public final IBakedFunction<String> sound;
    public final IBakedFunction<Boolean> repeat;
    private ISound currentSound = null;

    public ActionSound(IBakedFunction<Boolean> conditionStart, IBakedFunction<Boolean> conditionEnd, IBakedFunction<String> sound,
            IBakedFunction<Boolean> repeat) {
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
