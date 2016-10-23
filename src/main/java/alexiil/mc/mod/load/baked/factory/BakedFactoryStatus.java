package alexiil.mc.mod.load.baked.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;

import alexiil.mc.mod.load.baked.BakedFactory;
import alexiil.mc.mod.load.baked.BakedRenderingPart;
import alexiil.mc.mod.load.event.StatusPushedEvent;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeBoolean;
import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

public class BakedFactoryStatus extends BakedFactory {
    /** This can be called by any thread to add an event to the list */
    private List<StatusPushedEvent> events = Collections.synchronizedList(new ArrayList<StatusPushedEvent>());

    public BakedFactoryStatus(INodeBoolean shouldDestroy, BakedRenderingPart component) {
        super(null, shouldDestroy, component);
    }

    @Override
    public void tick(MinecraftDisplayerRenderer renderer) {
        List<StatusPushedEvent> tempList = Lists.newArrayList();
        /* Okay, this isn't the cleanest concurrent code, but what should this be? A concurrent linked Queue? However I
         * don't really mind if I miss the event by one tick (isEmpty returns true, then in the main thread
         * onStatusPushed() is called, then it missed it). Would it be faster other ways? */
        while (!events.isEmpty())
            tempList.add(events.remove(0));

        while (!tempList.isEmpty()) {
            StatusPushedEvent spe = tempList.remove(0);
            FactoryElementStatus fes = new FactoryElementStatus(this, spe);
            renderer.elements.add(fes);
        }
    }

    @Subscribe
    public void onStatusPushed(StatusPushedEvent event) {
        events.add(event);
    }
}
