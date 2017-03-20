package alexiil.mc.mod.load.baked.factory;

import java.util.List;

import alexiil.mc.mod.load.baked.BakedFactory;
import alexiil.mc.mod.load.baked.BakedRenderingPart;
import alexiil.mc.mod.load.json.subtypes.JsonFactoryVariableChange.KeptVariable;
import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.expression.node.binary.BiNodeToBooleanType;
import buildcraft.lib.expression.node.value.IVariableNode;
import buildcraft.lib.expression.node.value.NodeVariableLong;

public class BakedFactoryVariableChange extends BakedFactory {
    private final INodeBoolean shouldDestroy;
    private final IExpressionNode node;
    private final IVariableNode checkNode;
    private final List<KeptVariable> keptVariables;
    private final INodeBoolean hasChanged;
    private boolean hasAddedFirst;

    public BakedFactoryVariableChange(
        NodeVariableLong factoryIndex,
        NodeVariableLong factoryCount,
        BakedRenderingPart component,
        List<KeptVariable> keptVariables,
        INodeBoolean shouldDestroy,
        IExpressionNode node,
        boolean shouldSpawnFirst) throws InvalidExpressionException {

        super(factoryIndex, factoryCount, component);
        this.hasAddedFirst = !shouldSpawnFirst;
        this.keptVariables = keptVariables;
        this.shouldDestroy = shouldDestroy;
        this.node = node;
        this.checkNode = NodeType.getType(node).makeVariableNode();
        hasChanged = (INodeBoolean) BiNodeToBooleanType.NOT_EQUAL.createNode(node, checkNode);
    }

    @Override
    public void tick(MinecraftDisplayerRenderer renderer) {
        if (!hasAddedFirst || hasChanged.evaluate()) {
            hasAddedFirst = true;
            checkNode.set(node);
            elements.add(new ElementVarChange(keptVariables));
        }
        super.tick(renderer);
    }

    public class ElementVarChange extends FactoryElement {
        private final KeptVariable[] _keptVariables;

        public ElementVarChange(List<KeptVariable> keptVariables) {
            _keptVariables = new KeptVariable[keptVariables.size()];
            for (int i = 0; i < keptVariables.size(); i++) {
                KeptVariable v = keptVariables.get(i);
                _keptVariables[i] = new KeptVariable(NodeType.createConstantNode(v.node), v.variable);
            }
        }

        @Override
        public boolean tick(MinecraftDisplayerRenderer renderer) {
            for (KeptVariable v : _keptVariables) {
                v.variable.set(v.node);
            }
            super.tick(renderer);
            return !shouldDestroy.evaluate();
        }
    }
}
