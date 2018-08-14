package naoki.smallpt.textures;

import naoki.smallpt.primitives.Vec;
import naoki.smallpt.primitives.Reflection;
import naoki.smallpt.primitives.combination.Col;
import naoki.smallpt.textures.surface.Surface;

public class SolidTexture extends Texture {
    private final Col col;

    public SolidTexture(Vec emission, Vec color, Reflection ref) {
        this.col = new Col(emission, color, ref);
    }
    @Override
    public Col getCol(Surface s, Vec x) {
        return col;
    }
}