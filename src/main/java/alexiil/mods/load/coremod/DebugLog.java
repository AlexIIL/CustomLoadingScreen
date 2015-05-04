package alexiil.mods.load.coremod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Any methods or classes annotated with the components of this (DebugLog.Invoke, DebugLog.Return or
 * DebugLog.LogicSplit) will have logging statements inserted at the positions given by the annotation If you annotate
 * it with DebugLog then all of the possible log types will be logged (this list may expand in the future so be warned).
 * You MUST register any classes that annotate themselves with this in LogHelperTransformer, with the addToWhitelist()
 * method. Use the package base for your class, not the entire class. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD })
public @interface DebugLog {
    /** This will add a logging statements right at the very start of the method */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD })
    @interface Invoke {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD })
    @interface Return {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD })
    @interface LogicSplit {}

    /** This will make any method ignore what the class has */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.CONSTRUCTOR, ElementType.METHOD })
    @interface IgnoreClass {}
}
