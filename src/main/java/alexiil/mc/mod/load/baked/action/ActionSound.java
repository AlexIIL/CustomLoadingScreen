package alexiil.mc.mod.load.baked.action;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.baked.BakedAction;

import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;

public class ActionSound extends BakedAction {
    public static final SoundHandler sndHandler = Minecraft.getMinecraft().getSoundHandler();
    public final INodeObject<String> sound;
    public final INodeBoolean repeat;
    private ISound currentSound = null;

    public ActionSound(INodeBoolean conditionStart, INodeBoolean conditionEnd, INodeObject<String> sound, INodeBoolean repeat) {
        super(conditionStart, conditionEnd);
        this.sound = sound;
        this.repeat = repeat;
    }

    @Override
    protected void start() {
        ResourceLocation soundLocation = new ResourceLocation(sound.evaluate());
        // currentSound = PositionedSoundRecord.create(soundLocation);
        // sndHandler.playSound(currentSound);
        // TODO finish action sound!
    }

    @Override
    protected void tick() {
        if ((!sndHandler.isSoundPlaying(currentSound)) && repeat.evaluate() && !conditionEnd.evaluate()) {
            sndHandler.playSound(currentSound);
        }
    }

    @Override
    protected void end() {}
}
