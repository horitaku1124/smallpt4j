package naoki.smallpt.textures.surface;

import naoki.smallpt.SmallPT;
import naoki.smallpt.primitives.Point;
import naoki.smallpt.primitives.Ray;
import naoki.smallpt.primitives.Reflection;
import naoki.smallpt.primitives.Vec;
import naoki.smallpt.primitives.combination.Col;
import naoki.smallpt.textures.Texture;

final public class Plane extends Surface {
    private final double width, height;
    public Plane(double x, double y, Vec pos, Vec emission, Vec color, Reflection reflection) {
        super(pos, emission, color, reflection);
        this.width = x;
        this.height = y;
    }
    public Plane(double x, double y, Vec pos, Texture tex) {
        super(pos, tex);
        this.width = x;
        this.height = y;
    }
    @Override
    public double intersect(Ray ray, Surface[] robj) {
        if (ray.dist.z < SmallPT.EPS && ray.dist.z > -SmallPT.EPS) {
            return 0;
        }
        double d = (pos.z - ray.obj.z) / ray.dist.z;
        if (d < 0) {
            return 0;
        }
        Vec x = ray.obj.add(ray.dist.mul(d));
        Vec p = x.sub(pos);
        if (p.x < 0 || p.x > width || p.y < 0 || p.y > height) {
            return 0;
        }
        if (!texture.isHit(this, x)) {
            return 0;
        }
        robj[0] = this;
        return d;
    }

    private static final Vec TO_FRONT = new Vec(0, 0, 1);
    private static final Vec TO_BACK = new Vec(0, 0, -1);
    
    @Override
    public void position(Vec p, Ray r, Vec[] n, Col[] c) {
        n[0] =r.dist.z > 0 ? TO_BACK : TO_FRONT;
        c[0] =  texture.getCol(this, p);
    }
    
    @Override
    public Point makeXY(Vec p) {
        return new Point((p.x - pos.x) / width, (p.y - pos.y) / height);
    }
}