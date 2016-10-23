package alexiil.mc.mod.load.expression.node.value;

import alexiil.mc.mod.load.expression.api.IExpressionNode;

/** Marker interface that means calling evaluate() on this will *always* return the same value. */
public interface IImmutableNode extends IExpressionNode {}
