package naoki.smallpt.textures;

import static naoki.smallpt.primitives.Reflection.DIFFUSE;

import naoki.smallpt.primitives.Vec;
import naoki.smallpt.primitives.combination.Col;
import naoki.smallpt.textures.surface.Surface;

public class EmissionTexture extends BitmapTexture {
    final Vec emission = new Vec(12, 12, 12);
    final Vec color = Vec.ZERO;
    public EmissionTexture(String file) {
        super(file);
    }

    @Override
    public Col getCol(Surface s, Vec x) {
        return new Col(emission, color, DIFFUSE);
    }

    @Override
    public boolean isHit(Surface s, Vec x) {
        int rgb = getRgb(s, x);
        return (rgb >> 24 != 0) && (rgb >> 16 & 255) < 80;
    }
}