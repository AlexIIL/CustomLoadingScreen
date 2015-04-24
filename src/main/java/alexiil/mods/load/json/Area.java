package alexiil.mods.load.json;

public class Area {
    public final String x, y, width, height;

    public Area(double x, double y, double width, double height) {
        this.x = Double.toString(x);
        this.y = Double.toString(y);
        this.width = Double.toString(width);
        this.height = Double.toString(height);
    }

    @Override
    public String toString() {
        return "ImageTexture [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
    }
}
