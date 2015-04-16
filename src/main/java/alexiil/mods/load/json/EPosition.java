package alexiil.mods.load.json;

public enum EPosition {
    TOP_LEFT(-1, -1), TOP_CENTER(0, -1), TOP_RIGHT(1, -1), CENTER_LEFT(-1, 0), CENTER(0, 0), CENTER_RIGHT(1, 0), BOTTOM_LEFT(-1, 1), BOTTOM_CENTER(0,
            1), BOTTOM_RIGHT(1, 1);

    private final int x;
    private final int y;

    private EPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    private int transform(int switcher, int coord, int screenThing) {
        switch (switcher) {
            case -1:
                return coord;
            case 0:
                return screenThing / 2 - coord;
            case 1:
                return screenThing - coord;
        }
        throw new Error("switcher (" + switcher + ") != -1, 0 or 1 (" + this.toString() + ")");
    }

    public int transformX(int x, int screenWidth) {
        return transform(this.x, x, screenWidth);
    }

    public int transformY(int y, int screenHeight) {
        return transform(this.y, y, screenHeight);
    }
}
