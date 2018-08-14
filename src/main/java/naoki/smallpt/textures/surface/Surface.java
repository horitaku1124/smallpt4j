package naoki.smallpt.textures.surface;

import naoki.smallpt.primitives.Vec;
import naoki.smallpt.primitives.Point;
import naoki.smallpt.primitives.Ray;
import naoki.smallpt.primitives.Reflection;
import naoki.smallpt.primitives.combination.Col;
import naoki.smallpt.textures.SolidTexture;
import naoki.smallpt.textures.Texture;

public abstract class Surface {
    final Vec pos;
    final Texture texture;

    public Surface(Vec pos, Texture texture) {
        this.pos = pos;
        this.texture = texture;
    }

    public Surface(Vec pos, Vec emission, Vec color, Reflection reflection) {
        this(pos, new SolidTexture(emission, color, reflection));
    }
    
    public abstract double intersect(Ray y, Surface[] robj);
    public abstract void position(Vec p, Ray r, Vec[] n, Col[] c);
    public abstract Point makeXY(Vec p);
}