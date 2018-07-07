package naoki.smallpt.textures;

import static naoki.smallpt.primitives.Reflection.DIFFUSE;
import static org.apache.commons.math3.util.FastMath.floor;

import naoki.smallpt.primitives.Point;
import naoki.smallpt.primitives.Vec;
import naoki.smallpt.primitives.combination.Col;
import naoki.smallpt.textures.surface.Surface;

public class CheckTexture extends Texture {
    final Col col1, col2;
    final double freq;

    public CheckTexture(Vec col1, Vec col2, double freq) {
        this.col1 = new Col(Vec.ZERO, col1, DIFFUSE);
        this.col2 = new Col(Vec.ZERO, col2, DIFFUSE);
        this.freq = freq;
    }

    @Override
    public Col getCol(Surface s, Vec x) {
        Point p = s.makeXY(x);
        return (under(p.x / freq) - 0.5) * (under(p.y / freq) - 0.5) > 0 ? col1 : col2;
    }
    private double under(double d) {
        return d - floor(d);
    }
}