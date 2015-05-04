package alexiil.mods.load.json;

@Deprecated
public enum EType {
    /** A single image, with nothing injected about it. */
    STATIC,
    /** A single line of text, with nothing injected into it. */
    STATIC_TEXT,
    /** A single line of text that overrides the 'text' variable with the current status. */
    DYNAMIC_TEXT_STATUS,
    /** A single line of text that overrides the 'text' variable with the function "(percentage * 100) integer + '%'". */
    DYNAMIC_TEXT_PERCENTAGE,
    /** A single image, that the width and texture width are interpolated with the respective start positions to indicate
     * the progress bar. */
    DYNAMIC_PERCENTAGE,
    /** A single image that displays a panorama screen, similar to the one used for Minecraft's Main Menu. This overrides
     * all the position logic, texture area and offsets, and both textual and animated (no text and cannot be animated) */
    DYNAMIC_PANORAMA;
}
