package alexiil.mc.mod.load.baked.factory;

import alexiil.mc.mod.load.baked.BakedFactory;
import alexiil.mc.mod.load.baked.BakedRenderingPart;
import alexiil.mc.mod.load.baked.BakedVariable;
import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IVariableNode;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.expression.node.binary.BiNodeToBooleanType;
import buildcraft.lib.expression.node.value.NodeVariableLong;

public class BakedFactoryVariableChange extends BakedFactory {
    private final INodeBoolean shouldDestroy;
    private final IExpressionNode node;
    private final IVariableNode checkNode;
    private final BakedVariable[] variables, keptVariables;
    private final INodeBoolean hasChanged;
    private boolean hasAddedFirst;

    public BakedFactoryVariableChange(
        NodeVariableLong factoryIndex,
        NodeVariableLong factoryCount,
        BakedRenderingPart component,
        BakedVariable[] variables,
        BakedVariable[] keptVariables,
        INodeBoolean shouldDestroy,
        IExpressionNode node,
        boolean shouldSpawnFirst) throws InvalidExpressionException {

        super(factoryIndex, factoryCount, component);
        this.hasAddedFirst = !shouldSpawnFirst;
        this.variables = variables;
        this.keptVariables = keptVariables;
        this.shouldDestroy = shouldDestroy;
        this.node = node;
        this.checkNode = NodeType.getType(node).makeVariableNode("check");
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
        private final BakedVariable[] _keptVariables;

        public ElementVarChange(BakedVariable[] keptVariables) {
            _keptVariables = new BakedVariable[keptVariables.length];
            for (int i = 0; i < keptVariables.length; i++) {
                BakedVariable v = keptVariables[i];
                _keptVariables[i] = v.copyAsConstant();
            }
        }
        
        @Override
        protected void setVariables(MinecraftDisplayerRenderer renderer) {
            super.setVariables(renderer);
            for (BakedVariable v : _keptVariables) {
                v.tick(renderer);
            }
            for (BakedVariable v : variables) {
                v.tick(renderer);
            }
        }

        @Override
        public boolean tick(MinecraftDisplayerRenderer renderer) {
            super.tick(renderer);
            return !shouldDestroy.evaluate();
        }
    }
}
