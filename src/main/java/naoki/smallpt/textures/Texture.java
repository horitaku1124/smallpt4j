package naoki.smallpt.textures;

import naoki.smallpt.primitives.combination.Col;
import naoki.smallpt.textures.surface.Surface;
import naoki.smallpt.primitives.Vec;

public abstract class Texture {
    public abstract Col getCol(Surface s, Vec x);
    public boolean isHit(Surface s, Vec x)  {
        return true;
    }
}