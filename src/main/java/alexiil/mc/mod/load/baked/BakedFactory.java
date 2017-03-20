package alexiil.mc.mod.load.baked;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

import buildcraft.lib.expression.node.value.NodeVariableLong;

public abstract class BakedFactory extends BakedTickable {
    public final NodeVariableLong factoryIndex, factoryCount;
    public final BakedRenderingPart component;

    public final List<FactoryElement> elements = new LinkedList<>();
    protected int createdCount = 0;

    public BakedFactory(NodeVariableLong factoryIndex, NodeVariableLong factoryCount, BakedRenderingPart component) {
        this.factoryIndex = factoryIndex;
        this.factoryCount = factoryCount;
        this.component = component;
    }

    @Override
    public void tick(MinecraftDisplayerRenderer renderer) {
        Iterator<FactoryElement> iter = elements.iterator();
        while (iter.hasNext()) {
            FactoryElement elem = iter.next();
            if (!elem.tick(renderer)) {
                iter.remove();
            }
        }
    }

    public class FactoryElement {
        public final int elementIndex = createdCount++;

        public boolean tick(MinecraftDisplayerRenderer renderer) {
            factoryIndex.value = elementIndex;
            factoryCount.value = createdCount;
            component.tick(renderer);
            return true;
        }
    }
}
