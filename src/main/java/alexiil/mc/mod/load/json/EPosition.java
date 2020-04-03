package alexiil.mc.mod.load.json;

public enum EPosition {
    TOP_LEFT(-1, -1),
    TOP_CENTER(0, -1),
    TOP_RIGHT(1, -1),
    CENTER_LEFT(-1, 0),
    CENTER(0, 0),
    CENTER_RIGHT(1, 0),
    BOTTOM_LEFT(-1, 1),
    BOTTOM_CENTER(0, 1),
    BOTTOM_RIGHT(1, 1);

    private final int x;
    private final int y;

    EPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String getPositionFunctionX(String width, String argument) {
        if (x == -1) return argument;
        else if (x == 0) return "(" + width + ") / 2 + (" + argument + ")";
        else return "(" + width + ") + (" + argument + ")";
    }

    public String getPositionFunctionY(String height, String argument) {
        if (y == -1) return argument;
        else if (y == 0) return "(" + height + ") / 2 + (" + argument + ")";
        else return "(" + height + ") + (" + argument + ")";
    }

    public String getOffsetFunctionX(String width) {
        if (x == -1) return "0";
        if (x == 0) return "-(" + width + ")/2";
        return "-(" + width + ")";
    }

    public String getOffsetFunctionY(String height) {
        if (y == -1) return "0";
        if (y == 0) return "-(" + height + ")/2";
        return "-(" + height + ")";
    }
}
