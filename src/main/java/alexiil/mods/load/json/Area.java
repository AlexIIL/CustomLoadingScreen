package alexiil.mods.load.json;

public class Area {
    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public Area(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return "ImageTexture [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
    }
}
