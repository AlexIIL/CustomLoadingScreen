package alexiil.mc.mod.load.json;

import alexiil.mc.mod.load.baked.insn.BakedColourFunctional;
import alexiil.mc.mod.load.baked.insn.BakedColourFunctionalTogether;
import alexiil.mc.mod.load.baked.insn.BakedColourSimple;
import alexiil.mc.mod.load.baked.insn.BakedInsn;
import alexiil.mc.mod.load.baked.insn.BakedRotationFunctional;
import alexiil.mc.mod.load.baked.insn.BakedRotationSimple;
import alexiil.mc.mod.load.baked.insn.BakedScaleFunctional;
import alexiil.mc.mod.load.baked.insn.BakedScaleSimple;
import alexiil.mc.mod.load.baked.insn.BakedTranslateFunctional;
import alexiil.mc.mod.load.baked.insn.BakedTranslateSimple;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeConstantDouble;
import buildcraft.lib.expression.node.value.NodeConstantLong;

public abstract class JsonInsn extends JsonConfigurable<JsonInsn, BakedInsn> {
    public static class JsonInsnRotate extends JsonInsn {
        public final String angle, x, y, z;

        public JsonInsnRotate(String angle, String x, String y, String z) {
            this.angle = angle;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public BakedInsn actuallyBake(FunctionContext functions) throws InvalidExpressionException {
            INodeDouble _a = GenericExpressionCompiler.compileExpressionDouble(angle, functions);
            INodeDouble _x = GenericExpressionCompiler.compileExpressionDouble(x, functions);
            INodeDouble _y = GenericExpressionCompiler.compileExpressionDouble(y, functions);
            INodeDouble _z = GenericExpressionCompiler.compileExpressionDouble(z, functions);
            if (_a instanceof NodeConstantDouble//
                && _x instanceof NodeConstantDouble//
                && _y instanceof NodeConstantDouble//
                && _z instanceof NodeConstantDouble//
            ) {
                return new BakedRotationSimple(_a.evaluate(), _x.evaluate(), _y.evaluate(), _z.evaluate());
            }
            return new BakedRotationFunctional(_a, _x, _y, _z);
        }
    }

    public static class JsonInsnScale extends JsonInsn {
        public final String x, y, z;

        public JsonInsnScale(String x, String y, String z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public BakedInsn actuallyBake(FunctionContext functions) throws InvalidExpressionException {
            INodeDouble _x = GenericExpressionCompiler.compileExpressionDouble(x, functions);
            INodeDouble _y = GenericExpressionCompiler.compileExpressionDouble(y, functions);
            INodeDouble _z = GenericExpressionCompiler.compileExpressionDouble(z, functions);
            if (_x instanceof NodeConstantDouble//
                && _y instanceof NodeConstantDouble//
                && _z instanceof NodeConstantDouble//
            ) {
                return new BakedScaleSimple(_x.evaluate(), _y.evaluate(), _z.evaluate());
            }
            return new BakedScaleFunctional(_x, _y, _z);
        }
    }

    public static class JsonInsnColourSplit extends JsonInsn {
        public final String a, r, g, b;

        public JsonInsnColourSplit(String a, String r, String g, String b) {
            this.a = a;
            this.r = r;
            this.g = g;
            this.b = b;
        }

        @Override
        public BakedInsn actuallyBake(FunctionContext functions) throws InvalidExpressionException {
            INodeDouble _a = GenericExpressionCompiler.compileExpressionDouble(a, functions);
            INodeDouble _r = GenericExpressionCompiler.compileExpressionDouble(r, functions);
            INodeDouble _g = GenericExpressionCompiler.compileExpressionDouble(g, functions);
            INodeDouble _b = GenericExpressionCompiler.compileExpressionDouble(b, functions);
            if (_a instanceof NodeConstantDouble//
                && _r instanceof NodeConstantDouble//
                && _g instanceof NodeConstantDouble//
                && _b instanceof NodeConstantDouble//
            ) {
                return new BakedColourSimple((float) _a.evaluate(), (float) _r.evaluate(), (float) _g.evaluate(), (float) _b.evaluate());
            }
            return new BakedColourFunctional(_a, _r, _g, _b);
        }
    }

    public static class JsonInsnColourTogether extends JsonInsn {
        public final String argb;

        public JsonInsnColourTogether(String argb) {
            this.argb = argb;
            setSource(argb);
        }

        @Override
        public BakedInsn actuallyBake(FunctionContext context) throws InvalidExpressionException {
            INodeLong _argb = GenericExpressionCompiler.compileExpressionLong(argb, context);
            if (_argb instanceof NodeConstantLong) {
                long value = ((NodeConstantLong) _argb).value;
                float a = ((value >> 24) & 0xFF) / 255f;
                float r = ((value >> 16) & 0xFF) / 255f;
                float g = ((value >> 8) & 0xFF) / 255f;
                float b = (value & 0xFF) / 255f;
                return new BakedColourSimple(a, r, g, b);
            }
            return new BakedColourFunctionalTogether(_argb);
        }
    }

    public static class JsonInsnTranslate extends JsonInsn {
        public final String x, y, z;

        public JsonInsnTranslate(String x, String y, String z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public BakedInsn actuallyBake(FunctionContext context) throws InvalidExpressionException {
            INodeDouble _x = GenericExpressionCompiler.compileExpressionDouble(x, context);
            INodeDouble _y = GenericExpressionCompiler.compileExpressionDouble(y, context);
            INodeDouble _z = GenericExpressionCompiler.compileExpressionDouble(z, context);
            if (_x instanceof NodeConstantDouble//
                && _y instanceof NodeConstantDouble//
                && _z instanceof NodeConstantDouble//
            ) {
                return new BakedTranslateSimple(_x.evaluate(), _y.evaluate(), _z.evaluate());
            }
            return new BakedTranslateFunctional(_x, _y, _z);
        }
    }
}
